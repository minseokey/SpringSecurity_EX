package com.exapmle.jwtEX.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MyFilter1 implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)request; // 다운캐스팅
        HttpServletResponse res = (HttpServletResponse)response; // 다운캐스팅

        // 토큰을 만들어줘야함. id, pw 정상적으로 들어와서 로그인이 되면 토큰을 만들어주고, 그걸 응답해준다.
        // 요청이 올때마다 Authorization이라는 헤더에 value값으로 토큰을 가지고 온다.
        // 그럼 넘어오는 토큰이 내가 뿌린게 맞는지 검증해주면 된다. => (RSA,HS256 을 통해서)
        // RSA => private, public key, HS256 => secret key

        if(req.getMethod().equals("POST")){
            System.out.println("POST 요청됨");
            String headerAuth = req.getHeader("Authorization"); // 토큰을 받아서 처리하는 부분
            System.out.println("headerAuth : " + headerAuth);

            if(headerAuth.equals("minseokey")){
                chain.doFilter(req, res); // 토큰이 맞으면 체인에 등록, 다음 필터를 타게 된다.
            }
            else{
                PrintWriter out = res.getWriter();
                out.println("인증안됨"); // 같지 않다면 뱉어낸다.
            }
        }
    }
}
