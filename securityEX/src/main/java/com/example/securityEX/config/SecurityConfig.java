package com.example.securityEX.config;

import com.example.securityEX.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 필터가 스프링 필터체인에 등록됨.
//@EnableMethodSecurity(securedEnabled = true) // secured 어노테이션 활성화
@EnableMethodSecurity(prePostEnabled = true) // preAuthorize, postAuthorize 어노테이션 활성화
public class SecurityConfig {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService; // -> 후처리 해줘야함 (Attribute정보 받았으니 User 만들어서 넣기)

    // SecurityFilterChain 을 빈으로 등록해야함.
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        // user로 들어오는 요청은 인증이 필요
                        .requestMatchers(new AntPathRequestMatcher("/user/**")).authenticated()
                        // manager로 들어오는 요청은 admin, manager 권한이 필요
                        .requestMatchers(new AntPathRequestMatcher("/manager/**")).access(hasRole("ADMIN or MANAGER"))
                        // admin으로 들어오는 요청은 admin 권한이 필요
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).access(hasRole("ADMIN"))
                        // 그 외의 요청은 모두 허용
                        .anyRequest().permitAll())
                // 로그인 설정
                // 인증이 필요한 페이지로 갈때는, 로그인 페이지로 이동할 때는 /loginForm으로 이동
                .formLogin((formLogin) -> formLogin.loginPage("/loginForm")
                        // /login 이 호출되면 시큐리티가 대신 로그인 진행 => 컨트롤러에 /login이 있으면 안됨.
                        .loginProcessingUrl("/login")
                        // 만약 받아오는 username의 이름이 바뀌면 여기서 설정해줘야함
                        //.usernameParameter("username")
                        // 내가 로그인 페이지로 들어와서 로그인을 진행할 때, 로그인이 성공하면 "/"로 이동
                        // 다른곳에서 자동으로 로그인페이지로 넘어왔다?(인증이 필요한 페이지로 갔다) => 로그인 성공하면 다시 원래 페이지로 이동
                        .defaultSuccessUrl("/"))
                .oauth2Login((ologin) -> ologin.loginPage("/loginForm")
                // 소셜 로그인 완료후에 후처리 코드 1.코드받기(인증) 2.엑세스토큰(권한 사용자 정보 접근)  3.사용자 프로필정보 가져오기 4. 그 정보를 토대로 회원가입 자동화
                // 아니면 추가적인 회원가입 창을 띄워서 추가 정보를 받아와야함.
                // 그런데 구글 로그인이 완료 되면 => (엑세스토큰 + 사용자 프로필정보를 바로 받아줌) 매우 유용하다.
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(principalOauth2UserService)));


        return http.build();

    }

    // 비밀번호 암호화
    // 해당 메소드의 리턴되는 오브젝트를 IoC로 등록해줌. => @Autowired로 주입 가능.
    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }
}
