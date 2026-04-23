package com.example.takeouttry.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订单评价实体类
 */
@Data
public class Comment {
    private Long id;            // 评价ID
    private Long orderId;       // 关联的订单ID
    private Long userId;        // 评价人ID
    private Long merchantId;    // 商家ID
    private Integer score;      // 评分 (1-5)
    private String content;     // 评价内容
    private String imageUrls;   // 图片URL，多张用逗号隔开
    private String replyContent;// 商家回复
    private LocalDateTime replyTime; // 商家回复时间
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 更新时间
}