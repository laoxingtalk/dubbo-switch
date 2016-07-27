package com.github.xburning.dubboswitch.util;


import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DubboSwitchTool {

    private static final Logger logger = LoggerFactory.getLogger(DubboSwitchTool.class);

    private static final String DUBBO_ROOT_NODE = "/dubbo";

    private static final String DUBBO_PROVIDERS_NODE = "/providers";

    private static final String DUBBO_CONSUMERS_NODE = "/consumers";

    private static final String DUBBO_ROUTERS_NODE = "/routers";

    private static final String DUBBO_CONFIGURATORS_NODE = "/configurators";

    private static final char SLASH_SEPARATOR = '/';

    private static final char EQUALS_SEPARATOR = '=';

    private static final String APPLICATION = "application";

    private static final int ZK_SESSION_TIMEOUT = 10000;

    private static final String CHAR_ENCODING = "UTF-8";


    /**
     * 切换应用服务提供者
     * @param sourceHostPort
     * @param targetHostPort
     * @param appName
     * @return
     */
    public static Response switchAppProvider(String sourceHostPort, String targetHostPort, String appName) {
        Response response = new Response(true,"切换成功!");
        ZooKeeper sourceZk = null;
        ZooKeeper targetZk = null;
        try {
            sourceZk = connectZk(sourceHostPort);
            if (sourceZk == null) {
                response.setSuccess(false);
                response.setMessage("无法连接:" + sourceHostPort);
                return response;
            }
            boolean isExistProviders = existProviders(sourceZk, appName);
            if (!isExistProviders) {
                response.setSuccess(false);
                response.setMessage("无" + appName + "服务提供者:" + sourceHostPort);
                return response;
            }
            targetZk = connectZk(targetHostPort);
            if (targetZk == null) {
                response.setSuccess(false);
                response.setMessage("无法连接:" + targetHostPort);
                return response;
            }
            switchProviderNode(getDubboServiceBeans(sourceZk,appName), targetZk);
        } catch (Exception e) {
            logger.error("切换失败",e);
            response.setSuccess(false);
            response.setMessage("切换失败:" + e.getMessage());
        } finally {
            closeZk(sourceZk);
            closeZk(targetZk);
        }
        return response;
    }

    /**
     * 清理应用服务提供者
     * @param targetHostPort
     * @param appName
     * @return
     */
    public static Response clearAppProvider(String targetHostPort, String appName) {
        Response response = new Response(true,"清理成功!");
        ZooKeeper targetZk = null;
        try {
            targetZk = connectZk(targetHostPort);
            if (targetZk == null) {
                response.setSuccess(false);
                response.setMessage("无法连接:" + targetHostPort);
                return response;
            }
            boolean isExistProviders = existProviders(targetZk, appName);
            if (!isExistProviders) {
                response.setSuccess(false);
                response.setMessage("无" + appName + "服务提供者:" + targetHostPort);
                return response;
            }
            for (DubboServiceBean serviceBean : getDubboServiceBeans(targetZk, appName)) {
                String providerPath = DUBBO_ROOT_NODE + SLASH_SEPARATOR + serviceBean.getServiceName() + DUBBO_PROVIDERS_NODE;
                //remove all target providers
                List<String> targetProviders = targetZk.getChildren(providerPath, false);
                for (String targetProvider : targetProviders) {
                    deleteNode(targetZk, providerPath + SLASH_SEPARATOR + targetProvider);
                }
            }
        } catch (Exception e) {
            logger.error("清理失败",e);
            response.setSuccess(false);
            response.setMessage("清理失败:" + e.getMessage());
        } finally {
            closeZk(targetZk);
        }
        return response;
    }


    /**
     * 查看应用服务
     * @param targetHostPort
     * @param appName
     * @return
     */
    public static Response viewAppServices(String targetHostPort, String appName) {
        Response response = new Response(true,"查看成功!");
        ZooKeeper targetZk = null;
        try {
            targetZk = connectZk(targetHostPort);
            if (targetZk == null) {
                response.setSuccess(false);
                response.setMessage("无法连接:" + targetHostPort);
                return response;
            }
            List<DubboServiceBean> serviceBeans = getDubboServiceBeans(targetZk, appName);
            if (serviceBeans.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("无数据");
                return response;
            }
            response.setDubboServiceBeanList(serviceBeans);
        } catch (Exception e) {
            logger.error("查看失败",e);
            response.setSuccess(false);
            response.setMessage("查看失败:" + e.getMessage());
        } finally {
            closeZk(targetZk);
        }
        return response;
    }



    /**
     * 切换提供者节点
     * @param sourceServiceBeans
     * @param targetZk
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static void switchProviderNode(List<DubboServiceBean> sourceServiceBeans, ZooKeeper targetZk) throws KeeperException, InterruptedException {
        createNode(targetZk,DUBBO_ROOT_NODE);
        for (DubboServiceBean serviceBean : sourceServiceBeans) {
            String servicePath = DUBBO_ROOT_NODE + SLASH_SEPARATOR + serviceBean.getServiceName();
            createNode(targetZk, servicePath);
            String providerPath = servicePath + DUBBO_PROVIDERS_NODE;
            createNode(targetZk, providerPath);
            //remove all target providers
            List<String> targetProviders = targetZk.getChildren(providerPath, false);
            for (String targetProvider : targetProviders) {
                deleteNode(targetZk, providerPath + SLASH_SEPARATOR + targetProvider);
            }
            //create node from source providers
            List<String> sourceProviders = serviceBean.getProvidersList();
            for (String sourceProvider : sourceProviders) {
                createNode(targetZk, providerPath + SLASH_SEPARATOR + sourceProvider);
            }
        }
    }

    /**
     * 创建zk节点
     * @param zooKeeper
     * @param path
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static void createNode(ZooKeeper zooKeeper,String path) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(path, false) == null) {
            zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    /**
     * 删除zk节点
     * @param zooKeeper
     * @param path
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static void deleteNode(ZooKeeper zooKeeper,String path) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(path, false) != null) {
            zooKeeper.delete(path, -1);
        }
    }


    /**
     * 获取dubbo服务
     * @param zooKeeper
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static List<DubboServiceBean> getDubboServiceBeans(ZooKeeper zooKeeper,String appName) throws KeeperException, InterruptedException, UnsupportedEncodingException {
        List<DubboServiceBean> dubboServiceBeanList = new ArrayList<>();
        if (zooKeeper.exists(DUBBO_ROOT_NODE, false) == null) {
            return dubboServiceBeanList;
        }
        List<String> serviceNames = zooKeeper.getChildren(DUBBO_ROOT_NODE,false);
        String appNameKey = APPLICATION + EQUALS_SEPARATOR + appName;
        for (String serviceName : serviceNames) {
            String servicePath = DUBBO_ROOT_NODE + SLASH_SEPARATOR + serviceName;
            //过滤消费者
            List<String> filteredConsumersList = new ArrayList<>();
            String consumerPath = servicePath + DUBBO_CONSUMERS_NODE;
            if (zooKeeper.exists(consumerPath, false) != null) {
                List<String> consumersList = zooKeeper.getChildren(consumerPath, false);
                for (String consumer : consumersList) {
                    String _consumer = decode(consumer);
                    if (_consumer.contains(appNameKey)) {
                        filteredConsumersList.add(_consumer);
                    }
                }
            }
            //过滤提供者
            String providerPath = servicePath + DUBBO_PROVIDERS_NODE;
            List<String> filteredProvidersList = new ArrayList<>();
            if (zooKeeper.exists(providerPath, false) != null) {
                List<String> providersList = zooKeeper.getChildren(providerPath, false);
                for (String provider : providersList) {
                    String _provider = decode(provider);
                    if (_provider.contains(appNameKey)) {
                        filteredProvidersList.add(provider);
                    }
                }
            }
            if (!(filteredConsumersList.isEmpty() && filteredProvidersList.isEmpty())) {
                DubboServiceBean serviceBean = new DubboServiceBean();
                serviceBean.setServiceName(serviceName);
                serviceBean.setConsumersList(filteredConsumersList);
                serviceBean.setProvidersList(filteredProvidersList);
                dubboServiceBeanList.add(serviceBean);
            }
        }
        return dubboServiceBeanList;
    }


    public static String decode(String consumer){
        try {
            return URLDecoder.decode(consumer, CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.error("url编码失败",e);
            return consumer;
        }
    }


    /**
     * 连接zk
     * @param hostPort
     * @return
     */
    private static ZooKeeper connectZk(String hostPort) {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(hostPort, ZK_SESSION_TIMEOUT, event -> {});
            zooKeeper.getChildren(DUBBO_ROOT_NODE, false);
            return zooKeeper;
        } catch (Exception e) {
            logger.error("zk连接失败",e);
            closeZk(zooKeeper);
            return null;
        }
    }

    /**
     * 关闭zk
     * @param zookeepr
     */
    private static void closeZk(ZooKeeper zookeepr) {
        if(zookeepr != null){
            try {
                zookeepr.close();
            } catch (InterruptedException e) {
                logger.error("zk关闭失败",e);
            }
        }
    }

    /**
     * 是否连接成功
     * @param hostPort
     * @return
     */
    public static boolean isConnected(String hostPort) {
        ZooKeeper zooKeeper = connectZk(hostPort);
        if (zooKeeper == null) {
            return false;
        }
        closeZk(zooKeeper);
        return true;
    }

    /**
     * 是否有提供者
     * @param zk
     * @param appName
     * @return
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     * @throws KeeperException
     */
    private static boolean existProviders(ZooKeeper zk,String appName) throws InterruptedException, UnsupportedEncodingException, KeeperException {
        List<DubboServiceBean> serviceBeans = getDubboServiceBeans(zk, appName);
        if (serviceBeans.isEmpty()) {
            return false;
        }
        for (DubboServiceBean bean : serviceBeans) {
            List<String> providers = bean.getProvidersList();
            if (providers != null && !providers.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
