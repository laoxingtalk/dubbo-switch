package com.github.xburning.dubboswitch.repository;


import com.github.xburning.dubboswitch.entity.ZookeeperConsumer;
import com.github.xburning.dubboswitch.entity.ZookeeperProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZookeeperProviderRepository extends JpaRepository<ZookeeperProvider,Long>{

}
