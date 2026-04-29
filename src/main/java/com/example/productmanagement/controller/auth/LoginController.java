package com.example.productmanagement.controller.auth;


import com.example.productmanagement.dto.LoginDto;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.service.UserDetailService;
import com.example.productmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/login")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final UserDetailService userDetailService;

    @PostMapping("/user")
    public Result<String> loginUser(@RequestBody LoginDto loginDto) {
        return Result.ok(userService.login(loginDto));
    }

    @PostMapping("/admin")
    public Result<String> loginAdmin(@RequestBody LoginDto loginDto) {
        return Result.ok(userService.login(loginDto));
    }

}
