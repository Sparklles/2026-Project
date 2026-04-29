package com.example.productmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.vo.ProductDetailVO;

import com.example.productmanagement.vo.ProductDetailVO;

public interface FrontBookService {

    /**
     * 获取前台商品详情（包含基础信息、标签、评价聚合）
     * @param productId 商品主键ID
     * @return 完整商品详情视图对象
     */
    ProductDetailVO getProductDetail(Long productId);
}
