package com.hanna.webservice.service.posts;

import com.hanna.webservice.domain.posts.Posts;
import com.hanna.webservice.domain.posts.PostsRepository;
import com.hanna.webservice.web.dto.PostsResponseDto;
import com.hanna.webservice.web.dto.PostsSaveRequestDto;
import com.hanna.webservice.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor //@Autowired와 같은 빈 주입받기(생성자)
@Service
public class PostsService {

    private final PostsRepository postsRepository;

    @Transactional
    public Long save(PostsSaveRequestDto postsSaveRequestDto) {
        return postsRepository.save(postsSaveRequestDto.toEntity()).getId();

    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto) {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id)); //값이 null인경우 예외처리
        posts.update(requestDto.getTitle(),requestDto.getContent());

        return id;
    }

    public PostsResponseDto findById(Long id) {
        Posts entity = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id)); //값이 null인경우 예외처리

        return new PostsResponseDto(entity);
    }
}
