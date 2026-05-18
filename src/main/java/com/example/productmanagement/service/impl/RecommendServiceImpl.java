package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.BookTag;
import com.example.productmanagement.entity.BookTagRelation;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.BookTagMapper;
import com.example.productmanagement.mapper.BookTagRelationMapper;
import com.example.productmanagement.mapper.RecommendationItemMapper;
import com.example.productmanagement.service.RecommendService;
import com.example.productmanagement.vo.HomeRecommendVO;
import com.example.productmanagement.vo.RecommendBookVO;
import com.example.productmanagement.vo.ThemeRecommendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final RecommendationItemMapper recommendationItemMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BookTagRelationMapper bookTagRelationMapper;
    private final BookTagMapper bookTagMapper;

    private static final int LIMIT = 6;

    @Override
    public List<RecommendBookVO> getAlsoBought(Long bookId) {
        List<Long> targetIds = recommendationItemMapper.selectTargetBookIdsBySource("ALSO_BOUGHT", bookId, LIMIT);
        return buildRecommendBookVOs(targetIds);
    }

    @Override
    public List<RecommendBookVO> getPersonalized(Long userId) {
        List<Long> targetIds;
        if (userId != null) {
            targetIds = recommendationItemMapper.selectTargetBookIdsByUser("PERSONALIZED", userId, LIMIT);
            if (targetIds == null || targetIds.isEmpty()) {
                targetIds = recommendationItemMapper.selectTargetBookIdsByScene("POPULAR", LIMIT);
            }
        } else {
            targetIds = recommendationItemMapper.selectTargetBookIdsByScene("POPULAR", LIMIT);
        }
        return buildRecommendBookVOs(targetIds);
    }

    @Override
    public HomeRecommendVO getHomeRecommend() {
        HomeRecommendVO vo = new HomeRecommendVO();

        List<Long> carouselIds = recommendationItemMapper.selectTargetBookIdsByScene("HOME_TOPIC", LIMIT);
        vo.setHomeTopic(buildThemeRecommendVOs(carouselIds));

        List<Long> popularIds = recommendationItemMapper.selectTargetBookIdsByScene("POPULAR", LIMIT);
        vo.setPopular(buildRecommendBookVOs(popularIds));

        List<Long> newBookIds = recommendationItemMapper.selectTargetBookIdsByScene("NEW", LIMIT);
        vo.setNewBooks(buildRecommendBookVOs(newBookIds));

        return vo;
    }

    private List<ThemeRecommendVO> buildThemeRecommendVOs(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<BookInfo> books = bookInfoMapper.selectBatchIds(bookIds);

        List<ThemeRecommendVO> result = new ArrayList<>();
        for (Long bookId : bookIds) {
            BookInfo book = books.stream().filter(b -> b.getId().equals(bookId)).findFirst().orElse(null);
            if (!isAvailable(book)) {
                continue;
            }
            ThemeRecommendVO vo = new ThemeRecommendVO();
            vo.setId(book.getId());
            vo.setTitle(book.getTitle());
            vo.setAuthor(book.getAuthor());
            vo.setDescription(book.getDescription());
            vo.setCoverImageUrl(book.getCoverImageUrl());
            result.add(vo);
        }
        return result;
    }

    private List<RecommendBookVO> buildRecommendBookVOs(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<BookInfo> books = bookInfoMapper.selectBatchIds(bookIds);

        Map<Long, List<String>> tagsMap = getTagsMap(bookIds);

        List<RecommendBookVO> result = new ArrayList<>();
        for (Long bookId : bookIds) {
            BookInfo book = books.stream().filter(b -> b.getId().equals(bookId)).findFirst().orElse(null);
            if (!isAvailable(book)) {
                continue;
            }
            RecommendBookVO vo = new RecommendBookVO();
            vo.setId(book.getId());
            vo.setTitle(book.getTitle());
            vo.setAuthor(book.getAuthor());
            vo.setPrice(book.getPrice());
            vo.setCoverImageUrl(book.getCoverImageUrl());
            vo.setDifficultyTag(book.getDifficultyTag());
            vo.setTags(tagsMap.getOrDefault(bookId, Collections.emptyList()));
            result.add(vo);
        }
        return result;
    }

    private Map<Long, List<String>> getTagsMap(List<Long> bookIds) {
        LambdaQueryWrapper<BookTagRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.in(BookTagRelation::getBookId, bookIds);
        List<BookTagRelation> relations = bookTagRelationMapper.selectList(relWrapper);

        List<Long> tagIds = relations.stream().map(BookTagRelation::getTagId).distinct().collect(Collectors.toList());
        Map<Long, String> tagNameMap;
        if (tagIds.isEmpty()) {
            tagNameMap = Collections.emptyMap();
        } else {
            List<BookTag> tags = bookTagMapper.selectBatchIds(tagIds);
            tagNameMap = tags.stream().collect(Collectors.toMap(BookTag::getId, BookTag::getName));
        }

        return relations.stream()
                .collect(Collectors.groupingBy(
                        BookTagRelation::getBookId,
                        Collectors.mapping(
                                r -> tagNameMap.getOrDefault(r.getTagId(), ""),
                                Collectors.toList()
                        )
                ));
    }

    private boolean isAvailable(BookInfo book) {
        return book != null
                && Integer.valueOf(1).equals(book.getStatus())
                && Integer.valueOf(0).equals(book.getIsDeleted());
    }
}
