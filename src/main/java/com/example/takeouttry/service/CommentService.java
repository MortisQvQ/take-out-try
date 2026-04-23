package com.example.takeouttry.service;

import com.example.takeouttry.entity.Comment;
import com.example.takeouttry.DTO.CommentVO;
import java.util.List;

public interface CommentService {
    // 提交评价
    boolean postComment(Comment comment);

    // 根据商家查询评价（VO 包含用户信息）
    List<CommentVO> getMerchantComments(Long merchantId);

    // 获取商家平均分
    Double getMerchantRating(Long merchantId);

    boolean updateReply(Long id, String replyContent);
}