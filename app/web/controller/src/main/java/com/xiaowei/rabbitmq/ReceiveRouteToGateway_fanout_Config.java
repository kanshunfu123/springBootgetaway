package com.xiaowei.rabbitmq;

/**
 * 消息路由规则：四种模式（topic、direct、fanout、header）
 * topic：根据绑定关键字通配符规则匹配、比较灵活
 * direct：默认，根据routingKey完全匹配，好处是先匹配再发送
 * fanout：不需要指定routingkey，相当于群发
 * header：不太常用，可以自定义匹配规则
 Broker:它提供一种传输服务,它的角色就是维护一条从生产者到消费者的路线，保证数据能按照指定的方式进行传输,
 Exchange：消息交换机,它指定消息按什么规则,路由到哪个队列。
 Queue:消息的载体,每个消息都会被投到一个或多个队列。
 Binding:绑定，它的作用就是把exchange和queue按照路由规则绑定起来.
 Routing Key:路由关键字,exchange根据这个关键字进行消息投递。
 vhost:虚拟主机,一个broker里可以有多个vhost，用作不同用户的权限分离。
 Producer:消息生产者,就是投递消息的程序.
 Consumer:消息消费者,就是接受消息的程序.
 Channel:消息通道,在客户端的每个连接里,可建立多个channel.
 */
public class ReceiveRouteToGateway_fanout_Config {
    //交换机名称
    public final static String EXCHANGE = "routes-exchange";
    //路由key
    public final static String ROUTINGKEY_V1 = "add_route.momo";
    public final static String ROUTINGKEY_V2 = "modify_route.momo";
    //队列名称
    public final static String QUEUE = "route";
    //是否持久化
    public final static String DERABLE = "true";
    //消息路由规则
    public final static String TYPE = "fanout";
    // 忽略声明异常
    public final static String IGNOREDECEXCEPTION = "true";
    /**
     *  绑定的路由键或模式。
     *      *（星号）：可以（只能）匹配一个单词
     *       #（井号）：可以匹配多个单词（或者零个）
     */
    public final static String KEY_V1 = "add_route.momo";
    public final static String KEY_V2 = "modify_route.momo";
}
