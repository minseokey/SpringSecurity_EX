package com.exapmle.jwtEX.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.exapmle.jwtEX.config.auth.PrincipalDetails;
import com.exapmle.jwtEX.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;

// 스프링 시큐리티에 UsernamePasswordAuthenticationFilter 가 있음
// 원래는 내가 /login 요청을 해서 username, pw 를 전송하면(post) 이게 동작.
// 근데 우리가 formlogin 꺼버림. --> 이 필터를 다시 시큐리티컨피그에 등록을 해주면 된다. addfilter.

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

    private final AuthenticationManager authenticationManager;

    // /login 요청을 하면 로그인 시도를 위해서 실행되는 함수. -> 이제 /login 으로 요청이 들어오면 attemptAuthentication 함수가 실행됨.
    // 1. username, password 받아서
    // 2. 정상인지 로그인 시도를 해봄.
    // authenticationManager 로 로그인 시도를 하면 PrincipalDetailsService 가 호출되고 loadUserByUsername() 함수가 실행됨.
    // 3. PrincipalDetails 를 세션에 담고 (여기서 세션은 스프링 시큐리티의 세션이다) (권한 관리를 위해) -> 이거 직접할 자신있으면 세션에 담을 필요 X
    // 4. JWT 토큰을 만들어서 응답해주면 됨.

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse rep) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter : 로그인 시도중");
        System.out.println(authenticationManager);
        try {
            // 1. username, password 받아서 json -> User 파싱해서 받자.
            ObjectMapper om = new ObjectMapper(); // 이렇게하면 User 오브젝트에 담을 수 있다.
            User user = om.readValue(req.getInputStream(), User.class); // 여기 담긴 User 객체로 로그인 시도.
            System.out.println(user);

            UsernamePasswordAuthenticationToken authenticationToken = // 토큰 만들기 (Username과 Password를 담아서)
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

            // 2. 정상인지 로그인 시도를 해봄 (DB 와 들어온값이 일치).
            Authentication authentication = // 로그인 시도를 하면 authenticationManager 가 PrincipalDetailsService 의 loadUserByUsername() 함수를 호출하고
                    authenticationManager.authenticate(authenticationToken);
            // 인증이 되면 PrincipalDetails 를 리턴받는다. -> authentication 객체가 만들어진다.
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal(); // 다운캐스팅

            System.out.println(principalDetails.getUser().getUsername());
            System.out.println("===========================================");
            // 3. authentication 객체가 session 영역에 저장됨 -> 로그인이 되었다는 뜻. (id,pw 정확하다는것)
            // 리턴의 이유는 단지 권한 관리를 위해서. 굳이 JWT 토큰을 사용하면서 세션을 만들 이유가 없음. (stateless 서버이기 때문에)
            return authentication;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
        // attemptAuthentication 함수가 정상적으로 실행되면 successfulAuthentication 함수가 실행된다.
        // 여기서 jwt 만들어서 req 요청한 사용자에게 보내면 된다.
        @Override
        protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse rep, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
            System.out.println("successfulAuthentication 실행됨 : 인증이 완료되었다는 뜻임.");
            PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
            // RSA 방식은 아니고 Hash 암호방식 (HS256) -> 서버만 알고있는 key 값이 필요하다.
            String jwtToken = JWT.create() // 기본적으로 빌더패턴을 따른다.
                    .withSubject("토큰제목")
                    .withExpiresAt(new java.util.Date(System.currentTimeMillis() + (60000 * 10))) // 10분 (만료시간)
                    .withClaim("id", principalDetails.getUser().getId()) // 페이로드 (비공개 클레임)
                    .withClaim("username", principalDetails.getUser().getUsername())
                    .sign(Algorithm.HMAC512("minseokey")); // 서버만 아는 고유값.

            rep.addHeader("Authorization", "Bearer " + jwtToken); // 헤더에 토큰을 넣어서 응답해준다.
        }
}

