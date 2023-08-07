package com.exapmle.jwtEX.config;

import com.exapmle.jwtEX.filter.MyFilter1;
import com.exapmle.jwtEX.filter.MyFilter2;
import jakarta.servlet.FilterRegistration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class FilterConfig {
    // 시큐리티 필터체인이 기본적으로 내가 만든거보다 먼저 실행.
    // 내가 만든 필터가 스프링꺼보다 먼저 동작하도록 만들고 싶다? -> http.addFilterBefore(내필터, 이거보다 먼저하고싶다 하는 필터)
    @Bean
    public FilterRegistrationBean<MyFilter1> filter1(){
        FilterRegistrationBean<MyFilter1> bean = new FilterRegistrationBean<>(new MyFilter1());
        bean.addUrlPatterns("/*"); // 모든 요청에 대해서 필터를 거치게 한다.
        bean.setOrder(0); // 낮은 번호가 필터중에서 가장 먼저 실행된다.
        return bean;
    }
}
