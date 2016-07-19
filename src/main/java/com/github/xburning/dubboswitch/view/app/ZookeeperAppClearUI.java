package com.github.xburning.dubboswitch.view.app;

import com.github.xburning.dubboswitch.entity.ZookeeperApp;
import com.github.xburning.dubboswitch.entity.ZookeeperConsumer;
import com.github.xburning.dubboswitch.entity.ZookeeperProvider;
import com.github.xburning.dubboswitch.repository.ZookeeperAppRepository;
import com.github.xburning.dubboswitch.repository.ZookeeperConsumerRepository;
import com.github.xburning.dubboswitch.repository.ZookeeperProviderRepository;
import com.github.xburning.dubboswitch.util.DubboSwitchTool;
import com.github.xburning.dubboswitch.util.Response;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Zookeeper 应用服务提供者清理
 */
@SpringUI
public class ZookeeperAppClearUI extends Window{

    private final ZookeeperAppRepository zookeeperAppRepository;

    private final ZookeeperConsumerRepository zookeeperConsumerRepository;

    public boolean isClearSuccess = false;

    private TextField appField;

    private ComboBox consumerBox;

    private Long appId;

    private Button clearButton;

    @Autowired
    public ZookeeperAppClearUI(ZookeeperAppRepository zookeeperAppRepository, ZookeeperConsumerRepository zookeeperConsumerRepository) {
        super("清理服务提供者");
        this.zookeeperAppRepository = zookeeperAppRepository;
        this.zookeeperConsumerRepository = zookeeperConsumerRepository;
        center();
        setModal(true);
        setClosable(true);
        setDraggable(false);
        setResizable(false);
        setWidth(350,Unit.PIXELS);
        setHeight(200,Unit.PIXELS);
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
        formLayout.addComponent(createClearButton());
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
     * 创建清理按钮
     * @return
     */
    private Button createClearButton() {
        clearButton = new Button("清理", FontAwesome.TRASH);
        clearButton.addStyleName(ValoTheme.BUTTON_DANGER);
        clearButton.setDisableOnClick(true);
        clearButton.addClickListener((Button.ClickListener) clickEvent -> {
            Long consumerId = (Long) consumerBox.getValue();
            if (consumerId == null) {
                Notification.show("请选择消费者!", Notification.Type.ERROR_MESSAGE);
                clearButton.setEnabled(true);
                return;
            }
            ZookeeperConsumer consumer = zookeeperConsumerRepository.findOne(consumerId);
            if (consumer == null) {
                Notification.show("消费者获取失败!", Notification.Type.ERROR_MESSAGE);
                clearButton.setEnabled(true);
                return;
            }
            //clear app service
            Response response = DubboSwitchTool.clearAppProvider(consumer.getIp() + ":" + consumer.getPort(), appField.getValue());
            if (!response.isSuccess()) {
                Notification.show(response.getMessage(), Notification.Type.ERROR_MESSAGE);
                clearButton.setEnabled(true);
                return;
            }
            updateZookeeperLastInfo(consumer);
            clearButton.setEnabled(true);
            isClearSuccess = true;
            close();
        });
        return clearButton;
    }

    /**
     * 更新服务应用
     * @param consumer
     */
    private void updateZookeeperLastInfo(ZookeeperConsumer consumer) {
        ZookeeperApp zookeeperApp = zookeeperAppRepository.findOne(appId);
        zookeeperApp.setLastSwitchConsumer(consumer.getName());
        zookeeperApp.setLastSwitchProvider("已清理");
        zookeeperApp.setLastSwitchTime(new Date());
        zookeeperAppRepository.save(zookeeperApp);
    }


    public void show(Long appId,String appName) {
        this.appId = appId;
        appField.setReadOnly(false);
        appField.setValue(appName);
        appField.setReadOnly(true);
        reloadConsumerBox();
    }
}
