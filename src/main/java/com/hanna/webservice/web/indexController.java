package com.hanna.webservice.web;

import com.hanna.webservice.config.auth.LoginUser;
import com.hanna.webservice.config.dto.SessionUser;
import com.hanna.webservice.service.PostsService;
import com.hanna.webservice.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class indexController { //메인

    private final PostsService postsService;
    private final HttpSession httpSession;
    private final UserService userService;

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        model.addAttribute("posts", postsService.findAllDesc());

        if (user != null) {
            model.addAttribute("userName", user.getName());
        }

        return "index";

    }


//    @GetMapping("/posts/save")
//    public String postsSave() {
//        return "posts-save";
//    } //등록페이지
//
//    @GetMapping("/posts/update/{id}")
//    public String postsUpdate(@PathVariable Long id, Model model) { //수정페이지
//        PostsResponseDto dto = postsService.findById(id);
//        model.addAttribute("post",dto);
//
//        return "posts-update";
//    }




}
