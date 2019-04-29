package com.xiaowei.rabbitmq;

import com.rabbitmq.client.Channel;
import com.xiaowei.router.DynamicRouteMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Created by MOMO on 2019/2/27.
 */
@Component
@Slf4j
public class ReceiveRouteToGateway_fanout {

    /**
     *  更新 网关路由
     * @param message
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    durable = ReceiveRouteToGateway_fanout_Config.DERABLE),//是否持久化
            exchange = @Exchange(value = ReceiveRouteToGateway_fanout_Config.EXCHANGE, //交换机名称
                    durable = ReceiveRouteToGateway_fanout_Config.DERABLE,//是否持久化o
                    type = ReceiveRouteToGateway_fanout_Config.TYPE, //消息路由规则
                    ignoreDeclarationExceptions = ReceiveRouteToGateway_fanout_Config.IGNOREDECEXCEPTION) // 忽略声明异常
    )
    )
    @RabbitHandler
    public void routeMessage(Message message, Channel channel) throws Exception {
        try {
            log.info("接收 编辑routes 队列：{}", message.getPayload());
            Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
            String msg = (String) message.getPayload();
            DynamicRouteMap.put(msg);
            //统一时刻服务器只会发一条消息给消费者;
            channel.basicQos(1);
            //手工ACK
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
            log.error("接收 编辑routes 队列：{}  异常信息---》", message.getPayload(), e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
