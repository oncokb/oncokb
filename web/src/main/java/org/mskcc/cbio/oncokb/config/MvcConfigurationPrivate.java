package org.mskcc.cbio.oncokb.config;

import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.mskcc.oncokb.meta.enumeration.RedisType;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mskcc.cbio.oncokb.Constants.PRIVATE_API_VERSION;

@Configuration
@ComponentScan(basePackages = "org.mskcc.cbio.oncokb.api.pvt")
@EnableWebMvc
@EnableSwagger2
public class MvcConfigurationPrivate extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/api/private").setViewName("redirect:/api/private/swagger-ui.html");
        registry.addViewController("/api/private/").setViewName("redirect:/api/private/swagger-ui.html");
    }




    @Bean
    public RedissonClient redissonClient()
        throws Exception {
        Config config = new Config();
        String redisType= PropertiesUtils.getProperties("redis.type");
        String redisPassword= PropertiesUtils.getProperties("redis.password");
        String redisAddress= PropertiesUtils.getProperties("redis.address");

        if (redisType.equals(RedisType.SINGLE.getType())) {
            config
                .useSingleServer()
                .setAddress(redisAddress)
                .setPassword(redisPassword);
        } else if (redisType.equals(RedisType.SENTINEL.getType())) {
            config
                .useSentinelServers()
                .setMasterName("oncokb-master")
                .setCheckSentinelsList(false)
                .addSentinelAddress(redisAddress)
                .setPassword(redisPassword);
        } else {
            throw new Exception(
                "The redis type " +
                    redisType +
                    " is not supported. Only single and sentinel are supported."
            );
        }
        return Redisson.create(config);
    }

    @Bean
    CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<String, CacheConfig>();
        return new RedissonSpringCacheManager(redissonClient, config);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("org.mskcc.cbio.oncokb.api.pvt"))
            .build()
            .apiInfo(new ApiInfo(
                "OncoKB APIs",
                "OncoKB, a comprehensive and curated precision oncology knowledge base, offers oncologists detailed, evidence-based information about individual somatic mutations and structural alterations present in patient tumors with the goal of supporting optimal treatment decisions.",
                PRIVATE_API_VERSION,
                "https://www.oncokb.org/terms",
                new Contact("OncoKB", "https://www.oncokb.org", "contact@oncokb.org"),
                "Terms of Use",
                "https://www.oncokb.org/terms"
            ));
    }
}
