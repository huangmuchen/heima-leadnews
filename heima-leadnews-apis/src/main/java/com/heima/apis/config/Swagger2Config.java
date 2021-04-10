package com.heima.apis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author: HuangMuChen
 * @date: 2019/11/9 19:29
 * @version: V1.0
 * @Description: Swagger配置类
 */
@Configuration // 让Spring来加载该类配置
@EnableSwagger2 // 开启Swagger2的支持
public class Swagger2Config {

    /**
     * 创建API应用
     *    1. apiInfo() 增加API相关信息
     *    2. 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现，
     *    3. 采用指定扫描的包路径来指定要建立API的目录。
     *
     * @return
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.heima"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 创建该API的基本信息（这些基本信息会展现在文档页面中）
     * 访问地址：http://项目实际地址/swagger-ui.html
     *
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("黑马头条api文档")
                .description("既可以查看接口功能，还提供调试测试功能")
                .contact(new Contact("huangmuchen", "https://blog.csdn.net/weixin_43473380/", "13970802980@139.com"))
                .version("1.0")
                .build();
    }
}