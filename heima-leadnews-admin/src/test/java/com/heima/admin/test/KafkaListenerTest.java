package com.heima.admin.test;

import com.heima.common.kafka.KafkaListener;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerTest implements KafkaListener {

    /**
     * 指定消息主题
     *
     * @return
     */
    @Override
    public String topic() {
        return "muchen.topic.test.sigle";
    }

    /**
     * 处理监听到的消息
     *
     * @param data
     * @param consumer
     */
    @Override
    public void onMessage(ConsumerRecord data, Consumer consumer) {
        System.out.println("接收测试消息：" + data);
    }

    @Override
    public void onMessage(Object o) {

    }
}
