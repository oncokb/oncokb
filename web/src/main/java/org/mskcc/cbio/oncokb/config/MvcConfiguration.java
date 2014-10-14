package org.mskcc.cbio.oncokb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
		resolver.setPrefix("/app/");
		resolver.setSuffix(".html");
		return resolver;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/app/**").addResourceLocations("/app/");
		registry.addResourceHandler("/components/**").addResourceLocations("/app/components/");
		registry.addResourceHandler("/images/**").addResourceLocations("/app/images/");
		registry.addResourceHandler("/scripts/**").addResourceLocations("/app/scripts/");
		registry.addResourceHandler("/styles/**").addResourceLocations("/app/styles/");
		registry.addResourceHandler("/views/**").addResourceLocations("/app/views/");
		registry.addResourceHandler("/data/**").addResourceLocations("/app/data/");
	}

	
}
