package com.hanna.webservice.web.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class HelloResponseDtoTest {

    @Test
    public void lombokImpoTest() {

        //given
        String name = "test";
        int amount = 1000;

        //when
        HelloResponseDto dto = new HelloResponseDto(name,amount);

        //then
        /*
        - assertThat 테스트 검증 라이브버리 검증 메소드(검증하고자 하는 대상을 메소드 인자로 받기)
        - isEqualTo 비교해서 같은 경우에만 성공
         */
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getAmount()).isEqualTo(amount);

    }
}
