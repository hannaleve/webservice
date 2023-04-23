package com.hanna.webservice.domain.posts;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass //BaseTimeEntity클래스 상속받는 경우 필드들도 칼럼으로 인식가능하도록
@EntityListeners(AuditingEntityListener.class) //Auditing기능 포함시킴
public class BaseTimeEntity { //모든 Entity의 상위 클래스로 Entity들의 createdDate, modifiedDate를 자동으로 관리하는 역할로서의 클래스

    @CreatedDate //Entity 생성되어 저장될 때 시간 자동 저장
    private LocalDateTime createdDate;

    @LastModifiedDate //조회한 Entity의 값 변경시 시간 자동 저장
    private LocalDateTime modifiedDate;
}
