package com.github.xburning.dubboswitch.view.app;

import com.github.xburning.dubboswitch.entity.ZookeeperApp;
import com.github.xburning.dubboswitch.repository.ZookeeperAppRepository;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * 添加Zookeeper 应用服务
 */
@SpringUI
public class ZookeeperAppAddUI extends Window{

    private final ZookeeperAppRepository zookeeperAppRepository;

    public boolean isAddSuccess = false;

    private TextField nameField;

    @Autowired
    public ZookeeperAppAddUI(ZookeeperAppRepository zookeeperAppRepository) {
        super("添加应用服务");
        this.zookeeperAppRepository = zookeeperAppRepository;
        center();
        setModal(true);
        setClosable(true);
        setDraggable(false);
        setResizable(false);
        setWidth(300,Unit.PIXELS);
        setHeight(150,Unit.PIXELS);
        createSubmitForm();
    }

    /**
     * 创建提交表单
     */
    private void createSubmitForm() {
        FormLayout formLayout = new FormLayout();
        nameField = new TextField();
        nameField.setCaption("服务名称");
        formLayout.addComponent(nameField);
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
                Notification.show("服务名称不能为空!",Notification.Type.ERROR_MESSAGE);
                return;
            }
            //insert data
            ZookeeperApp zookeeperApp = new ZookeeperApp();
            zookeeperApp.setName(name);
            zookeeperAppRepository.save(zookeeperApp);
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
    }
}
