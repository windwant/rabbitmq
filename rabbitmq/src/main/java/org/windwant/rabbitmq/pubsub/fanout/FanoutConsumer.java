package org.windwant.rabbitmq.pubsub.fanout;

import com.rabbitmq.client.*;
import com.rabbitmq.client.DefaultConsumer;
import org.windwant.rabbitmq.core.ConnectionMgr;
import org.apache.commons.configuration.ConfigurationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 广播接收 fanout
 * Created by windeant on 2016/8/15.
 */
public class FanoutConsumer {
    private DefaultConsumer consumer;
    private final String EXCHANGE_NAME = "exchange_fanout";
    private final String ROUTE_KEY = "";
    private Connection connection = null;
    public void run(){
        try {
            connection = ConnectionMgr.getConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);//fanoout模式

            String queueName = channel.queueDeclare().getQueue();//随机queue
            channel.queueBind(queueName, EXCHANGE_NAME, ROUTE_KEY);//需要绑定 routekey "" 接收所有消息 fanout模式下自动忽略
            consumer = new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(Thread.currentThread().getName() + " Received envelope: " + envelope.toString() + ", msg: " + message);
                    channel.basicAck(envelope.getDeliveryTag(),false);
                }
            };
            //autoAck false
            channel.basicConsume(queueName, false, consumer);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FanoutConsumer().run();
    }
}
