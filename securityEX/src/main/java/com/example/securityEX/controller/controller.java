package com.example.securityEX.controller;

import com.example.securityEX.config.auth.PrincipalDetails;
import com.example.securityEX.model.User;
import com.example.securityEX.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

@Controller // view 를 리턴하는 컨트롤러
public class controller {

    @GetMapping("/test/login")
    // DI -> authentication 이 부모가 같기때문에 (userDatail), 외부에서 주입 가능.
    public @ResponseBody String testLogin(Authentication authentication,
                                          @AuthenticationPrincipal PrincipalDetails userDetails){ // 어노테이션으로 세션 정보 접근 가능.
        System.out.println("/test/login ===================");
        // authentication : 로그인 정보를 가지고 있음.
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal(); // 다운캐스팅
        System.out.println("authentication : " + principalDetails.getUser());
        System.out.println("userDetails : " + userDetails.getUser());
        return "세션 정보 확인하기";

        // -> 1. Authentication 으로도 접근 가능. 2. @AuthenticationPrincipal UserDetails 로도 접근 가능.
        // 단 1 방법을 사용시 UserDetails 타입으로 다운캐스팅 해야함.
    }

    @GetMapping("/test/oauth/login")
    public @ResponseBody String testOauthLogin(Authentication authentication,
                                          @AuthenticationPrincipal OAuth2User oauth){ // 어노테이션으로 세션 정보 접근 가능.
        System.out.println("/test/oauth/login ===================");
        // authentication : 로그인 정보를 가지고 있음.
        // Oauth는 PrincipalDetails가 아닌 OAuth2User 타입으로 받아야함. PrincipalOauth2UserService에서 리턴해주는 타입이 OAuth2User 타입이기 때문.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();// 다운캐스팅
        System.out.println("authentication : " + oAuth2User.getAttributes());
        System.out.println("oauthUser : " + oauth.getAttributes());
        return "세션 정보 확인하기";

        // -> 1. Authentication 으로도 접근 가능. 2. @AuthenticationPrincipal OAuth2User 로도 접근 가능.
        // 단 1 방법을 사용시 OAuth2User 타입으로 다운캐스팅 해야함.
    }

    // 결론: 시큐리티는 자기만의 세션을 들고있다. 서버의 세션안에 시큐리티가 관리하는 세션이 있다.
    // 이 세션이 가질 수 있는 타입은 오직 "Authenticaion" 타입만 가능하다.
    // Authentication 안에는 1. UserDetails 타입 2. OAuth2User 타입 두가지가 들어갈 수 있다. (1. 그냥 로그인, 2. 소셜로그인)
    // 이러면 두가지를 각각 처리해야하는데 빡셈 -> PrincipalDetails에 두가지 타입을 다 받을 수 있도록 함. (implementation UserDetails, OAuth2User)
    // PrincipalDetails를 Authentication에 넣어주면 된다.(DI 활용)

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encodePwd;

    @GetMapping({"","/"})
    // mustache 사용 시 기본 폴더 src/main/resources/
    // 뷰리졸버 설정 : templates (prefix, 접두어), .mustache (suffix, 접미어) 생략
    // security 의존성 받으면 모든 출입에 인증이 필요
    public String index(){
        return "index";
    }


    // Oauth, 일반 로그인 둘다 PrincipalDetails 타입으로 받을 수 있도록 구현.
    // Authentication 으로 접근시 다운캐스팅 필요, @AuthenticationPrincipal 어노테이션으로 접근시 다운캐스팅 필요없음.
    // loadUser, loadUserByUsername 의 리턴타입 모두 PrincipalDetails 타입 -> Authentication 에 넣어주면 됨.
    // 오버라이딩 안하면 기본적으로 UserDetails, Oauth2User 타입으로 리턴됨. -> 묶을수 없기때문에 각각 오버라이딩


    @GetMapping("/user")
    public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails principalDetails){
        System.out.println("principalDetails : " + principalDetails.getUser());
        return "User";
    }

    @GetMapping("/admin")
    public @ResponseBody String admin(){
        return "admin";
    }

    @GetMapping("/manager")
    public @ResponseBody String manager(){
        return "manager";
    }

    // 시큐리티가 주소 후킹 - security config이후 안함.
    @GetMapping("/loginForm")
    public  String loginForm(){
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public  String joinForm(){
        return "joinForm";
    }

    @PostMapping("/join")
    public String join(User user){
        // 비밀번호 암호화 필요 ,시큐리티는 암호화된 비밀번호만 로그인 가능. => 암호화 SecurityConfig에 등록.
        System.out.println(user);
        // 역할 지정
        user.setRole("ROLE_USER");
        String rawPassword = user.getPassword();
        // 비밀번호 암호화
        user.setPassword(encodePwd.encode(rawPassword));
        userRepository.save(user);

        // 회원가입이 완료되면 로그인 페이지로 이동(loginForm 함수로)
        return "redirect:/loginForm";
    }

    // 메소드 하나에만 권한 설정을 하고싶을때. -> 글로벌로 하려면 config에서 설정해주어야한다.
    @Secured("ROLE_ADMIN") // 어노테이션으로 권한 설정 => config에서 설정해주어야한다.
    // 두개? @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/info")
    public @ResponseBody String info(){
        return "개인정보";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") // 메소드 실행 전에 권한을 체크 => config에서 설정해주어야한다.
    //@PostAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") // 메소드 실행 후에 권한을 체크 => config에서 설정해주어야한다.
    @GetMapping("/data")
    public @ResponseBody String data(){
        return "데이터정보";
    }

    // @GetMapping("/login/oauth2/code/google") => 이거는 자동으로 구성.
    // 구글 로그인이 완료되면 code를 리턴받는다.
}

