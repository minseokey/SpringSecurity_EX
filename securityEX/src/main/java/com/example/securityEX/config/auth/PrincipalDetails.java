package com.example.securityEX.config.auth;

// 시큐리티가 /login을 잡아서 로그인 진행.
// 로그인 진행 완료시 시큐리티 session에 넣어준다. (Security ContextHolder) 이 키값에 세션정보 저장.
// 들어갈 수 있는 오브젝트 타입 정해져있음 => Authentication 타입 객체
// Authentication 안에 User 정보가 있어야 함.
// User 오브젝트 타입 => UserDetails 타입 객체

import com.example.securityEX.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

// Security Session ==> Authentication ==> UserDetails(PricipalDetails 과 같은 타입 )
@Data
public class PrincipalDetails implements UserDetails, OAuth2User { // 이렇게만 해주면 Authentication 에 들어가는거 통일 가능.

    private User user; // 콤포지션
    private Map<String, Object> attributes;

    // 생성자
    // 1. => 일반 로그인
    public PrincipalDetails(User user){
        this.user = user;
    }

    // 2. => OAuth 로그인
    // 오버로딩을 활용한다.
    public PrincipalDetails(User user, Map<String, Object> attributes){
        this.user = user; // 이 유저는 attributes 를 기반으로 만들어서 넣어줄거다.
        this.attributes = attributes;
    }

    @Override //OAuth2User
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // 해당 유저의 권한 리턴
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 리턴 타입을 맞춰주어야함.
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add((GrantedAuthority) () -> user.getRole());
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }


    @Override
    // 계정 만료 여부
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    // 계정 잠김 여부
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    // 비밀번호 만료 여부
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    // 계정 활성화 여부
    public boolean isEnabled() {
        // 여기서 휴면계정 설정등 가능
        return true;
    }

    @Override  //OAuth2User
    public String getName() {
        return attributes.get("sub").toString();
    }
}
