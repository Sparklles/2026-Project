package com.example.productmanagement.controller.front;

import com.example.productmanagement.controller.Result;
import com.example.productmanagement.service.RecommendService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.HomeRecommendVO;
import com.example.productmanagement.vo.RecommendBookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/front/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/also-bought")
    public Result<List<RecommendBookVO>> alsoBought(@RequestParam Long bookId) {
        List<RecommendBookVO> list = recommendService.getAlsoBought(bookId);
        return Result.success(list);
    }

    @GetMapping("/personalized")
    public Result<List<RecommendBookVO>> personalized() {
        Long userId = getCurrentUserId();
        List<RecommendBookVO> list = recommendService.getPersonalized(userId);
        return Result.success(list);
    }

    @GetMapping("/home")
    public Result<HomeRecommendVO> home() {
        HomeRecommendVO vo = recommendService.getHomeRecommend();
        return Result.success(vo);
    }

    private Long getCurrentUserId() {
        try {
            return UserHolder.getLoginUser().getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}