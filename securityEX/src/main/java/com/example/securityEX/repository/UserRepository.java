package com.example.securityEX.repository;

import com.example.securityEX.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

// CRUD 함수를 JpaRepository가 들고 있음.
// @Repository 를 추가할 필요도 없음. IOC 등록이 되어있음(bean으로)
// 필요한곳에 autowired 해서 쓰면 됨.
public interface UserRepository  extends JpaRepository<User, Integer> {

    // findby 규칙 => Username 문법
    // select * from user where username = 1?
    User findByUsername(String username);
}
