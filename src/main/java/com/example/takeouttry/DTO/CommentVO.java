package com.example.takeouttry.DTO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价显示对象
 * 用于给前端展示：包含用户信息和处理后的评价内容
 */
@Data
public class CommentVO {

    private Long id;            // 评价ID

    // --- 用户信息（通过关联查询获得） ---
    private String username;    // 用户昵称
    private String avatarUrl;   // 用户头像地址

    // --- 评价内容 ---
    private Integer score;      // 评分 (1-5)
    private String content;     // 评价文本
    private String imageUrls;   // 图片字符串（"img1.jpg,img2.jpg"）

    // --- 商家回复 ---
    private String replyContent;// 商家回复内容

    private LocalDateTime createTime; // 评价时间
}