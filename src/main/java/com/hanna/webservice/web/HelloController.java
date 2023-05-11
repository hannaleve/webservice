package com.hanna.webservice.web;

import com.hanna.webservice.dto.posts.HelloResponseDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//@RestController //json으로 반환하는 컨트롤러
@Controller
@RequestMapping("/hello")
@Log4j2
public class HelloController {

    @GetMapping("/hello")
    public void hello() {
        //log.info("나와라!");
    }

    @GetMapping("/hello/dto")
    public HelloResponseDto helloDto(@RequestParam("name") String name, @RequestParam("amount") int amount) {
        return new HelloResponseDto(name,amount);
    }
}
