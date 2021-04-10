package com.heima.admin.test;

import com.heima.AdminApplication;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.mappers.admin.AdChannelMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = AdminApplication.class)
@RunWith(SpringRunner.class)
public class KafkaSendTest {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void sendMessageTest() {
        try {
            this.kafkaTemplate.send("muchen.topic.test.sigle", "muchen_key", "muchen_value");
            System.out.println("=================消息发送了=================");
            Thread.sleep(500000);// 休眠等待消费者接收消息
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
