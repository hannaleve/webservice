package com.hanna.webservice.repository;

import com.hanna.webservice.domain.posts.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Posts,Long> { //JpaRepository<Entity 클래스, PK 타입〉를 상속 시 기본적인 crud 메소드 자동생성

    @Query("SELECT p FROM Posts p ORDER BY p.id DESC")
    List<Posts> findAllDesc(); //조회
}
