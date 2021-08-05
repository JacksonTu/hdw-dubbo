package com.hdw.common.starter.activemq.service;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.jms.core.JmsTemplate;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * @Description ActiveMQ 生产者服务类
 * @Author JacksonTu
 * @Date 2018/5/23 16:01
 */
public class ActivemqSendMsgService {

    @Resource
    private JmsTemplate jmsTemplate;

    /**
     * 生产者/消费者模式
     * 发送消息
     *
     * @param queueName 队列名称
     * @param obj       消息
     */
    public void sendMessage(String queueName, Object obj) {
        Queue queue = new ActiveMQQueue(queueName);
        jmsTemplate.convertAndSend(queue, obj);
    }

    /**
     * 订阅模式
     * 发送消息
     *
     * @param topicName topic名称
     * @param obj       消息
     */
    public void sendTopic(String topicName, Object obj) {
        Topic topic = new ActiveMQTopic(topicName);
        jmsTemplate.convertAndSend(topic, obj);
    }
}
