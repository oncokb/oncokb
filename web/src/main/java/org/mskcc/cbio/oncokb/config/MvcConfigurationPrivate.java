package org.mskcc.cbio.oncokb.config;

import io.sentry.spring.SentryExceptionResolver;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mskcc.cbio.oncokb.Constants.PRIVATE_API_VERSION;

@Configuration
@ComponentScan(basePackages = {"org.mskcc.cbio.oncokb.api.pvt", "org.mskcc.cbio.oncokb.cache", "org.mskcc.cbio.oncokb.bo"})
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
//        registry.addViewController("/api/private").setViewName("redirect:/api/private/swagger-ui.html");
//        registry.addViewController("/api/private/").setViewName("redirect:/api/private/swagger-ui.html");
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

    @Bean
    public ServletContextInitializer sentryServletContextInitializer() {
        return new io.sentry.spring.SentryServletContextInitializer();
    }

    @Bean
    public HandlerExceptionResolver sentryExceptionResolver() {
        // Exclude specific events https://stackoverflow.com/questions/48914391/avoid-reporting-broken-pipe-errors-to-sentry-in-a-spring-boot-application
        return new SentryExceptionResolver() {
            @Override
            public ModelAndView resolveException(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object handler,
                                                 Exception ex) {
                Throwable rootCause = ex;

                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }

                if (rootCause.getMessage() == null || (!rootCause.getMessage().contains("Broken pipe")
                    && !rootCause.getMessage().contains("ClientAbortException")
                    && !rootCause.getMessage().contains("Connection reset by peer")
                    && !rootCause.getMessage().contains("Cannot deserialize value")
                    && !rootCause.getMessage().contains("Required request body content is missing")
                    && !rootCause.getMessage().contains("Required request body is missing")
                    && !rootCause.getMessage().contains("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'")
                    && !rootCause.getMessage().contains("Required String parameter "))
                ) {
                    super.resolveException(request, response, handler, ex);
                }
                return null;
            }

        };
    }
}
