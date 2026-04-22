package com.example.takeouttry.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResult<T> {
    private List<T> list;       // 当前页数据
    private long total;         // 总条数
    private int pages;          // 总页数
    private int pageNum;        // 当前页
    private int pageSize;       // 每页条数
}