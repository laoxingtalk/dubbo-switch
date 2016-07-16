package com.github.xburning.dubboswitch.view.app;


import com.github.xburning.dubboswitch.entity.ZookeeperApp;
import com.github.xburning.dubboswitch.repository.ZookeeperAppRepository;
import com.vaadin.data.Item;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Zookeeper 应用服务切换页
 */
@SpringUI
public class ZookeeperAppManageUI extends VerticalLayout{

    private final ZookeeperAppRepository zookeeperAppRepository;

    private final ZookeeperAppAddUI zookeeperAppAddUI;

    private final ZookeeperAppSwitchUI zookeeperAppSwitchUI;

    private Grid grid;

    private TextField filterField;

    private Button searchButton;

    @Autowired
    public ZookeeperAppManageUI(ZookeeperAppRepository zookeeperAppRepository, ZookeeperAppAddUI zookeeperAppAddUI,ZookeeperAppSwitchUI zookeeperAppSwitchUI) {
        this.zookeeperAppRepository = zookeeperAppRepository;
        this.zookeeperAppAddUI = zookeeperAppAddUI;
        this.zookeeperAppSwitchUI = zookeeperAppSwitchUI;
        createOperatePanel();
        createDataGrid();
        addAppAddWinCloseListener();
        addAppSwitchWinCloseListener();
    }

    /**
     * 创建操作面板
     */
    private void createOperatePanel() {
        Panel operatePanel = new Panel();
        operatePanel.setCaption("操作");
        operatePanel.setHeight(100,Unit.PIXELS);
        HorizontalLayout operateLayout = new HorizontalLayout();
        operateLayout.addComponent(createFilterField());
        operateLayout.addComponent(createSearchButton());
        operateLayout.addComponent(createAddButton());
        operateLayout.addComponent(createDeleteButton());
        operateLayout.setSpacing(true);
        operateLayout.setMargin(true);
        operatePanel.setContent(operateLayout);
        addComponent(operatePanel);
    }


    /**
     * 创建查询过滤
     * @return
     */
    private TextField createFilterField() {
        filterField = new TextField();
        filterField.setDescription("服务名称");
        return filterField;
    }

    /**
     * 创建查询按钮
     * @return
     */
    private Button createSearchButton() {
        searchButton = new Button("查询",FontAwesome.SEARCH);
        searchButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        searchButton.addClickListener((Button.ClickListener) clickEvent -> {
            search();
        });
        return searchButton;
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
                if(item != null){
                    Long id = (Long) item.getItemProperty("序号").getValue();
                    zookeeperAppRepository.delete(id);
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
        Button addButton = new Button("添加",FontAwesome.PLUS);
        addButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        addButton.addClickListener((Button.ClickListener) clickEvent ->{
            zookeeperAppAddUI.clearForm();
            UI.getCurrent().addWindow(zookeeperAppAddUI);
        });
        return addButton;
    }

    /**
     * 创建数据展示网格
     */
    private void createDataGrid() {
        grid = new Grid();
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn("序号", Long.class);
        grid.addColumn("应用名称", String.class);
        grid.addColumn("已切换消费者",String.class);
        grid.addColumn("已切换提供者",String.class);
        grid.addColumn("切换时间",String.class);
        grid.addColumn("操作",String.class);
        grid.getColumn("操作").setRenderer(new ButtonRenderer((ClickableRenderer.RendererClickListener) rendererClickEvent -> {
            Object itemId = rendererClickEvent.getItemId();
            Item item = grid.getContainerDataSource().getItem(itemId);
            Long appId = (Long) item.getItemProperty("序号").getValue();
            String appName = (String) item.getItemProperty("应用名称").getValue();
            zookeeperAppSwitchUI.show(appId,appName);
            UI.getCurrent().addWindow(zookeeperAppSwitchUI);
        }));
        addComponent(grid);
        search();
    }


    /**
     * 添加新增应用服务关闭监听
     */
    private void addAppAddWinCloseListener() {
        this.zookeeperAppAddUI.addCloseListener((Window.CloseListener) closeEvent -> {
            if (this.zookeeperAppAddUI.isAddSuccess) {
                search();
                this.zookeeperAppAddUI.isAddSuccess = false;
            }
        });
    }

    /**
     * 添加应用服务切换关闭监听
     */
    private void addAppSwitchWinCloseListener() {
        this.zookeeperAppSwitchUI.addCloseListener((Window.CloseListener) closeEvent -> {
            if (this.zookeeperAppSwitchUI.isSwitchSuccess) {
                search();
                this.zookeeperAppSwitchUI.isSwitchSuccess = false;
            }
        });
    }


    /**
     * 查询数据
     */
    private void search(){
        grid.getContainerDataSource().removeAllItems();
        List<ZookeeperApp> list;
        String filterName = filterField.getValue();
        if (StringUtils.isEmpty(filterName)) {
            list = zookeeperAppRepository.findAll();
        }else{
            list = zookeeperAppRepository.findByNameContaining(filterName);
        }
        for(ZookeeperApp zookeeperApp :list){
            Date switchTime = zookeeperApp.getLastSwitchTime();
            String lastSwitchTime = switchTime == null ? "": new DateTime(switchTime).toString("yyyy-MM-dd HH:mm:ss");
            grid.addRow(zookeeperApp.getId(), zookeeperApp.getName(), zookeeperApp.getLastSwitchConsumer(),zookeeperApp.getLastSwitchProvider(), lastSwitchTime,"选择切换");
        }
    }

}
