package com.hanna.webservice.domain.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostsRepository extends JpaRepository<Posts,Long> { //JpaRepository<Entity 클래스, PK 타입〉를 상속 시 기본적인 crud 메소드 자동생성
}
