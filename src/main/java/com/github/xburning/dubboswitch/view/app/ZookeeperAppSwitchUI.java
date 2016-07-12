package com.github.xburning.dubboswitch.view.app;

import com.github.xburning.dubboswitch.entity.ZookeeperApp;
import com.github.xburning.dubboswitch.entity.ZookeeperConsumer;
import com.github.xburning.dubboswitch.entity.ZookeeperProvider;
import com.github.xburning.dubboswitch.repository.ZookeeperAppRepository;
import com.github.xburning.dubboswitch.repository.ZookeeperConsumerRepository;
import com.github.xburning.dubboswitch.repository.ZookeeperProviderRepository;
import com.github.xburning.dubboswitch.util.DubboSwitchTool;
import com.github.xburning.dubboswitch.util.Response;
import com.vaadin.data.Item;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Zookeeper 应用服务切换
 */
@SpringUI
public class ZookeeperAppSwitchUI extends Window{

    private final ZookeeperAppRepository zookeeperAppRepository;

    private final ZookeeperConsumerRepository zookeeperConsumerRepository;

    private final ZookeeperProviderRepository zookeeperProviderRepository;

    public boolean isSwitchSuccess = false;

    private TextField appField;

    private ComboBox consumerBox;

    private ComboBox providerBox;

    private Long appId;

    private Button switchButton;

    @Autowired
    public ZookeeperAppSwitchUI(ZookeeperAppRepository zookeeperAppRepository,ZookeeperConsumerRepository zookeeperConsumerRepository,ZookeeperProviderRepository zookeeperProviderRepository) {
        super("切换应用服务");
        this.zookeeperAppRepository = zookeeperAppRepository;
        this.zookeeperConsumerRepository = zookeeperConsumerRepository;
        this.zookeeperProviderRepository = zookeeperProviderRepository;
        center();
        setModal(true);
        setClosable(true);
        setDraggable(false);
        setResizable(false);
        setWidth(350,Unit.PIXELS);
        setHeight(260,Unit.PIXELS);
        createSubmitForm();
    }

    /**
     * 创建提交表单
     */
    private void createSubmitForm() {
        FormLayout formLayout = new FormLayout();
        appField = new TextField("服务名称");
        appField.setWidth("100%");
        formLayout.addComponent(appField);
        formLayout.addComponent(createConsumerBox());
        formLayout.addComponent(createProviderBox());
        formLayout.addComponent(createSwitchButton());
        formLayout.setMargin(true);
        setContent(formLayout);
    }

    /**
     * 创建消费者列表
     * @return
     */
    private ComboBox createConsumerBox() {
        consumerBox = new ComboBox("消费者");
        consumerBox.setWidth("100%");
        consumerBox.setFilteringMode(FilteringMode.CONTAINS);
        reloadConsumerBox();
        return consumerBox;
    }

    /**
     * 重新装载消费者
     */
    private void reloadConsumerBox() {
        consumerBox.removeAllItems();
        List<ZookeeperConsumer> consumers = zookeeperConsumerRepository.findAll();
        for (ZookeeperConsumer consumer : consumers) {
            consumerBox.addItem(consumer.getId());
            consumerBox.setItemCaption(consumer.getId(),consumer.getName());
        }
    }

    /**
     * 创建提供者列表
     * @return
     */
    private ComboBox createProviderBox() {
        providerBox = new ComboBox("提供者");
        providerBox.setWidth("100%");
        providerBox.setFilteringMode(FilteringMode.CONTAINS);
        reloadProviderBox();
        return providerBox;
    }

    /**
     * 重新装载提供者
     */
    private void reloadProviderBox() {
        providerBox.removeAllItems();
        List<ZookeeperProvider> providers = zookeeperProviderRepository.findAll();
        for (ZookeeperProvider provider : providers) {
            providerBox.addItem(provider.getId());
            providerBox.setItemCaption(provider.getId(),provider.getName());
        }
    }

    /**
     * 创建切换按钮
     * @return
     */
    private Button createSwitchButton() {
        switchButton = new Button("切换", FontAwesome.BOLT);
        switchButton.addStyleName(ValoTheme.BUTTON_DANGER);
        switchButton.addClickListener((Button.ClickListener) clickEvent -> {
            Long consumerId = (Long) consumerBox.getValue();
            if (consumerId == null) {
                Notification.show("请选择消费者!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            Long providerId = (Long) providerBox.getValue();
            if (providerId == null) {
                Notification.show("请选择提供者!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            ZookeeperConsumer consumer = zookeeperConsumerRepository.findOne(consumerId);
            if (consumer == null) {
                Notification.show("消费者获取失败!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            ZookeeperProvider provider = zookeeperProviderRepository.findOne(providerId);
            if (provider == null) {
                Notification.show("提供者获取失败!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            //switch app service
            Response response = DubboSwitchTool.switchAppProvider(provider.getIp() + ":" + provider.getPort(), consumer.getIp() + ":" + consumer.getPort(), appField.getValue());
            if (!response.isSuccess()) {
                Notification.show(response.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
            updateZookeeperLastInfo(consumer, provider);
            isSwitchSuccess = true;
            close();
        });
        return switchButton;
    }

    /**
     * 更新服务应用
     * @param consumer
     * @param provider
     */
    private void updateZookeeperLastInfo(ZookeeperConsumer consumer, ZookeeperProvider provider) {
        ZookeeperApp zookeeperApp = zookeeperAppRepository.findOne(appId);
        zookeeperApp.setLastSwitchConsumer(consumer.getName());
        zookeeperApp.setLastSwitchProvider(provider.getName());
        zookeeperApp.setLastSwitchTime(new Date());
        zookeeperAppRepository.save(zookeeperApp);
    }


    public void show(Long appId,String appName) {
        this.appId = appId;
        appField.setReadOnly(false);
        appField.setValue(appName);
        appField.setReadOnly(true);
        reloadConsumerBox();
        reloadProviderBox();
    }
}
