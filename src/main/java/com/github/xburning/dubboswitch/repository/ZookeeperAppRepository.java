package com.github.xburning.dubboswitch.repository;


import com.github.xburning.dubboswitch.entity.ZookeeperApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZookeeperAppRepository extends JpaRepository<ZookeeperApp,Long>{

    List<ZookeeperApp> findByNameContaining(String name);

}
