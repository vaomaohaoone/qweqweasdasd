package ru.mephi.cloud.ml.deploying.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.mephi.cloud.ml.deploying.filter.JwtFilter;

@EnableAutoConfiguration
@ComponentScan
@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns("/user/api/*");
        registrationBean.addUrlPatterns("/user/exp_time");
        return registrationBean;
    }

}