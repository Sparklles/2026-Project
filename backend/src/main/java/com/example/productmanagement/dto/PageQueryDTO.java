package com.example.productmanagement.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    /** 当前页码，默认为 1 */
    private Integer current = 1;

    /** 每页显示条数，默认为 10 */
    private Integer size = 10;

    /** 可选：搜索关键字 */
    private String keyword;
}
