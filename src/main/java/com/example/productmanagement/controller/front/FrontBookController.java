package com.example.productmanagement.controller.front;
import com.example.productmanagement.controller.Result;
import com.example.productmanagement.service.FrontBookService;
import com.example.productmanagement.vo.ProductDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/front/books")
public class FrontBookController {

    @Autowired
    private FrontBookService frontBookService;

    @GetMapping("/{id}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable("id") Long id) {
        try {
            ProductDetailVO detail = frontBookService.getProductDetail(id);
            return Result.success(detail);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }
}
