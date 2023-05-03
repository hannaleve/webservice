package com.hanna.webservice.config.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) //어노테이션 타입은 PARAMETER 메소드의 파라미터로 선언된 객체에서만 사용
@Retention(RetentionPolicy.RUNTIME) //클래스를 메모리에 읽어왔을 때까지 유지
public @interface LoginUser { //@interface 어노테이션 클래스 생성 (LoginUser 이름을 가진 어노테이션 생성)
}
