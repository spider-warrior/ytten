package cn.t.ytten.metricexposer.common.message.metrics.batch;

import cn.t.ytten.metricexposer.common.message.infos.NetworkInterfaceInfo;

import java.util.List;

public class BatchNetworkInterfaceInfo {
    private List<NetworkInterfaceInfo> networkInterfaceInfoList;

    public List<NetworkInterfaceInfo> getNetworkInterfaceInfoList() {
        return networkInterfaceInfoList;
    }

    public void setNetworkInterfaceInfoList(List<NetworkInterfaceInfo> networkInterfaceInfoList) {
        this.networkInterfaceInfoList = networkInterfaceInfoList;
    }

    @Override
    public String toString() {
        return "BatchNetworkInterfaceInfo{" +
                "networkInterfaceInfoList=" + networkInterfaceInfoList +
                '}';
    }
}
