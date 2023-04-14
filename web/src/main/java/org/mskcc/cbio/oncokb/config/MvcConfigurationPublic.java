package org.mskcc.cbio.oncokb.config;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
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


import static org.mskcc.cbio.oncokb.Constants.*;

@Configuration
@ComponentScan(basePackages = {"org.mskcc.cbio.oncokb.api.pub.v1", "org.mskcc.cbio.oncokb.cache", "org.mskcc.cbio.oncokb.bo"})
@EnableWebMvc
@EnableSwagger2
public class MvcConfigurationPublic extends WebMvcConfigurerAdapter{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/api/v1/swagger-ui.html");
        registry.addViewController("/api").setViewName("redirect:/api/v1/swagger-ui.html");
        registry.addViewController("/api/").setViewName("redirect:/api/v1/swagger-ui.html");
        registry.addViewController("/api/v1/").setViewName("redirect:/api/v1/swagger-ui.html");
        registry.addViewController("/api/v1").setViewName("redirect:/api/v1/swagger-ui.html");
    }

    @Bean
    public Docket publicApi() {
        String swaggerDescription = PropertiesUtils.getProperties(SWAGGER_DESCRIPTION);
        String finalDescription = StringUtils.isNullOrEmpty(swaggerDescription) ? SWAGGER_DEFAULT_DESCRIPTION : swaggerDescription;
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("Public APIs")
            .select()
            .apis(RequestHandlerSelectors.withMethodAnnotation(PublicApi.class))
            .build()
            .apiInfo(new ApiInfo(
                "OncoKB APIs",
                finalDescription,
                PUBLIC_API_VERSION,
                "https://www.oncokb.org/terms",
                new Contact("OncoKB", "https://www.oncokb.org", "contact@oncokb.org"),
                "Terms of Use",
                "https://www.oncokb.org/terms"
            ))
            .useDefaultResponseMessages(false);
    }
}
