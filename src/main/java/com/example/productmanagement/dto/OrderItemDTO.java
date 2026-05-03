package com.example.productmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {

    @NotNull(message = "书籍ID不能为空")
    private Long bookId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "数量不能少于1")
    private Integer quantity;
}
