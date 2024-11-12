package cn.t.ytten.metricexposer.common.util;

import cn.t.ytten.core.channel.UnPooledHeapByteBuf;
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

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MsgEncodeUtil {

    private static final Map<Class<?>, Byte> classMsgTypeMap = Stream.of(
            new AbstractMap.SimpleEntry<>(HeartBeat.class, MsgType.HEARTBEAT.value),

            new AbstractMap.SimpleEntry<>(SystemInfo.class, MsgType.SYSTEM_INFO.value),
            new AbstractMap.SimpleEntry<>(DiscInfo.class, MsgType.DISC_INFO.value),
            new AbstractMap.SimpleEntry<>(NetworkInterfaceInfo.class, MsgType.NETWORK_INTERFACE_INFO.value),

            new AbstractMap.SimpleEntry<>(SystemMetric.class, MsgType.SYSTEM_METRIC.value),
            new AbstractMap.SimpleEntry<>(CpuLoadMetric.class, MsgType.CPU_METRIC.value),
            new AbstractMap.SimpleEntry<>(MemoryMetric.class, MsgType.MEMORY_METRIC.value),
            new AbstractMap.SimpleEntry<>(DiscMetric.class, MsgType.DISC_METRIC.value),
            new AbstractMap.SimpleEntry<>(NetworkMetric.class, MsgType.NETWORK_METRIC.value),
            new AbstractMap.SimpleEntry<>(BatchDiscInfo.class, MsgType.BATCH.value),
            new AbstractMap.SimpleEntry<>(BatchNetworkInterfaceInfo.class, MsgType.BATCH.value),
            new AbstractMap.SimpleEntry<>(BatchDiscMetric.class, MsgType.BATCH.value),
            new AbstractMap.SimpleEntry<>(BatchNetworkMetric.class, MsgType.BATCH.value),
            new AbstractMap.SimpleEntry<>(CmdRequest.class, MsgType.CMD_REQUEST.value),
            new AbstractMap.SimpleEntry<>(CmdResponse.class, MsgType.CMD_RESPONSE.value)
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


    private static final Map<Byte, Integer> msgTypeLengthMap = Stream.of(
            new AbstractMap.SimpleEntry<>(MsgType.HEARTBEAT.value, 5),

            new AbstractMap.SimpleEntry<>(MsgType.SYSTEM_INFO.value, 1024),
            new AbstractMap.SimpleEntry<>(MsgType.DISC_INFO.value, 256),
            new AbstractMap.SimpleEntry<>(MsgType.NETWORK_INTERFACE_INFO.value, 256),

            new AbstractMap.SimpleEntry<>(MsgType.SYSTEM_METRIC.value, 1024),
            new AbstractMap.SimpleEntry<>(MsgType.CPU_METRIC.value, 256),
            new AbstractMap.SimpleEntry<>(MsgType.MEMORY_METRIC.value, 256),
            new AbstractMap.SimpleEntry<>(MsgType.DISC_METRIC.value, 256),
            new AbstractMap.SimpleEntry<>(MsgType.NETWORK_METRIC.value, 256),
            new AbstractMap.SimpleEntry<>(MsgType.CMD_REQUEST.value, 256),
            new AbstractMap.SimpleEntry<>(MsgType.CMD_RESPONSE.value, 1024),

            new AbstractMap.SimpleEntry<>(MsgType.BATCH.value, 1024)
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


    private static final Map<String, byte[]> stringBytesCache = new WeakHashMap<>();

    public static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, Object message) {
        if(message instanceof SystemInfo) {
            return encode(buf, (SystemInfo)message);
        } else if(message instanceof DiscInfo) {
            return encode(buf, (DiscInfo)message);
        } else if(message instanceof NetworkInterfaceInfo) {
            return encode(buf, (NetworkInterfaceInfo)message);
        } else if(message instanceof SystemMetric) {
            return encode(buf, (SystemMetric)message);
        } else if(message instanceof CpuLoadMetric) {
            return encode(buf, (CpuLoadMetric)message);
        } else if(message instanceof DiscMetric) {
            return encode(buf, (DiscMetric)message);
        } else if(message instanceof MemoryMetric) {
            return encode(buf, (MemoryMetric)message);
        } else if(message instanceof NetworkMetric) {
            return encode(buf, (NetworkMetric)message);
        } else if(message instanceof HeartBeat) {
            return encode(buf, (HeartBeat)message);
        } else if(message instanceof BatchDiscInfo) {
            return encodeBatchDiscInfo(buf, (BatchDiscInfo)message);
        } else if(message instanceof BatchNetworkInterfaceInfo) {
            return encodeBatchNetworkInterfaceInfo(buf, (BatchNetworkInterfaceInfo) message);
        } else if(message instanceof BatchDiscMetric) {
            return encodeBatchDiscMetric(buf, (BatchDiscMetric)message);
        } else if(message instanceof BatchNetworkMetric) {
            return encodeBatchNetworkMetric(buf, (BatchNetworkMetric)message);
        } else if(message instanceof CmdRequest) {
            return encodeCmdRequest(buf, (CmdRequest)message);
        } else if(message instanceof CmdResponse) {
            return encodeCmdResponse(buf, (CmdResponse)message);
        } else {
            throw new RuntimeException("不支持编码的消息: " + message);
        }
    }

    public static UnPooledHeapByteBuf encodeCmdRequest(UnPooledHeapByteBuf buf, CmdRequest request) {
        //cmd
        writeString(buf, request.getCmd());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", request.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encodeCmdResponse(UnPooledHeapByteBuf buf, CmdResponse response) {
        //success
        buf.writeByte(response.isSuccess() ? (byte)1 : 0b0);
        //output
        buf = writeString(buf, response.getOutput());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", response.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encodeBatchDiscInfo(UnPooledHeapByteBuf buf, BatchDiscInfo batchDiscInfo) {
        //content type
        buf.writeByte(MsgType.DISC_INFO.value);
        //content list
        writeDiscInfoCollection(buf, batchDiscInfo.getDiscInfoList());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", batchDiscInfo.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encodeBatchNetworkInterfaceInfo(UnPooledHeapByteBuf buf, BatchNetworkInterfaceInfo batchNetworkInterfaceInfo) {
        //content type
        buf.writeByte(MsgType.NETWORK_INTERFACE_INFO.value);
        //content list
        writeNetworkInterfaceInfoCollection(buf, batchNetworkInterfaceInfo.getNetworkInterfaceInfoList());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", batchNetworkInterfaceInfo.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encodeBatchDiscMetric(UnPooledHeapByteBuf buf, BatchDiscMetric batchDiscMetric) {
        //content type
        buf.writeByte(MsgType.DISC_METRIC.value);
        //content list
        writeDiscMetricCollection(buf, batchDiscMetric.getDiscMetricList());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", batchDiscMetric.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encodeBatchNetworkMetric(UnPooledHeapByteBuf buf, BatchNetworkMetric batchNetworkMetric) {
        //content type
        buf.writeByte(MsgType.NETWORK_METRIC.value);
        //content list
        writeNetworkMetricCollection(buf, batchNetworkMetric.getNetworkMetricList());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", batchNetworkMetric.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    private static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, NetworkInterfaceInfo info) {
        writeNetworkInterfaceInfo(buf, info);
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", info.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    private static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, DiscInfo info) {
        writeDiscInfo(buf, info);
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", info.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, HeartBeat heartBeat) {
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", heartBeat.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, SystemInfo info) {
        //os name
        writeString(buf, info.getOsName());
        //os arch
        writeString(buf, info.getOsArch());
        //os version
        writeString(buf, info.getOsVersion());
        //总物理内存大小
        buf.writeLong(info.getTotalPhysicalMemorySize());
        //剩余物理内存大小
        buf.writeLong(info.getFreePhysicalMemorySize());
        //总swap大小
        buf.writeLong(info.getTotalSwapSpaceSize());
        //剩余swap大小
        buf.writeLong(info.getFreeSwapSize());
        //processor数量
        buf.writeInt(info.getProcessorCount());
        //cpu load
        buf.writeDouble(info.getSystemCpuLoad());
        //cpi load average
        buf.writeDouble(info.getSystemCpuLoadAverage());
        //磁盘
        writeDiscInfoCollection(buf, info.getDiscInfoList());
        //网卡
        writeNetworkInterfaceInfoCollection(buf, info.getNetworkInterfaceInfoList());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", info.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    private static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, SystemMetric metric) {
        //可用物理内存大小
        buf.writeLong(metric.getFreePhysicalMemorySize());
        //可用Swap大小
        buf.writeLong(metric.getFreeSwapSize());
        //系统cpu负载
        buf.writeDouble(metric.getSystemCpuLoad());
        //系统cpu平均负载
        buf.writeDouble(metric.getSystemCpuLoadAverage());
        //磁盘
        writeDiscMetricCollection(buf, metric.getDiscMetricList());
        //网卡
        writeNetworkMetricCollection(buf, metric.getNetworkMetricList());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", metric.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, CpuLoadMetric metric) {
        //cpu load
        buf.writeDouble(metric.getSystemCpuLoad());
        //cpu load average
        buf.writeDouble(metric.getSystemCpuLoadAverage());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", metric.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, DiscMetric metric) {
        //name
        writeString(buf, metric.getName());
        //free size
        buf.writeLong(metric.getFreeSize());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", metric.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    public static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, MemoryMetric metric) {
        //physical memory
        buf.writeLong(metric.getPhysicalMemoryFree());
        //swap
        buf.writeLong(metric.getSwapMemoryFree());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", metric.getClass().getSimpleName(), buffer.position());
        return buf;
    }
    public static UnPooledHeapByteBuf encode(UnPooledHeapByteBuf buf, NetworkMetric metric) {
        //name
        writeString(buf, metric.getInterfaceName());
        //tx bytes
        buf.writeLong(metric.getSendBytes());
        //rx bytes
        buf.writeLong(metric.getReceiveBytes());
        //upload
        buf.writeInt(metric.getUploadBytePerSecond());
        //download
        buf.writeInt(metric.getDownloadBytePerSecond());
        //length
        writeLength(buf);
//        System.out.printf("encode %s, buffer size: %d%n", metric.getClass().getSimpleName(), buffer.position());
        return buf;
    }

    private static void writeDiscInfoCollection(UnPooledHeapByteBuf buf, Collection<DiscInfo> discInfoCollection) {
        if(discInfoCollection == null || discInfoCollection.isEmpty()) {
            buf.writeByte((byte)0);
        } else {
            buf.writeByte((byte)discInfoCollection.size());
            discInfoCollection.forEach(discInfo -> writeDiscInfo(buf, discInfo));
        }
    }

    private static void writeDiscInfo(UnPooledHeapByteBuf buf, DiscInfo discInfo) {
        //name
        writeString(buf, discInfo.getName());
        //type
        writeString(buf, discInfo.getType());
        //total size
        buf.writeLong(discInfo.getTotalSize());
        //free size
        buf.writeLong(discInfo.getFreeSize());
    }

    private static void writeNetworkInterfaceInfoCollection(UnPooledHeapByteBuf buf, Collection<NetworkInterfaceInfo> networkInterfaceInfoCollection) {
        if(networkInterfaceInfoCollection == null || networkInterfaceInfoCollection.isEmpty()) {
            buf.writeByte((byte)0);
        } else {
            buf.writeByte((byte)networkInterfaceInfoCollection.size());
            networkInterfaceInfoCollection.forEach(networkInterfaceInfo -> writeNetworkInterfaceInfo(buf, networkInterfaceInfo));
        }
    }

    private static void writeNetworkInterfaceInfo(UnPooledHeapByteBuf buf, NetworkInterfaceInfo networkInterfaceInfo) {
        //name
        writeString(buf, networkInterfaceInfo.getInterfaceName());
        //ip
        writeString(buf, networkInterfaceInfo.getIp());
        //mac
        writeString(buf, networkInterfaceInfo.getMac());
    }

    private static void writeNetworkMetricCollection(UnPooledHeapByteBuf buf, Collection<NetworkMetric> networkMetricCollection) {
        if(networkMetricCollection == null || networkMetricCollection.isEmpty()) {
            buf.writeByte((byte)0);
        } else {
            buf.writeByte((byte)networkMetricCollection.size());
            networkMetricCollection.forEach(networkMetric -> writeNetworkMetric(buf, networkMetric));
        }
    }

    private static void writeNetworkMetric(UnPooledHeapByteBuf buf, NetworkMetric networkMetric) {
        //name
        writeString(buf, networkMetric.getInterfaceName());
        //tx bytes
        buf.writeLong(networkMetric.getSendBytes());
        //rx bytes
        buf.writeLong(networkMetric.getReceiveBytes());
        //upload
        buf.writeLong(networkMetric.getUploadBytePerSecond());
        //download
        buf.writeLong(networkMetric.getDownloadBytePerSecond());
    }

    private static void writeDiscMetricCollection(UnPooledHeapByteBuf buf, Collection<DiscMetric> discMetricCollection) {
        if(discMetricCollection == null || discMetricCollection.isEmpty()) {
            buf.writeByte((byte)0);
        } else {
            buf.writeByte((byte)discMetricCollection.size());
            discMetricCollection.forEach(discMetric -> writeDiscMetric(buf, discMetric));
        }
    }

    private static void writeDiscMetric(UnPooledHeapByteBuf buf, DiscMetric discMetric) {
        //name
        writeString(buf, discMetric.getName());
        //free size
        buf.writeLong(discMetric.getFreeSize());
    }

    private static UnPooledHeapByteBuf writeString(UnPooledHeapByteBuf buf, String data) {
        byte[] bytes = stringBytesCache.computeIfAbsent(data, key -> data.getBytes());
        return buf.writeInt(bytes.length).writeBytes(bytes);
    }

    private static ByteBuffer allocate(Object msg) {
        Byte msgType = classMsgTypeMap.get(msg.getClass());
        Integer length = msgTypeLengthMap.get(msgType);
        ByteBuffer buffer = ByteBuffer.allocate(length);
        //length
        buffer.putInt(0);
        //message type
        buffer.put(msgType);
        return buffer;
    }

    private static void writeLength(UnPooledHeapByteBuf buf) {
        int length = buf.writerIndex() - 4;
        int writePosition = buf.writerIndex();
        buf.writerIndex(0);
        buf.writeInt(length);
        buf.writerIndex(writePosition);
    }
}
