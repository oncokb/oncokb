package org.mskcc.cbio.oncokb.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mskcc.cbio.oncokb.config.annotation.DefaultApi;
import org.mskcc.cbio.oncokb.config.annotation.V1Api;
import org.mskcc.cbio.oncokb.config.annotation.V2Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Date;
import java.util.List;

@Configuration
@ComponentScan(basePackages = "org.mskcc.cbio.oncokb.api.pub")
@EnableWebMvc
@EnableSwagger2
public class MvcConfigurationPublic extends MvcConfiguration {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        super.addViewControllers(registry);
        registry.addViewController("/api").setViewName("redirect:/api/swagger-ui.html");
        registry.addViewController("/api/private").setViewName("redirect:/api/private/swagger-ui.html");

        registry.addViewController("/legacy").setViewName("redirect:/legacy-api/swagger-ui.html");
        registry.addViewController("/legacy/").setViewName("redirect:/legacy-api/swagger-ui.html");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonConverter());
        super.configureMessageConverters(converters);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(Date.class, new DateSerializer());
        javaTimeModule.addDeserializer(Date.class, new DateDeserializer());
        mapper.registerModule(javaTimeModule);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter jacksonConverter = new
            MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(objectMapper());
        return jacksonConverter;
    }

    @Bean
    public Docket defaultApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(DefaultApi.class))
            .build()
            .apiInfo(apiInfo("v2.0"));
    }

    @Bean
    public Docket v1Api() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("v1")
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(V1Api.class))
            .build()
            .apiInfo(apiInfo("v1.0"))
            .useDefaultResponseMessages(false);
    }

    @Bean
    public Docket v2Api() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("v2")
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(V2Api.class))
            .build()
            .apiInfo(apiInfo("v2.0"))
            .useDefaultResponseMessages(false);
    }
}
