package com.hdw.common.starter.activemq.service;

import com.hdw.common.core.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.RemoveInfo;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;

/**
 * @author JacksonTu
 * @version 1.0
 * @description topic模式的消费者
 * @since 2020/11/11 16:13
 */
@Slf4j
@Component
public class ActivemqTopicConsumerListener {

    /**
     * topic模式的消费者
     *
     * @param message
     */
    @JmsListener(destination = "testTopic", containerFactory = "topicListener")
    public void readActiveQueue(String message) {
        System.out.println("topic接受到：" + message);
    }

    @JmsListener(destination = "ActiveMQ.Advisory.Connection", containerFactory = "topicListener", concurrency = "10")
    public void advisoryConnection(Message msg) {
        try {
            if (msg instanceof ActiveMQMessage) {
                ActiveMQMessage aMsg = (ActiveMQMessage) msg;
                if (aMsg.getDataStructure() instanceof ConnectionInfo) {
                    ConnectionInfo connectionInfo = (ConnectionInfo) aMsg.getDataStructure();
                    log.info("连接信息：" + JacksonUtil.toJson(connectionInfo));
                } else if (aMsg.getDataStructure() instanceof RemoveInfo) {
                    RemoveInfo removeInfo = (RemoveInfo) aMsg.getDataStructure();
                    log.info("移除信息：" + JacksonUtil.toJson(removeInfo));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
