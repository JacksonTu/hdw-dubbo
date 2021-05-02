package com.hdw.common.starter.activemq.configure;

import com.hdw.common.starter.activemq.service.ActivemqQueueConsumerListener;
import com.hdw.common.starter.activemq.service.ActivemqSendMsgService;
import com.hdw.common.starter.activemq.service.ActivemqTopicConsumerListener;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;


/**
 * @Description ActiveMQ配置
 * @Author JacksonTu
 * @Date 2018/5/28 15:05
 */
@Configuration
@EnableJms
public class ActivemqConfigure {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.password}")
    private String password;

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        activeMQConnectionFactory.setUserName(username);
        activeMQConnectionFactory.setPassword(password);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        cachingConnectionFactory.setSessionCacheSize(100);
        return cachingConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        /**
         * 进行持久化配置 1表示非持久化，2表示持久化
         */
        jmsTemplate.setDeliveryMode(2);
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setConnectionFactory(cachingConnectionFactory());
        return jmsTemplate;
    }


    /**
     * 在Queue模式中，对消息的监听需要对containerFactory进行配置
     *
     * @param cachingConnectionFactory
     * @return
     */
    @Bean("queueListener")
    public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(CachingConnectionFactory cachingConnectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(cachingConnectionFactory);
        factory.setPubSubDomain(false);
        return factory;
    }

    /**
     * 在Topic模式中，对消息的监听需要对containerFactory进行配置
     *
     * @param cachingConnectionFactory
     * @return
     */
    @Bean("topicListener")
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(CachingConnectionFactory cachingConnectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(cachingConnectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

    @Bean
    @ConditionalOnBean(name = "jmsTemplate")
    public ActivemqSendMsgService activemqSendMsgService() {
        return new ActivemqSendMsgService();
    }

    @Bean
    @ConditionalOnBean(name = "jmsTemplate")
    public ActivemqTopicConsumerListener activemqTopicConsumerListener() {
        return new ActivemqTopicConsumerListener();
    }

    @Bean
    @ConditionalOnBean(name = "jmsTemplate")
    public ActivemqQueueConsumerListener activemqQueueConsumerListener() {
        return new ActivemqQueueConsumerListener();
    }
}
