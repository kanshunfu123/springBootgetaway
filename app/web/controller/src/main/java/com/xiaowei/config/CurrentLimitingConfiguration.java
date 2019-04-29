package com.xiaowei.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Created by MOMO on 2018/12/27.
 */
@Configuration
public class CurrentLimitingConfiguration {

    //根据api接口来限流
    //获取请求地址的uri作为限流key
    @Bean(name = "apiKeyResolver")
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            return Mono.just(exchange.getRequest().getPath().value());
        };
    }

    //用户限流
    //使用这种方式限流，请求路径中必须携带userId参数
    @Bean(name = "userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("userId"));
    }

    //IP限流
    @Bean(name = "ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }

}
