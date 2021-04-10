package com.heima.admin.test;

import com.heima.AdminApplication;
import com.heima.common.aliyun.AliyunImageScanRequest;
import com.heima.common.aliyun.AliyunTextScanRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: HuangMuChen
 * @date: 2021/2/20 10:12
 * @version: V1.0
 * @Description: 文本/图片审核测试
 */
@SpringBootTest(classes = AdminApplication.class)
@RunWith(SpringRunner.class)
public class AliyunScanRequestTest {
    @Autowired
    private AliyunTextScanRequest aliyunTextScanRequest;
    @Autowired
    private AliyunImageScanRequest aliyunImageScanRequest;

    @Test
    public void testTextScanRequest() throws Exception {
        try {
            String message = "阿里云，阿里巴巴集团旗下云计算品牌，全球卓越的云计算技术和服务提供商。创立于2009年，在杭州、北京、硅谷等地设有研发中心和运营机构。";
            String response = this.aliyunTextScanRequest.textScanRequest(message);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testImageScanRequest() {
        try {
            List<String> list = new ArrayList<>();
            list.add("https://www.xsimg.xyz/i/2021/02/18/hct1ht.jpg");
            String response = this.aliyunImageScanRequest.imageScanRequest(list);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
