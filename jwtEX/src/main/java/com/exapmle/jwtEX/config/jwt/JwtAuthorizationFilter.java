package com.exapmle.jwtEX.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.exapmle.jwtEX.config.auth.PrincipalDetails;
import com.exapmle.jwtEX.repository.UserRepository;
import com.exapmle.jwtEX.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


import java.io.IOException;

// 시큐리티가 filter를 가지고 있는데 그 필터중에 BasicAuthenticationFilter라는 것이 있음
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음
// 만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 안탐
// 따라서 권한이나 인증 조정할때는 이 필터를 받아서 조작하는데 좋다.
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository){
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    // 필터 걸기 (필터 내부에 로직 추가)
    // 인증이나 권한이 필요한 주소요청이 있을 때 해당 필터를 타게 됨
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("인증이나 권한이 필요");

        String jwtHeader = req.getHeader("Authorization");
        System.out.println("jwtHeader : " + jwtHeader);

        // 이제 Authorization에 토큰을 담아 보내면 jwtHeader에 담긴다. 이걸 가지고 사용자 검증을 해보자.
        // 토큰이 정상적으로 들어왔는지 확인
        if(jwtHeader == null || !jwtHeader.startsWith("Bearer")){
            // 헤더가 없거나, Bearer가 없으면 다음 필터를 타게 한다.(진행이 더이상 안될거야)
            chain.doFilter(req, res);
            return;
        }
        // 토큰이 있으면 토큰을 검증해서 정상적인 사용자인지 확인
        String jwtToken = req.getHeader("Authorization").replace("Bearer ", ""); // 깔끔한 토큰만 남는다.
        String username = JWT.require(Algorithm.HMAC512("minseokey")) // 설정했던 알고리즘과 키값을 넣어준다.
                .build().verify(jwtToken).getClaim("username").asString(); // 저장할때 넣은 claim 의 username 가져온다, verify 로 서명이 정상적으로 되었는지 테스트한다.

        if(username != null){
            // 레포지토리에서 찾아지면 정상 사용자.
            User userEntity = userRepository.findByUsername(username);

            // 이제 정상적인 사용자인지 확인했으니까 Authentication 객체를 만들어준다.
            PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
            // Authentication 객체를 만들어서 시큐리티 세션에 넣어준다.
            // 결론적으로, JWT 서명을 통해 만드는 객체 (Authentication) 이다.
            Authentication authentication = new UsernamePasswordAuthenticationToken( // 로그인 없이 Authentication 만들어서 세션에 넣어준다.
                    principalDetails,
                    null, // 패스워드는 모르니까 null (username null 아니고, 검색도 되므로 보안문제 x)
                    principalDetails.getAuthorities()); //권한도 알려줘야한다.

            // 강제로 시큐리티에 접근하여 Authentication 객체를 저장한다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 그러고나서 다시 필터 이용
        chain.doFilter(req, res);
    }
}
