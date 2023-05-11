package com.hanna.webservice.web;

import com.hanna.webservice.domain.posts.Posts;
import com.hanna.webservice.repository.PostsRepository;
import com.hanna.webservice.dto.posts.PostsSaveRequestDto;
import com.hanna.webservice.dto.posts.PostsUpdateRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.
        WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class PostsApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @AfterEach
    public void testDown() throws Exception {
        postsRepository.deleteAll();
    }

    @Test
    public void PostsInsert() throws Exception {
        //given
        String title = "title";
        String content = "content";

        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                .title(title)
                .content(content)
                .author("hanna")
                .build();

        //test용 url
        String url = "http://localhost:" + port + "/api/v1/posts";

        //when
        //TestRestTemplate (RestTemplate를 사용한 JSON으로 POST요청 만들기)
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url,requestDto,Long.class); //type을 Long타입으로 반환

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L); //값이 지정된 값보다 큰 지 테스트 (long형 0)

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
    }

    @Test
    public void PostsUpdate() throws Exception {
        //given
        //일단 등록
        Posts savedPosts = postsRepository.save(Posts.builder()
                        .title("title")
                        .content("content")
                        .author("hanna")
                .build());

        Long updateId = savedPosts.getId();

        //수정된 데이터
        String expectedTitle = "title2";
        String expectedContent = "content2";

        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
                .title(expectedTitle)
                .content(expectedContent)
                .build();

        String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

        //HTTP요청또는 응답에 해당하는 HttpHeader와 HttpBody를 포함하는 클래스
        HttpEntity<PostsUpdateRequestDto> requestDtoHttpEntity = new HttpEntity<>(requestDto);

        //when
        //exchange 모든 HTTP 요청 메소드를 지원하며 원하는 서버에 요청시켜주는 메소드
        ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestDtoHttpEntity,Long.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);

    }
}
