package cn.t.ytten.metricexposer.common.handler;

import cn.t.ytten.core.channel.ChannelContext;
import cn.t.ytten.core.channel.ChannelHandler;
import cn.t.ytten.core.channel.UnPooledHeapByteBuf;
import cn.t.ytten.core.exception.DecodeException;
import cn.t.ytten.metricexposer.common.constants.MsgType;
import cn.t.ytten.metricexposer.common.message.HeartBeat;
import cn.t.ytten.metricexposer.common.message.infos.DiscInfo;
import cn.t.ytten.metricexposer.common.message.infos.NetworkInterfaceInfo;
import cn.t.ytten.metricexposer.common.message.infos.SystemInfo;
import cn.t.ytten.metricexposer.common.message.metrics.*;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchDiscInfo;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchDiscMetric;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchNetworkInterfaceInfo;
import cn.t.ytten.metricexposer.common.message.metrics.batch.BatchNetworkMetric;
import cn.t.ytten.metricexposer.common.message.request.CmdRequest;
import cn.t.ytten.metricexposer.common.message.response.CmdResponse;
import cn.t.ytten.metricexposer.common.util.MsgEncodeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageCodec implements ChannelHandler {
    @Override
    public void read(ChannelContext ctx, Object msg) throws Exception {
        UnPooledHeapByteBuf buf = (UnPooledHeapByteBuf)msg;
        while (true) {
            if(buf.readableBytes() < 4) {
                return;
            }
            int readerIndex = buf.readerIndex();
            int length = buf.readInt();
            if(buf.readableBytes() < length) {
                buf.readerIndex(readerIndex);
                return;
            }
            Object decodedMsg;
            //message type
            byte type = buf.readByte();
            if(MsgType.SYSTEM_INFO.value == type) {
                decodedMsg = decodeSystemInfo(buf);
            } else if (MsgType.DISC_INFO.value == type) {
                decodedMsg = decodeDiscInfo(buf);
            } else if (MsgType.NETWORK_INTERFACE_INFO.value == type) {
                decodedMsg = decodeNetworkInterfaceInfo(buf);
            } else if (MsgType.SYSTEM_METRIC.value == type) {
                decodedMsg = decodeSystemMetric(buf);
            } else if (MsgType.CPU_METRIC.value == type) {
                decodedMsg = decodeCpuLoadMetric(buf);
            } else if (MsgType.MEMORY_METRIC.value == type) {
                decodedMsg = decodeMemoryMetric(buf);
            } else if (MsgType.DISC_METRIC.value == type) {
                decodedMsg = decodeDiscMetric(buf);
            } else if (MsgType.NETWORK_METRIC.value == type) {
                decodedMsg = decodeNetworkMetric(buf);
            } else if (MsgType.HEARTBEAT.value == type) {
                decodedMsg = decodeHeartBeat(buf);
            } else if (MsgType.BATCH.value == type) {
                decodedMsg = decodeBatch(buf);
            } else if (MsgType.CMD_REQUEST.value == type) {
                decodedMsg = decodeCmdRequest(buf);
            } else if (MsgType.CMD_RESPONSE.value == type) {
                decodedMsg = decodeCmdResponse(buf);
            } else {
                throw new DecodeException("解析失败,未知消息类型: " +type);
            }
            ctx.getPipeline().invokeNextChannelRead(ctx.getPipeline().nextHandler(this), ctx,  decodedMsg);
        }
    }

    @Override
    public void write(ChannelContext ctx, Object msg) {
        MsgEncodeUtil.encode(ctx.getWriteCache(), msg);
    }

    private static SystemInfo decodeSystemInfo(UnPooledHeapByteBuf buf) {
        SystemInfo systemInfo = new SystemInfo();
        //os name
        systemInfo.setOsName(analyseString(buf));
        //os arch
        systemInfo.setOsArch(analyseString(buf));
        //os version
        systemInfo.setOsVersion(analyseString(buf));
        //总物理内存大小
        systemInfo.setTotalPhysicalMemorySize(buf.readLong());
        //可用物理内存大小
        systemInfo.setFreePhysicalMemorySize(buf.readLong());
        //总swap大小
        systemInfo.setTotalSwapSpaceSize(buf.readLong());
        //可用Swap大小
        systemInfo.setFreeSwapSize(buf.readLong());
        //processor数量
        systemInfo.setProcessorCount(buf.readInt());
        //cpu load
        systemInfo.setSystemCpuLoad(buf.readDouble());
        //cpu load average
        systemInfo.setSystemCpuLoadAverage(buf.readDouble());
        //磁盘
        systemInfo.setDiscInfoList(decodeDiscInfoList(buf));
        //网卡
        systemInfo.setNetworkInterfaceInfoList(decodeNetworkInterfaceInfoList(buf));
        return systemInfo;
    }

    private static List<DiscInfo> decodeDiscInfoList(UnPooledHeapByteBuf buf) {
        byte size = buf.readByte();
        if(size == 0) {
            return Collections.emptyList();
        } else {
            List<DiscInfo> discInfoCollection = new ArrayList<>();
            for (byte i = 0; i < size; i++) {
                discInfoCollection.add(decodeDiscInfo(buf));
            }
            return discInfoCollection;
        }
    }

    private static DiscInfo decodeDiscInfo(UnPooledHeapByteBuf buf) {
        DiscInfo discInfo = new DiscInfo();
        //name
        discInfo.setName(analyseString(buf));
        //type
        discInfo.setType(analyseString(buf));
        //total size
        discInfo.setTotalSize(buf.readLong());
        //free size
        discInfo.setFreeSize(buf.readLong());
        return discInfo;
    }

    private static List<NetworkInterfaceInfo> decodeNetworkInterfaceInfoList(UnPooledHeapByteBuf buf) {
        byte size = buf.readByte();
        if(size == 0) {
            return Collections.emptyList();
        } else {
            List<NetworkInterfaceInfo> networkInterfaceInfoList = new ArrayList<>();
            for (byte i = 0; i < size; i++) {
                networkInterfaceInfoList.add(decodeNetworkInterfaceInfo(buf));
            }
            return networkInterfaceInfoList;
        }
    }

    private static NetworkInterfaceInfo decodeNetworkInterfaceInfo(UnPooledHeapByteBuf buf) {
        NetworkInterfaceInfo networkInterfaceInfo = new NetworkInterfaceInfo();
        //name
        networkInterfaceInfo.setInterfaceName(analyseString(buf));
        //ip
        networkInterfaceInfo.setIp(analyseString(buf));
        //mac
        networkInterfaceInfo.setMac(analyseString(buf));
        return networkInterfaceInfo;
    }

    public static SystemMetric decodeSystemMetric(UnPooledHeapByteBuf buf) {
        SystemMetric systemMetric = new SystemMetric();
        ////可用物理内存大小
        systemMetric.setFreePhysicalMemorySize(buf.readLong());
        //可用Swap大小
        systemMetric.setFreeSwapSize(buf.readLong());
        //系统cpu负载
        systemMetric.setSystemCpuLoad(buf.readDouble());
        //系统cpu平均负载
        systemMetric.setSystemCpuLoadAverage(buf.readDouble());
        //磁盘
        systemMetric.setDiscMetricList(decodeDiscMetricList(buf));
        //网卡
        systemMetric.setNetworkMetricList(decodeNetworkMetricList(buf));
        return systemMetric;
    }

    private static List<DiscMetric> decodeDiscMetricList(UnPooledHeapByteBuf buf) {
        byte size = buf.readByte();
        if(size == 0) {
            return Collections.emptyList();
        } else {
            List<DiscMetric> discMetricList = new ArrayList<>();
            for (byte i = 0; i < size; i++) {
                discMetricList.add(decodeDiscMetric(buf));
            }
            return discMetricList;
        }
    }

    private static DiscMetric decodeDiscMetric(UnPooledHeapByteBuf buf) {
        DiscMetric discMetric = new DiscMetric();
        //name
        discMetric.setName(analyseString(buf));
        //free size
        discMetric.setFreeSize(buf.readLong());
        return discMetric;
    }

    private static List<NetworkMetric> decodeNetworkMetricList(UnPooledHeapByteBuf buf) {
        byte size = buf.readByte();
        if(size == 0) {
            return Collections.emptyList();
        } else {
            List<NetworkMetric> networkMetricList = new ArrayList<>();
            for (byte i = 0; i < size; i++) {
                networkMetricList.add(decodeNetworkMetric(buf));
            }
            return networkMetricList;
        }
    }

    private static NetworkMetric decodeNetworkMetric(UnPooledHeapByteBuf buf) {
        NetworkMetric networkMetric = new NetworkMetric();
        //name
        networkMetric.setInterfaceName(analyseString(buf));
        //tx bytes
        networkMetric.setSendBytes(buf.readLong());
        //rx bytes
        networkMetric.setReceiveBytes(buf.readLong());
        //upload
        networkMetric.setUploadBytePerSecond(buf.readInt());
        //download
        networkMetric.setDownloadBytePerSecond(buf.readInt());
        return networkMetric;
    }

    public static CpuLoadMetric decodeCpuLoadMetric(UnPooledHeapByteBuf buf) {
        CpuLoadMetric cpuLoadMetric = new CpuLoadMetric();
        //cpu load
        cpuLoadMetric.setSystemCpuLoad(buf.readDouble());
        //cpu load average
        cpuLoadMetric.setSystemCpuLoadAverage(buf.readDouble());
        return cpuLoadMetric;
    }

    public static MemoryMetric decodeMemoryMetric(UnPooledHeapByteBuf buf) {
        MemoryMetric memoryMetric = new MemoryMetric();
        //physical memory
        memoryMetric.setPhysicalMemoryFree(buf.readLong());
        //swap
        memoryMetric.setSwapMemoryFree(buf.readLong());
        return memoryMetric;
    }

    private static Object decodeBatch(UnPooledHeapByteBuf buf) {
        byte type = buf.readByte();
        if(MsgType.DISC_INFO.value == type) {
            return decodeBatchDiscInfo(buf);
        } else if(MsgType.NETWORK_INTERFACE_INFO.value == type) {
            return decodeBatchNetworkInterfaceInfo(buf);
        } else if(MsgType.DISC_METRIC.value == type) {
            return decodeBatchDiscMetric(buf);
        } else if(MsgType.NETWORK_METRIC.value == type) {
            return decodeBatchNetworkMetric(buf);
        } else {
            throw new RuntimeException("未知的消息类型: " + type);
        }
    }

    private static BatchDiscInfo decodeBatchDiscInfo(UnPooledHeapByteBuf buf) {
        BatchDiscInfo batchDiscInfo = new BatchDiscInfo();
        List<DiscInfo> readDiscInfoList = decodeDiscInfoList(buf);
        batchDiscInfo.setDiscInfoList(readDiscInfoList);
        return batchDiscInfo;
    }

    private static BatchNetworkInterfaceInfo decodeBatchNetworkInterfaceInfo(UnPooledHeapByteBuf buf) {
        BatchNetworkInterfaceInfo batchNetworkInterfaceInfo = new BatchNetworkInterfaceInfo();
        List<NetworkInterfaceInfo> networkInterfaceInfoList = decodeNetworkInterfaceInfoList(buf);
        batchNetworkInterfaceInfo.setNetworkInterfaceInfoList(networkInterfaceInfoList);
        return batchNetworkInterfaceInfo;
    }

    private static BatchDiscMetric decodeBatchDiscMetric(UnPooledHeapByteBuf buf) {
        BatchDiscMetric batchDiscMetric = new BatchDiscMetric();
        batchDiscMetric.setDiscMetricList(decodeDiscMetricList(buf));
        return batchDiscMetric;
    }

    public static BatchNetworkMetric decodeBatchNetworkMetric(UnPooledHeapByteBuf buf) {
        BatchNetworkMetric batchNetworkMetric = new BatchNetworkMetric();
        batchNetworkMetric.setNetworkMetricList(decodeNetworkMetricList(buf));
        return batchNetworkMetric;
    }

    private static CmdRequest decodeCmdRequest(UnPooledHeapByteBuf buf) {
        CmdRequest request = new CmdRequest();
        request.setCmd(analyseString(buf));
        return request;
    }

    private static CmdResponse decodeCmdResponse(UnPooledHeapByteBuf buf) {
        CmdResponse response = new CmdResponse();
        byte success = buf.readByte();
        response.setSuccess(success != 0);
        response.setOutput(analyseString(buf));
        return response;
    }

    public static HeartBeat decodeHeartBeat(UnPooledHeapByteBuf buf) {
        return HeartBeat.DEFAULT;
    }

    private static String analyseString(UnPooledHeapByteBuf buf) {
        //name
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes);
    }
}
