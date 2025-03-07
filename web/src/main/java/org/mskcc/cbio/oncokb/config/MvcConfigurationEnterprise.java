package org.mskcc.cbio.oncokb.config;

import com.monitorjbl.json.JsonViewSupportFactoryBean;
import org.apache.commons.lang3.StringUtils;
import io.sentry.spring.SentryExceptionResolver;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.config.annotation.PublicApi;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import static org.mskcc.cbio.oncokb.Constants.*;

@Configuration
@ComponentScan(basePackages = {"org.mskcc.cbio.oncokb.api.pub.v1", "org.mskcc.cbio.oncokb.api.pvt", "org.mskcc.cbio.oncokb.controller", "org.mskcc.cbio.oncokb.cache", "org.mskcc.cbio.oncokb.bo"})
@EnableWebMvc
@EnableSwagger2
public class MvcConfigurationEnterprise extends WebMvcConfigurerAdapter {

    @Bean
    public ViewResolver getViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/");
        resolver.setSuffix(".html");
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("/");
        registry.addResourceHandler("/app/**").addResourceLocations("/app/");
        registry.addResourceHandler("/components/**").addResourceLocations("/components/");
        registry.addResourceHandler("/images/**").addResourceLocations("/images/");
        registry.addResourceHandler("/scripts/**").addResourceLocations("/scripts/");
        registry.addResourceHandler("/styles/**").addResourceLocations("/styles/");
        registry.addResourceHandler("/views/**").addResourceLocations("/views/");
        registry.addResourceHandler("/data/**").addResourceLocations("/data/");


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

        registry.addViewController("/api/private").setViewName("redirect:/api/private/swagger-ui.html");
        registry.addViewController("/api/private/").setViewName("redirect:/api/private/swagger-ui.html");
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("utf-8");
        multipartResolver.setMaxUploadSize(50000000);
        return multipartResolver;
    }

    @Bean
    public JsonViewSupportFactoryBean views() {
        return new JsonViewSupportFactoryBean();
    }


    @Bean
    public ServletContextInitializer sentryServletContextInitializer() {
        return new io.sentry.spring.SentryServletContextInitializer();
    }

    private ApiInfo getDefaultApiInfo(String title, String description, String apiVersion) {
        return new ApiInfo(
            Optional.ofNullable(title).orElse("OncoKB APIs"),
            Optional.ofNullable(description).orElse(""),
            Optional.ofNullable(apiVersion).orElse("v0.0.1"),
            "https://www.oncokb.org/terms",
            new Contact("OncoKB", "https://www.oncokb.org", "contact@oncokb.org"),
            "Terms of Use",
            "https://www.oncokb.org/terms"
        );
    }

    private void updateDocketHost(Docket docket, ServletContext servletContext, String basePath) {
        // Update swagger host/protocal with environment variable
        String swaggerUrl = PropertiesUtils.getProperties(SWAGGER_URL);
        if (StringUtils.isNotEmpty(swaggerUrl)) {
            URL url = null;
            try {
                url = new URL(swaggerUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (url != null) {
                docket
                    .pathProvider(new RelativePathProvider(servletContext) {
                        @Override
                        public String getApplicationBasePath() {
                            return basePath;
                        }
                    })
                    .protocols(Collections.singleton(Optional.ofNullable(url.getProtocol()).orElse("http")))
                    .host(url.getHost());
            }
        }
    }

    private String getSwaggerDescription() {
        String swaggerDescription = PropertiesUtils.getProperties(SWAGGER_DESCRIPTION);
        return StringUtils.isEmpty(swaggerDescription) ? SWAGGER_DEFAULT_DESCRIPTION : swaggerDescription;
    }


    @Bean
    public Docket publicApi(ServletContext servletContext) {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .groupName("Public APIs")
            .select()
            .apis(RequestHandlerSelectors.withMethodAnnotation(PublicApi.class))
            .build()
            .apiInfo(getDefaultApiInfo("OncoKB APIs", getSwaggerDescription(), PUBLIC_API_VERSION))
            .useDefaultResponseMessages(false);
        updateDocketHost(docket, servletContext, "/api/v1");
        return docket;
    }

    @Bean
    public Docket premiumPublicApi(ServletContext servletContext) {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .groupName("Private APIs")
            .select()
            .apis(RequestHandlerSelectors.withMethodAnnotation(PremiumPublicApi.class))
            .build()
            .apiInfo(getDefaultApiInfo("OncoKB Private APIs", "These endpoints are for private use only.", PUBLIC_API_VERSION))
            .useDefaultResponseMessages(false);
        updateDocketHost(docket, servletContext, "/api/v1");
        return docket;
    }

    @Bean
    public Docket privateApi(ServletContext servletContext) {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("org.mskcc.cbio.oncokb.api.pvt"))
            .build()
            .apiInfo(getDefaultApiInfo("OncoKB APIs", getSwaggerDescription(), PRIVATE_API_VERSION));
        updateDocketHost(docket, servletContext, "/api/private");
        return docket;
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
