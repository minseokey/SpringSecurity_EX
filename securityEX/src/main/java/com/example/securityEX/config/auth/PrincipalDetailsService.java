package com.example.securityEX.config.auth;

import com.example.securityEX.model.User;
import com.example.securityEX.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// SecurityConfig 에서 loginProcessingUrl("/login");
// /login 요청이 오면 자동으로 UserDetailsService 타입으로 IoC 되어있는 loadUserByUsername 함수가 실행
// PrincipalDetailsService 빈 등록

@Service
public class PrincipalDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    // 받을때 "username" 으로 안받으면 안됨 => 다른이름으로 받으려면 config 수정 필요

    // 시큐리티 session(내부 Authentication(내부 UserDetails)) 형태로 로그인한 유저 저장.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("username: " + username);
        User userEntity = userRepository.findByUsername(username);

        // 로그인시에 저장된 유저가 있다면 (회원가입이 되어 있다면)
        if(userEntity != null){
            //PrincipalDetails 를 리턴해야함 (UserDetail 타입).
            return new PrincipalDetails(userEntity);
        }
        return null;
    }
}
