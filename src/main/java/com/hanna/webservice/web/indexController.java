package com.hanna.webservice.web;

import com.hanna.webservice.service.posts.PostsService;
import com.hanna.webservice.web.dto.PostsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class indexController { //Shop

    private final PostsService postsService;

    @GetMapping("/")
    public String index(Model model) { //메인페이지
        model.addAttribute("posts",postsService.findAllDesc());
        return "index";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    } //등록페이지

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model) { //수정페이지
        PostsResponseDto dto = postsService.findById(id);
        model.addAttribute("post",dto);

        return "posts-update";
    }
}
