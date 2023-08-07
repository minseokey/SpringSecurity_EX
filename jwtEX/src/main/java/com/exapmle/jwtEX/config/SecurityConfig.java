package com.exapmle.jwtEX.config;

import com.exapmle.jwtEX.config.jwt.JwtAuthenticationFilter;
import com.exapmle.jwtEX.config.jwt.JwtAuthorizationFilter;
import com.exapmle.jwtEX.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{

    private final CorsFilter corsFilter; // corsFilter di 받아오기
    private final UserRepository userRepository;

    @Bean
    public BCryptPasswordEncoder encodePwd(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //- 세션을 사용하지 않겠다. 기본적으로 세션은 쿠키를 사용하는데 이거 사용 X.
                .addFilter(corsFilter) // 이렇게 하면 내 서버는 cors에서 벗어날 수 있다. (외부 요청이 와도 다 받아준다)
                //1. 컨트롤러에 @CrossOrigin(인증X), 2. 시큐리티 필터에 등록 (인증O)
                // -->  이렇게 하면 시큐리티를 사용하고있지만 세션을 사용x, 모든 페이지에 접근이 가능해진다.(stateless 서버로 사용, CrossOrigin 요청에서 벗어남, FormLogin 사용x);
                .formLogin(AbstractHttpConfigurer::disable) // 이렇게 하면 기존의 로그인 방법을 사용하지 않겠다는 의미이다.
                 //  로그인을 끄는대신 이 필터를 등록해준다. AuthenticationManager 를 만들어서 인자로 전달해준다.
                .addFilter(new JwtAuthenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class))))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)),userRepository))
                .httpBasic(AbstractHttpConfigurer::disable) // 아래서 설명하는 기본 보안 방식.
                // 세션 -> 서버에서 만들어서 세션 디비에 저장하고 클라에 뿌려줌. 그러면 클라는 그걸 쿠키영역에 저장. 그걸 요청때마다 들고옴.
                // 근데 쿠키는 동일 도메인에서만 발동(Same Origin Policy) -> 강제로 담을 순 있지만 대부분 쿠키를 http only 를 설정하여 서버쪽에서 거부.
                // 그래서 사용하는게 httpbasic -> 헤더에 아이디 비밀번호를 담아가는 방식. -> 클라의 쿠키, 서버의 세션디비 필요 x. 근데 이것도 보안에 취약하다. -> Basic 방식
                // https 를 사용하면 암호화되어 날아감 -> 해결. 노출되지 않는다.
                // 근데 jwt 방식은? authorization 영역에 토큰을 넣어서 날아간다. -> Bearer 방식. 노출이 되도 토큰이 바뀌면 다시 암호화 (유효시간 o). -> jwt 방식
                // 그래도 토큰이 노출되면 보안상 위험. 토큰들고 악의적으로 로그인이 가능하기때문
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers(new AntPathRequestMatcher("/user/**")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/manager/**")).access(hasRole("ADMIN or MANAGER"))
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).access(hasRole("ADMIN"))
                        .anyRequest().permitAll());
        return http.build();
    }
}
