package com.hdw.common.starter.activemq.service;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author JacksonTu
 * @version 1.0
 * @description queue模式的消费者
 * @since 2020/11/11 16:11
 */
@Component
public class ActivemqQueueConsumerListener {

    /**
     * queue模式的消费者
     *
     * @param message
     */
    @JmsListener(destination = "testQueue", containerFactory = "queueListener", concurrency = "10")
    public void readActiveQueue(String message) {
        System.out.println("queue接受到：" + message);
    }
}
