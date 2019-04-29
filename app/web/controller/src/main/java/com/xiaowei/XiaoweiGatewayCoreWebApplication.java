package com.xiaowei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient //让注册中心能够发现 ,可以是其他注册中心
@EnableCircuitBreaker //开启 断路器(熔断器)
@ComponentScan(basePackages = "com.xiaowei")
@EnableFeignClients({"com.xiaowei.feign"}) //调用其他服务的api
@SpringBootApplication
public class XiaoweiGatewayCoreWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(XiaoweiGatewayCoreWebApplication.class, args);
	}

}
