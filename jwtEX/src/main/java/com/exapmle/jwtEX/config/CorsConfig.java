package com.exapmle.jwtEX.config;

import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    // 스프링 필터가 들고있는 cors 필터
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 내 서버가 응답을 할때, json을 자바스크립트에서 처리할 수 있게 할지를 설정하는 것
                                            // False 로 하면 js 와 통신 불가...
        config.addAllowedOrigin("*"); // 모든 ip의 응답을 허용
        config.addAllowedHeader("*"); // 모든 헤더의 응답 허용
        config.addAllowedMethod("*"); // 모든 post, get, put, delete, patch 요청을 허용
        source.registerCorsConfiguration("/api/**", config);
        // api/**로 들어오는 모든 요청은 이 config를 따르게 된다.
        return new CorsFilter(source);
        // 이렇게 하고 필터에 등록이 필요하다. -> SecurityConfig.java 에다가 등록.
    }

}
