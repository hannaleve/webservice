package com.hanna.webservice.web;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HelloController.class)
@ExtendWith(SpringExtension.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void helloReturn() throws Exception {
        //given
        String hello = "hello";

        //when
        /*
        - get요청
        - HTTP Header의 Status검증 (200,400,500 등)
        - 응답 본문의 내용 검증 ("hello 리턴되는 지 확인용)
         */
        mvc.perform(get("/hello"))
                //then
                .andExpect(status().isOk())
                .andExpect(content().string(hello));
    }

    @Test
    /*
    - jsonPath : json 응답값을 필드별로 검증할 수 있는 메소드, $를 기준으로 필드명 명시
     */
    void helloDtoReturn() throws Exception {
        String name = "hello";
        int amount = 1000;


        mvc.perform(get("/hello/dto")
                        .param("name",name)
                        .param("amount", String.valueOf(amount)))//숫자 or 날짜 등 데이터 등록 시 문자열로 변경 필수
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.amount",is(amount)));
    }
}
