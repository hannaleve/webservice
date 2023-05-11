package com.hanna.webservice.config.auth;

import com.hanna.webservice.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity //Spring Security설정 활성화
@ConditionalOnDefaultWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration //@Bean등록(설정파일)
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().disable() //cors 방지
                .csrf().disable()  //csrf 방지
                .headers().frameOptions().disable()
                .and()
                .authorizeHttpRequests() //URL별 권한 관리 설정하는 옵션 시작 진입
                /*
                권한 관리 대상 지정 옵션
                URL, HTTP 메소드 별로 관리 가능
                "/"등 지정된 URL들은 permitAll() 옵션을 통해 전체 열람 권한 주기
                "/api/v1/**" 주소를 가진 API는 USER 권한을 가진 사람만 가능하도록함
                 */
                .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**", "/profile").permitAll()
                .requestMatchers("/api/v1/**").hasRole(Role.
                        USER.name())
                .anyRequest().authenticated()  //설정된 값들 이외 나머지 URL는 authenticated() 추가하여 나머지 URL들은 모두 인증된 사용자만 허용 (인증된 사용자 -> 로그인한 사용자)
                .and()
                .logout() //로그아웃 기능에 대한 여러 설정 진입
                .logoutSuccessUrl("/") //로그아웃 성공 시 주소 "/walk/main" 이동
                .and()
                .oauth2Login() //OAuth2 로그인 기능에 대한 여러 설정 진입점
                .userInfoEndpoint() //OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정 담당
                .userService(oAuth2UserService); //소셜로그인 성공 시 후속 조치를 진행할 UserService의 인터페이스 구현체 등록


        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }


}
