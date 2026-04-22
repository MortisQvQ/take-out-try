package com.example.takeouttry.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {

    private Long id;            // 分类ID，自增主键

    private Long merchantId;    // 所属商家ID，对应 merchant 表

    private String name;        // 分类名称（如热销、主食、饮品）

    private Integer sort;       // 排序权重（数字越小越靠前）

    private LocalDateTime createTime;   // 创建时间

    private LocalDateTime updateTime;   // 更新时间

}