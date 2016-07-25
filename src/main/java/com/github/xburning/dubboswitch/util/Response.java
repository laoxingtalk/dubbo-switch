package com.github.xburning.dubboswitch.util;


import java.util.List;

public class Response {

    private boolean success;

    private String message;

    private List<DubboServiceBean> dubboServiceBeanList;

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DubboServiceBean> getDubboServiceBeanList() {
        return dubboServiceBeanList;
    }

    public void setDubboServiceBeanList(List<DubboServiceBean> dubboServiceBeanList) {
        this.dubboServiceBeanList = dubboServiceBeanList;
    }
}
