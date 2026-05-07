package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.productmanagement.common.ErrorCode;
import com.example.productmanagement.dto.CartAddRequest;
import com.example.productmanagement.dto.CartDeleteRequest;
import com.example.productmanagement.dto.CartUpdateRequest;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.Cart;
import com.example.productmanagement.exception.BusinessException;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.CartMapper;
import com.example.productmanagement.service.CartService;
import com.example.productmanagement.vo.CartVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final BookInfoMapper bookInfoMapper;

    public CartServiceImpl(CartMapper cartMapper, BookInfoMapper bookInfoMapper) {
        this.cartMapper = cartMapper;
        this.bookInfoMapper = bookInfoMapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(CartAddRequest request) {
        Long userId = request.getUserId();
        Long bookId = request.getBookId();
        Integer quantity = request.getQuantity();

        // 检查书籍是否存在且上架
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null || bookInfo.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        if (bookInfo.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BOOK_OFF_SHELF);
        }

        // 检查库存是否充足
        if (bookInfo.getStock() < quantity) {
            throw new BusinessException(ErrorCode.ORDER_STOCK_INSUFFICIENT);
        }

        // 查询购物车中是否已存在该商品
        Cart existCart = cartMapper.selectByUserIdAndBookId(userId, bookId);

        if (existCart != null) {
            // 已存在，更新数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            existCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existCart);
            log.info("更新购物车数量，userId={}, bookId={}, newQuantity={}", userId, bookId, existCart.getQuantity());
        } else {
            // 不存在，新增记录
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setBookId(bookId);
            cart.setQuantity(quantity);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
            log.info("新增购物车记录，userId={}, bookId={}, quantity={}", userId, bookId, quantity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCartQuantity(CartUpdateRequest request) {
        Long userId = request.getUserId();
        Long cartId = request.getCartId();
        Integer quantity = request.getQuantity();

        // 校验权限
        Cart cart = cartMapper.selectByUserIdAndCartId(userId, cartId);
        if (cart == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        // 检查库存是否充足
        BookInfo bookInfo = bookInfoMapper.selectById(cart.getBookId());
        if (bookInfo != null && bookInfo.getStock() < quantity) {
            throw new BusinessException(ErrorCode.ORDER_STOCK_INSUFFICIENT);
        }

        // 更新数量
        cart.setQuantity(quantity);
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);

        log.info("更新购物车数量，cartId={}, newQuantity={}", cartId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCart(CartDeleteRequest request) {
        Long userId = request.getUserId();
        List<Long> cartIds = request.getCartIds();

        if (CollectionUtils.isEmpty(cartIds)) {
            return;
        }

        // 批量删除，只删除属于当前用户的记录
        int rows = cartMapper.deleteBatchByIds(userId, cartIds);
        log.info("批量删除购物车商品，userId={}, cartIds={}, rows={}", userId, cartIds, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartById(Long userId, Long cartId) {
        // 校验权限
        Cart cart = cartMapper.selectByUserIdAndCartId(userId, cartId);
        if (cart == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        cartMapper.deleteById(cartId);
        log.info("删除购物车商品，userId={}, cartId={}", userId, cartId);
    }

    @Override
    public IPage<CartVO> getCartList(Long userId, Integer page, Integer pageSize) {
        Page<Cart> pageParam = new  Page<>(page, pageSize);

        // 查询购物车列表
        IPage<Cart> cartPage = cartMapper.selectCartListByUserId(pageParam, userId);

        if(cartPage.getTotal()<=0 || cartPage.getRecords().size()<=0){
            return cartPage.convert(record->null);  // 空转换
        }

        // 获取书籍ID列表
        List<Long> bookIds = cartPage.getRecords().stream()
                .map(Cart::getBookId)
                .distinct()
                .collect(Collectors.toList());

        List<BookInfo> bookInfoList = bookInfoMapper.selectBatchIds(bookIds);

        Map<Long, BookInfo> bookInfoMap = bookInfoList.stream()
                .collect(Collectors.toMap(BookInfo::getId, book -> book));

        // 6. 转换为 VO 列表
        List<CartVO> voList = cartPage.getRecords().stream()
                .map(cart -> convertToVO(cart, bookInfoMap.get(cart.getBookId())))
                .collect(Collectors.toList());

        // 7. 构建 CartVO 分页结果（复用原分页参数）
        Page<CartVO> resultPage = new Page<>(cartPage.getCurrent(), cartPage.getSize());
        resultPage.setRecords(voList);
        resultPage.setTotal(cartPage.getTotal());  // 用数据库返回的总数

        return resultPage;
    }

    @Override
    public List<CartVO> getCartList(Long userId) {
        // 查询购物车列表
        List<Cart> cartList = cartMapper.selectCartListByUserId(userId);

        if (CollectionUtils.isEmpty(cartList)) {
            return new ArrayList<>();
        }

        // 获取书籍ID列表
        List<Long> bookIds = cartList.stream()
                .map(Cart::getBookId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询书籍信息
        List<BookInfo> bookInfoList = bookInfoMapper.selectBatchByIds(bookIds);
        Map<Long, BookInfo> bookInfoMap = bookInfoList.stream()
                .collect(Collectors.toMap(BookInfo::getId, book -> book));

        // 转换为VO
        List<CartVO> voList = new ArrayList<>();
        for (Cart cart : cartList) {
            CartVO vo = convertToVO(cart, bookInfoMap.get(cart.getBookId()));
            voList.add(vo);
        }

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        int rows = cartMapper.deleteByUserId(userId);
        log.info("清空购物车，userId={}, rows={}", userId, rows);
    }

    @Override
    public BigDecimal calculateTotalAmount(Long userId) {
        // 查询购物车列表
        List<Cart> cartList = cartMapper.selectCartListByUserId(userId);

        if (CollectionUtils.isEmpty(cartList)) {
            return BigDecimal.ZERO;
        }

        // 获取书籍ID列表
        List<Long> bookIds = cartList.stream()
                .map(Cart::getBookId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询书籍信息
        List<BookInfo> bookInfoList = bookInfoMapper.selectBatchByIds(bookIds);
        Map<Long, BookInfo> bookInfoMap = bookInfoList.stream()
                .collect(Collectors.toMap(BookInfo::getId, book -> book));

        // 计算总金额（只计算上架且未删除的商品）
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartList) {
            BookInfo bookInfo = bookInfoMap.get(cart.getBookId());
            if (bookInfo != null && bookInfo.getStatus() == 1 && bookInfo.getIsDeleted() == 0) {
                BigDecimal subtotal = bookInfo.getPrice()
                        .multiply(new BigDecimal(cart.getQuantity()));
                totalAmount = totalAmount.add(subtotal);
            }
        }

        return totalAmount;
    }

    @Override
    public Integer getCartTotalQuantity(Long userId) {
        return cartMapper.selectTotalQuantityByUserId(userId);
    }

    @Override
    public List<CartVO> getCartListByIds(Long userId, List<Long> cartIds) {
//        if (CollectionUtils.isEmpty(cartIds)) {
//            return new ArrayList<>();
//        }
//
//        // 查询选中的购物车记录
//        List<Cart> cartList = cartMapper.selectCartListByIds(userId, cartIds);
//
//        if (CollectionUtils.isEmpty(cartList)) {
//            return new ArrayList<>();
//        }
//
//        // 获取书籍ID列表
//        List<Long> bookIds = cartList.stream()
//                .map(Cart::getBookId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        // 批量查询书籍信息
//        List<BookInfo> bookInfoList = bookInfoMapper.selectBatchByIds(bookIds);
//        Map<Long, BookInfo> bookInfoMap = bookInfoList.stream()
//                .collect(Collectors.toMap(BookInfo::getId, book -> book));
//
//        // 转换为VO
//        List<CartVO> voList = new ArrayList<>();
//        for (Cart cart : cartList) {
//            CartVO vo = convertToVO(cart, bookInfoMap.get(cart.getBookId()));
//            voList.add(vo);
//        }
//
//        return voList;
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchCart(Long userId, List<Long> cartIds) {
        if (CollectionUtils.isEmpty(cartIds)) {
            return;
        }

        int rows = cartMapper.deleteBatchByIds(userId, cartIds);
        log.info("批量删除购物车商品，userId={}, cartIds={}, rows={}", userId, cartIds, rows);
    }

    @Override
    public boolean checkCartOwnership(Long userId, Long cartId) {
        Cart cart = cartMapper.selectByUserIdAndCartId(userId, cartId);
        return cart != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCartBook(Long userId, Long cartId, Long newBookId, Integer quantity) {
        // 校验权限
        Cart cart = cartMapper.selectByUserIdAndCartId(userId, cartId);
        if (cart == null) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        // 检查新书籍是否存在且上架
        BookInfo bookInfo = bookInfoMapper.selectById(newBookId);
        if (bookInfo == null || bookInfo.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        if (bookInfo.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BOOK_OFF_SHELF);
        }

        // 检查库存是否充足
        if (bookInfo.getStock() < quantity) {
            throw new BusinessException(ErrorCode.ORDER_STOCK_INSUFFICIENT);
        }

        // 检查新商品是否已在购物车中
        Cart existCart = cartMapper.selectByUserIdAndBookId(userId, newBookId);
        if (existCart != null && !existCart.getCartId().equals(cartId)) {
            // 新商品已存在，合并数量并删除当前记录
            existCart.setQuantity(existCart.getQuantity() + quantity);
            existCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existCart);
            cartMapper.deleteById(cartId);
            log.info("合并购物车商品，userId={}, cartId={}, newBookId={}", userId, cartId, newBookId);
        } else {
            // 更新为新的商品
            cart.setBookId(newBookId);
            cart.setQuantity(quantity);
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(cart);
            log.info("更新购物车商品种类，userId={}, cartId={}, newBookId={}", userId, cartId, newBookId);
        }
    }

    @Override
    public IPage<CartVO> getCartListByCategory(Long userId, Long categoryId, Integer page, Integer pageSize) {
        Page<Cart> pageParam = new Page<>(page, pageSize);
        IPage<Cart> cartPage = cartMapper.selectCartListByUserIdAndCategoryId(pageParam, userId, categoryId);

        if (cartPage.getTotal() <= 0 || cartPage.getRecords().size() <= 0) {
            return cartPage.convert(record -> null);
        }

        List<Long> bookIds = cartPage.getRecords().stream()
                .map(Cart::getBookId)
                .distinct()
                .collect(Collectors.toList());

        List<BookInfo> bookInfoList = bookInfoMapper.selectBatchIds(bookIds);
        Map<Long, BookInfo> bookInfoMap = bookInfoList.stream()
                .collect(Collectors.toMap(BookInfo::getId, book -> book));

        List<CartVO> voList = cartPage.getRecords().stream()
                .map(cart -> convertToVO(cart, bookInfoMap.get(cart.getBookId())))
                .collect(Collectors.toList());

        Page<CartVO> resultPage = new Page<>(cartPage.getCurrent(), cartPage.getSize());
        resultPage.setRecords(voList);
        resultPage.setTotal(cartPage.getTotal());

        return resultPage;
    }

    @Override
    public List<CartVO> getCartListByCategory(Long userId, Long categoryId) {
        List<Cart> cartList = cartMapper.selectCartListByUserIdAndCategoryId(userId, categoryId);

        if (CollectionUtils.isEmpty(cartList)) {
            return new ArrayList<>();
        }

        List<Long> bookIds = cartList.stream()
                .map(Cart::getBookId)
                .distinct()
                .collect(Collectors.toList());

        List<BookInfo> bookInfoList = bookInfoMapper.selectBatchIds(bookIds);
        Map<Long, BookInfo> bookInfoMap = bookInfoList.stream()
                .collect(Collectors.toMap(BookInfo::getId, book -> book));

        List<CartVO> voList = new ArrayList<>();
        for (Cart cart : cartList) {
            CartVO vo = convertToVO(cart, bookInfoMap.get(cart.getBookId()));
            voList.add(vo);
        }

        return voList;
    }

    /**
     * 将 Cart 实体和 BookInfo 实体转换为 CartVO
     *
     * @param cart 购物车实体
     * @param bookInfo 书籍信息实体
     * @return 购物车视图对象
     */
    private CartVO convertToVO(Cart cart, BookInfo bookInfo) {
        CartVO vo = new CartVO();
        vo.setCartId(cart.getCartId());
        vo.setUserId(cart.getUserId());
        vo.setBookId(cart.getBookId());
        vo.setQuantity(cart.getQuantity());
        vo.setCreateTime(cart.getCreateTime());
        vo.setUpdateTime(cart.getUpdateTime());

        if (bookInfo != null) {
            vo.setBookName(bookInfo.getTitle());
            vo.setAuthor(bookInfo.getAuthor());
            vo.setCoverUrl(bookInfo.getCoverImageUrl());
            vo.setPrice(bookInfo.getPrice());
            vo.setStock(bookInfo.getStock());
            vo.setStatus(bookInfo.getStatus());

            // 计算小计金额
            if (bookInfo.getPrice() != null) {
                vo.setSubtotal(bookInfo.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            }
        }

        return vo;
    }
}
