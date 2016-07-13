package com.github.xburning.dubboswitch.view;

import com.github.xburning.dubboswitch.view.app.ZookeeperAppManageUI;
import com.github.xburning.dubboswitch.view.consumer.ZookeeperConsumerManageUI;
import com.github.xburning.dubboswitch.view.provider.ZookeeperProviderManageUI;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Dubbo Switch 首页
 */
@SpringUI
@Theme("valo")
public class HomePageUI extends UI{

    private final ZookeeperAppManageUI zookeeperAppManageUI;

    private final ZookeeperConsumerManageUI zookeeperConsumerManageUI;

    private final ZookeeperProviderManageUI zookeeperProviderManageUI;

    @Autowired
    public HomePageUI(ZookeeperAppManageUI zookeeperAppManageUI,ZookeeperConsumerManageUI zookeeperConsumerManageUI,ZookeeperProviderManageUI zookeeperProviderManageUI){
        this.zookeeperAppManageUI = zookeeperAppManageUI;
        this.zookeeperConsumerManageUI = zookeeperConsumerManageUI;
        this.zookeeperProviderManageUI = zookeeperProviderManageUI;
    }

    /**
     * 首页初始化
     * @param vaadinRequest
     */
    @Override
    protected void init(VaadinRequest vaadinRequest) {
        VerticalLayout content = new VerticalLayout();
        content.addComponent(createTitle());
        content.addComponent(createTabSheet());
        setContent(content);
    }

    /**
     * 创建标题
     * @return
     */
    private HorizontalLayout createTitle() {
        HorizontalLayout layout = new HorizontalLayout();
        Image logoImg = getLogoImg();
        layout.addComponent(logoImg);
        layout.setComponentAlignment(logoImg,Alignment.MIDDLE_LEFT);
        Button githubBtn = getGithubBtn();
        layout.addComponent(githubBtn);
        layout.setComponentAlignment(githubBtn,Alignment.BOTTOM_RIGHT);
        layout.setSpacing(true);
        layout.setWidth("100%");
        layout.setHeight("80px");
        return layout;
    }


    /**
     * 创建切换标签
     * @return
     */
    private TabSheet createTabSheet() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addTab(zookeeperAppManageUI,"服务应用管理");
        tabSheet.addTab(zookeeperConsumerManageUI,"消费者管理");
        tabSheet.addTab(zookeeperProviderManageUI,"提供者管理");
        return tabSheet;
    }

    /**
     * 获取logo
     * @return
     */
    private Image getLogoImg() {
        Image image = new Image(null,getFileResource("images/dubbo_switch_logo.png",".png"));
        image.setHeight("80px");
        image.setWidth("200px");
        return image;
    }

    /**
     * 获取github
     * @return
     */
    private Button getGithubBtn() {
        Button button = new Button();
        button.setIcon(getFileResource("images/github.png",".png"));
        button.setCaption("了解更多");
        button.setStyleName(ValoTheme.BUTTON_TINY);
        button.addClickListener((Button.ClickListener) clickEvent -> getUI().getPage().open("https://github.com/xburning/dubbo-switch", "_blank"));
        return button;
    }


    private FileResource getFileResource(String path,String type){
        InputStream inputStream = null;
        try {
            ClassPathResource resource = new ClassPathResource(path);
            inputStream = resource.getInputStream();
            File tempFile = File.createTempFile("ds_" + System.currentTimeMillis(), type);
            FileUtils.copyInputStreamToFile(inputStream, tempFile);
            return new FileResource(tempFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
