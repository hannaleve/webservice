package com.hanna.webservice.web;

import com.hanna.webservice.service.PostsService;
import com.hanna.webservice.dto.posts.PostsSaveRequestDto;
import com.hanna.webservice.dto.posts.PostsResponseDto;
import com.hanna.webservice.dto.posts.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class PostsApiController {

    private final PostsService postsService;

    @PostMapping("/api/v1/posts")
    public Long save(@RequestBody PostsSaveRequestDto postsSaveRequestDto) { //등록
        return postsService.save(postsSaveRequestDto);
    }

    @PutMapping("/api/v1/posts/{id}") //{} PathVariable
    public Long update(@PathVariable Long id, @RequestBody PostsUpdateRequestDto requestDto) { //수정
        return postsService.update(id,requestDto);
    }

    @GetMapping("/api/v1/posts/{id}")
    public PostsResponseDto findById (@PathVariable Long id) { //조회
        return postsService.findById(id);
    } //조회

    @DeleteMapping("/api/v1/posts/{id}")
    public Long delete(@PathVariable Long id) { //삭제
        postsService.delete(id);
        return id;
    }
}
