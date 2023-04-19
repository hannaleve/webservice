package com.hanna.webservice.domain.posts;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter //Entity클래스에선 절대 Setter메소드 생성 X
@NoArgsConstructor
@Entity //테이블과 링크될 클래스 (보통_카멜케이스로 지정)
public class Posts {

    @Id //pk
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment
    private Long id;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private  String author;

    @Builder //빌더패턴적용(객체생성 시 생성자를 통해 생성, 생성자와 다르게 빌더패턴경우 순서에 종속적이지않음.)
    public Posts(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

}
