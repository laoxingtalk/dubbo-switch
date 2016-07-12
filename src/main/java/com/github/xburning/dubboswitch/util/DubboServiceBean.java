package com.github.xburning.dubboswitch.util;


import java.io.Serializable;
import java.util.List;

public class DubboServiceBean implements Serializable{

    private String serviceName;
    private List<String> consumersList;
    private List<String> routersList;
    private List<String> configuratorsList;
    private List<String> providersList;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getProvidersList() {
        return providersList;
    }

    public void setProvidersList(List<String> providersList) {
        this.providersList = providersList;
    }

    public List<String> getConsumersList() {
        return consumersList;
    }

    public void setConsumersList(List<String> consumersList) {
        this.consumersList = consumersList;
    }

    public List<String> getRoutersList() {
        return routersList;
    }

    public void setRoutersList(List<String> routersList) {
        this.routersList = routersList;
    }

    public List<String> getConfiguratorsList() {
        return configuratorsList;
    }

    public void setConfiguratorsList(List<String> configuratorsList) {
        this.configuratorsList = configuratorsList;
    }
}
