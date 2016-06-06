package org.mskcc.cbio.oncokb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@ComponentScan(basePackages="org.mskcc.cbio.oncokb")
@EnableWebMvc
public class MvcConfiguration extends WebMvcConfigurerAdapter{

	@Bean
	public ViewResolver getViewResolver(){
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
	}

	@Bean
        public CommonsMultipartResolver multipartResolver(){
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
            multipartResolver.setDefaultEncoding("utf-8");
            multipartResolver.setMaxUploadSize(50000000);
            return multipartResolver;
        }
}
