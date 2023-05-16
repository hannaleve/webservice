package com.hanna.webservice.login.service;

import com.hanna.webservice.domain.user.User;
import com.hanna.webservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final UserRepository userRepository;

    /*
    ProviderManager전달받은 UsernamePasswordAuthenticationToken을 ProviderManager구현체인 DaoAuthenticationProvider전달
    DaoAuthenticationProvider는 UserDetailsService의 loadUserByUsername를 통해 UserDetails(유저정보) 객체 받아온다.
    즉 클라이언트 Request의 username을 통해 DB에서 유저 찾아 있다면 user entity를 반환한다.
    그 entity를 내부적으로 UserDetails만들어서 반환받는다.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일이 존재하지 않습니다."));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
