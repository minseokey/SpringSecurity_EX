package com.exapmle.jwtEX.controller;

import com.exapmle.jwtEX.model.User;
import com.exapmle.jwtEX.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class RestApiController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/home")
    public String home(){
        return "<h1>home</h1>";
    }

    @PostMapping("/token")
    public String token(){
        return "<h1>token</h1>";
    }

    @PostMapping("/join")
    public String join(@RequestBody User user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_USER");
        userRepository.save(user);
        return "회원가입 완료";
    }

    // 모든 권한
    @GetMapping("user")
    public String user(){
        return "user";
    }
    // 매니저, 어드민
    @GetMapping("manager")
    public String manager(){
        return "manager";
    }
    // 어드민
    @GetMapping("admin")
    public String admin(){
        return "admin";
    }
}
