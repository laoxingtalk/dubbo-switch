package com.github.xburning.dubboswitch.view.provider;

import com.github.xburning.dubboswitch.entity.ZookeeperConsumer;
import com.github.xburning.dubboswitch.entity.ZookeeperProvider;
import com.github.xburning.dubboswitch.repository.ZookeeperConsumerRepository;
import com.github.xburning.dubboswitch.repository.ZookeeperProviderRepository;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * 添加提供者Zookeeper
 */
@SpringUI
public class ZookeeperProviderAddUI extends Window{

    private final ZookeeperProviderRepository zookeeperProviderRepository;

    //是否添加成功
    public boolean isAddSuccess = false;

    private TextField nameField;

    private TextField ipField;

    private TextField portField;

    @Autowired
    public ZookeeperProviderAddUI(ZookeeperProviderRepository zookeeperProviderRepository) {
        super("添加提供者");
        this.zookeeperProviderRepository = zookeeperProviderRepository;
        center();
        setModal(true);
        setClosable(true);
        setDraggable(false);
        setResizable(false);
        setWidth(330,Unit.PIXELS);
        setHeight(260,Unit.PIXELS);
        createSubmitForm();
    }

    /**
     * 创建提交表单
     */
    private void createSubmitForm() {
        FormLayout formLayout = new FormLayout();
        nameField = new TextField();
        nameField.setCaption("提供者名称");
        ipField = new TextField();
        ipField.setCaption("IP");
        portField = new TextField();
        portField.setCaption("端口");
        formLayout.addComponent(nameField);
        formLayout.addComponent(ipField);
        formLayout.addComponent(portField);
        formLayout.addComponent(createSaveButton());
        formLayout.setMargin(true);
        setContent(formLayout);
    }

    /**
     * 创建保存按钮
     * @return
     */
    private Button createSaveButton() {
        Button saveButton = new Button("保存", FontAwesome.CHECK);
        saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveButton.addClickListener((Button.ClickListener) clickEvent -> {
            String name = nameField.getValue();
            if (StringUtils.isEmpty(name)) {
                Notification.show("提供者名称不能为空!",Notification.Type.ERROR_MESSAGE);
                return;
            }
            String ip = ipField.getValue();
            if (StringUtils.isEmpty(ip)) {
                Notification.show("IP不能为空!",Notification.Type.ERROR_MESSAGE);
                return;
            }
            String port = portField.getValue();
            if (StringUtils.isEmpty(port)) {
                Notification.show("端口不能为空!",Notification.Type.ERROR_MESSAGE);
                return;
            }
            //insert data
            ZookeeperProvider provider = new ZookeeperProvider();
            provider.setName(name);
            provider.setIp(ip);
            provider.setPort(Integer.parseInt(port));
            zookeeperProviderRepository.save(provider);
            isAddSuccess = true;
            close();
        });
        return saveButton;
    }

    /**
     * 重置表单
     */
    public void clearForm() {
        nameField.clear();
        ipField.clear();
        portField.clear();
    }

}
