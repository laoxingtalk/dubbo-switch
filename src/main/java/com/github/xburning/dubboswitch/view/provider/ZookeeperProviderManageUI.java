package com.github.xburning.dubboswitch.view.provider;


import com.github.xburning.dubboswitch.entity.ZookeeperProvider;
import com.github.xburning.dubboswitch.repository.ZookeeperProviderRepository;
import com.github.xburning.dubboswitch.util.DubboSwitchTool;
import com.vaadin.data.Item;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Zookeeper 提供者管理页
 */
@SpringUI
public class ZookeeperProviderManageUI extends VerticalLayout{

    private final ZookeeperProviderRepository zookeeperProviderRepository;

    private final ZookeeperProviderAddUI zookeeperProviderAddUI;

    private Grid grid;

    @Autowired
    public ZookeeperProviderManageUI(ZookeeperProviderRepository zookeeperProviderRepository, ZookeeperProviderAddUI zookeeperProviderAddUI) {
        this.zookeeperProviderRepository = zookeeperProviderRepository;
        this.zookeeperProviderAddUI = zookeeperProviderAddUI;
        createOperatePanel();
        createDataGrid();
        addProviderAddWinCloseListener();
    }

    /**
     * 创建操作面板
     */
    private void createOperatePanel() {
        Panel operatePanel = new Panel();
        operatePanel.setCaption("操作");
        operatePanel.setHeight(100,Unit.PIXELS);
        HorizontalLayout operateLayout = new HorizontalLayout();
        operateLayout.addComponent(createAddButton());
        operateLayout.addComponent(createDeleteButton());
        operateLayout.setSpacing(true);
        operateLayout.setMargin(true);
        operatePanel.setContent(operateLayout);
        addComponent(operatePanel);
    }

    /**
     * 创建删除按钮
     * @return
     */
    private Button createDeleteButton() {
        Button deleteButton = new Button("删除",FontAwesome.CLOSE);
        deleteButton.addStyleName(ValoTheme.BUTTON_DANGER);
        deleteButton.addClickListener((Button.ClickListener) clickEvent -> {
            //validate
            Collection<Object> items = grid.getSelectedRows();
            if(items.size() == 0){
                Notification.show("请选中要删除的行!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            //batch delete
            for (Object object : items) {
                Item item = grid.getContainerDataSource().getItem(object);
                if (item != null) {
                    Long id = (Long) item.getItemProperty("序号").getValue();
                    zookeeperProviderRepository.delete(id);
                }
            }
            search();
        });
        return deleteButton;
    }

    /**
     * 创建添加按钮
     * @return
     */
    private Button createAddButton() {
        Button addButton = new Button("添加", FontAwesome.PLUS);
        addButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        addButton.addClickListener((Button.ClickListener) clickEvent ->{
            zookeeperProviderAddUI.clearForm();
            UI.getCurrent().addWindow(zookeeperProviderAddUI);
        });
        return addButton;
    }

    /**
     * 创建数据展示网格
     */
    private void createDataGrid() {
        grid = new Grid();
        grid.setSizeFull();
        grid.addColumn("序号", Long.class);
        grid.addColumn("提供者名称", String.class);
        grid.addColumn("IP",String.class);
        grid.addColumn("端口",String.class);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        addTestColumnButton("测试");
        addComponent(grid);
        search();
    }

    /**
     * 添加按钮列
     * @param pId
     */
    private void addTestColumnButton(String pId) {
        Grid.Column column = grid.addColumn(pId,String.class);
        column.setWidth(100d);
        column.setRenderer(new ButtonRenderer((ClickableRenderer.RendererClickListener) rendererClickEvent -> {
            Object itemId = rendererClickEvent.getItemId();
            Item item = grid.getContainerDataSource().getItem(itemId);
            String ip = (String) item.getItemProperty("IP").getValue();
            String port = (String) item.getItemProperty("端口").getValue();
            boolean isConnected = DubboSwitchTool.isConnected(ip + ":" + port);
            if (isConnected) {
                Notification.show("连接成功!",Notification.Type.HUMANIZED_MESSAGE);
                return;
            }
            Notification.show("连接失败!",Notification.Type.ERROR_MESSAGE);
        }));
    }


    /**
     * 添加新增消费者关闭监听
     */
    private void addProviderAddWinCloseListener() {
        this.zookeeperProviderAddUI.addCloseListener((Window.CloseListener) closeEvent -> {
            if(this.zookeeperProviderAddUI.isAddSuccess){
                search();
                this.zookeeperProviderAddUI.isAddSuccess = false;
            }
        });
    }


    /**
     * 查询数据
     */
    private void search(){
        grid.getContainerDataSource().removeAllItems();
        List<ZookeeperProvider> list = zookeeperProviderRepository.findAll();
        for(ZookeeperProvider zookeeperProvider :list){
            grid.addRow(zookeeperProvider.getId(), zookeeperProvider.getName(), zookeeperProvider.getIp(),zookeeperProvider.getPort().toString(),"测试");
        }
    }



}
