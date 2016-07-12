package com.github.xburning.dubboswitch.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity(name="T_ZOOKEEPER_APP")
public class ZookeeperApp {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String lastSwitchConsumer;

    private String lastSwitchProvider;

    private Date lastSwitchTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastSwitchConsumer() {
        return lastSwitchConsumer;
    }

    public void setLastSwitchConsumer(String lastSwitchConsumer) {
        this.lastSwitchConsumer = lastSwitchConsumer;
    }

    public String getLastSwitchProvider() {
        return lastSwitchProvider;
    }

    public void setLastSwitchProvider(String lastSwitchProvider) {
        this.lastSwitchProvider = lastSwitchProvider;
    }

    public Date getLastSwitchTime() {
        return lastSwitchTime;
    }

    public void setLastSwitchTime(Date lastSwitchTime) {
        this.lastSwitchTime = lastSwitchTime;
    }
}
