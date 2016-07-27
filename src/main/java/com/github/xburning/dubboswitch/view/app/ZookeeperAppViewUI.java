package com.github.xburning.dubboswitch.view.app;

import com.github.xburning.dubboswitch.entity.ZookeeperApp;
import com.github.xburning.dubboswitch.entity.ZookeeperConsumer;
import com.github.xburning.dubboswitch.entity.ZookeeperProvider;
import com.github.xburning.dubboswitch.repository.ZookeeperAppRepository;
import com.github.xburning.dubboswitch.repository.ZookeeperConsumerRepository;
import com.github.xburning.dubboswitch.repository.ZookeeperProviderRepository;
import com.github.xburning.dubboswitch.util.DubboServiceBean;
import com.github.xburning.dubboswitch.util.DubboSwitchTool;
import com.github.xburning.dubboswitch.util.Response;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;

import static java.awt.SystemColor.menu;

/**
 * Zookeeper 查看应用服务
 */
@SpringUI
public class ZookeeperAppViewUI extends Window{

    private final ZookeeperConsumerRepository zookeeperConsumerRepository;

    private final ZookeeperProviderRepository zookeeperProviderRepository;

    private TextField appNameField;

    private ComboBox viewBox;

    private boolean isSelectedConsumer = true;

    private Tree viewTree;

    private OptionGroup zkOption;

    @Autowired
    public ZookeeperAppViewUI(ZookeeperConsumerRepository zookeeperConsumerRepository,ZookeeperProviderRepository zookeeperProviderRepository) {
        super("查看应用服务");
        this.zookeeperConsumerRepository = zookeeperConsumerRepository;
        this.zookeeperProviderRepository = zookeeperProviderRepository;
        center();
        setModal(true);
        setClosable(true);
        setDraggable(false);
        setResizable(false);
        setWidth(1000,Unit.PIXELS);
        setHeight(600,Unit.PIXELS);
        setContent();
    }

    /**
     * 设置内容
     */
    private void setContent() {
        VerticalLayout verticalLayout = new VerticalLayout();
        addOperateLayout(verticalLayout);
        addViewTree(verticalLayout);
        setContent(verticalLayout);
    }

    /**
     * 添加操作布局
     * @param verticalLayout
     */
    private void addOperateLayout(VerticalLayout verticalLayout) {
        Panel operatePanel = new Panel();
        operatePanel.setHeight(70,Unit.PIXELS);
        HorizontalLayout operateLayout = new HorizontalLayout();
        operateLayout.addComponent(createAppNameField());
        operateLayout.addComponent(createZkOptionGroup());
        operateLayout.addComponent(createViewBox());
        operateLayout.addComponent(createViewButton());
        operateLayout.setSpacing(true);
        operateLayout.setMargin(true);
        operatePanel.setContent(operateLayout);
        verticalLayout.addComponent(operatePanel);
    }

    /**
     * 添加查看树
     * @param verticalLayout
     */
    private void addViewTree(VerticalLayout verticalLayout) {
        viewTree = new Tree();
        Panel treePanel = new Panel();
        treePanel.setHeight(490,Unit.PIXELS);
        treePanel.setWidth(1000,Unit.PIXELS);
        treePanel.setContent(viewTree);
        verticalLayout.addComponent(treePanel);
    }


    /**
     * 应用名称
     * @return
     */
    private TextField createAppNameField() {
        appNameField = new TextField();
        return appNameField;
    }

    /**
     * zk
     * @return
     */
    private OptionGroup createZkOptionGroup(){
        zkOption = new OptionGroup();
        zkOption.addItem(0);
        zkOption.setItemCaption(0, "消费者");
        zkOption.addItem(1);
        zkOption.setItemCaption(1, "提供者");
        zkOption.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        zkOption.select(0);
        zkOption.setNullSelectionAllowed(false);
        zkOption.addBlurListener((FieldEvents.BlurListener) blurEvent -> {
            String selected = blurEvent.getSource().toString();
            if ("0".equals(selected)) {
                isSelectedConsumer = true;
                reloadConsumerViewBox();
            } else if ("1".equals(selected)) {
                isSelectedConsumer = false;
                reloadProviderViewBox();
            }
        });
        return zkOption;
    }

    /**
     * 创建查看列表
     * @return
     */
    private ComboBox createViewBox() {
        viewBox = new ComboBox();
        viewBox.setWidth("100%");
        viewBox.setFilteringMode(FilteringMode.OFF);
        viewBox.setTextInputAllowed(false);
        reloadConsumerViewBox();
        return viewBox;
    }

    /**
     * 重新装载消费者
     */
    private void reloadConsumerViewBox() {
        viewBox.removeAllItems();
        List<ZookeeperConsumer> consumers = zookeeperConsumerRepository.findAll();
        for (ZookeeperConsumer consumer : consumers) {
            viewBox.addItem(consumer.getId());
            viewBox.setItemCaption(consumer.getId(),consumer.getName());
        }
    }

    /**
     * 重新装载提供者
     */
    private void reloadProviderViewBox() {
        viewBox.removeAllItems();
        List<ZookeeperProvider> providers = zookeeperProviderRepository.findAll();
        for (ZookeeperProvider provider : providers) {
            viewBox.addItem(provider.getId());
            viewBox.setItemCaption(provider.getId(),provider.getName());
        }
    }

    /**
     * 查看按钮
     * @return
     */
    private Button createViewButton(){
        Button viewButton = new Button("查看",FontAwesome.EYE);
        viewButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        viewButton.setDisableOnClick(true);
        viewButton.addClickListener((Button.ClickListener) clickEvent -> {
            Long id = (Long) viewBox.getValue();
            if (id == null) {
                Notification.show("请选择要查看的" + (isSelectedConsumer ? "消费者" : "提供者") + "!", Notification.Type.ERROR_MESSAGE);
                viewButton.setEnabled(true);
                return;
            }
            ZookeeperConsumer consumer = null;
            ZookeeperProvider provider = null;
            if (isSelectedConsumer) {
                consumer = zookeeperConsumerRepository.findOne(id);
                if (consumer == null) {
                    Notification.show("消费者获取失败!", Notification.Type.ERROR_MESSAGE);
                    viewButton.setEnabled(true);
                    return;
                }
            } else {
                provider = zookeeperProviderRepository.findOne(id);
                if (provider == null) {
                    Notification.show("提供者获取失败!", Notification.Type.ERROR_MESSAGE);
                    viewButton.setEnabled(true);
                    return;
                }
            }
            String hostPort = isSelectedConsumer ? (consumer.getIp() + ":" + consumer.getPort()) : (provider.getIp() + ":" + provider.getPort());
            Response response = DubboSwitchTool.viewAppServices(hostPort, appNameField.getValue());
            if (!response.isSuccess()) {
                Notification.show(response.getMessage(), Notification.Type.ERROR_MESSAGE);
                viewButton.setEnabled(true);
                return;
            }
            buildViewUI(response.getDubboServiceBeanList());
            viewButton.setEnabled(true);
        });
        return viewButton;
    }


    /**
     * 构建UI
     * @param dubboServiceBeanList
     */
    private void buildViewUI(List<DubboServiceBean> dubboServiceBeanList) {
        viewTree.removeAllItems();
        for (DubboServiceBean dubboServiceBean : dubboServiceBeanList) {
            String serviceName = dubboServiceBean.getServiceName();
            viewTree.addItem(serviceName);
            viewTree.expandItem(serviceName);
            viewTree.setItemIcon(serviceName,FontAwesome.CLOUD);
            addServiceNode(dubboServiceBean, serviceName,"consumers");
            addServiceNode(dubboServiceBean, serviceName,"providers");
        }
    }

    /**
     * 添加节点
     * @param dubboServiceBean
     * @param serviceName
     * @param node
     */
    private void addServiceNode(DubboServiceBean dubboServiceBean, String serviceName,String node) {
        String serviceNode = serviceName + "->" + node;
        viewTree.addItem(serviceNode);
        viewTree.setParent(serviceNode, serviceName);
        viewTree.expandItem(serviceNode);
        List<String> nodeList = null;
        if ("providers".equals(node)) {
            nodeList = dubboServiceBean.getProvidersList();
        } else if ("consumers".equals(node)) {
            nodeList = dubboServiceBean.getConsumersList();
        }
        if (nodeList != null) {
            for (String _node : nodeList) {
                String url = DubboSwitchTool.decode(_node);
                viewTree.addItem(url);
                viewTree.setParent(url, _node);
                viewTree.setItemIcon(url,FontAwesome.STAR_O);
                viewTree.setChildrenAllowed(url, false);
            }
        }
    }

    /**
     * 显示
     * @param appName
     */
    public void show(String appName) {
        appNameField.setReadOnly(false);
        appNameField.setValue(appName);
        appNameField.setReadOnly(true);
        zkOption.select(0);
        reloadConsumerViewBox();
        viewTree.removeAllItems();
    }
}
