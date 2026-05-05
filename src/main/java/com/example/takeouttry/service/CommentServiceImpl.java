package com.example.takeouttry.service;

import com.example.takeouttry.DTO.CommentVO;
import com.example.takeouttry.entity.Comment;
import com.example.takeouttry.mapper.CommentsMapper;
import com.example.takeouttry.mapper.OrdersMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 1. 提交评价
     * 保持原有逻辑：检查重复评价、插入评论、更新订单状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean postComment(Comment comment) {
        // 校验：检查该订单是否已经评价过
        Comment existingComment = commentsMapper.selectByOrderId(comment.getOrderId());
        if (existingComment != null) {
            throw new RuntimeException("该订单已完成评价，请勿重复操作");
        }

        // 设置评价时间
        comment.setCreateTime(LocalDateTime.now());

        // 插入评价表
        int commentResult = commentsMapper.insert(comment);

        // 同步更新订单表的评价状态字段
        int orderResult = ordersMapper.updateHasCommentStatus(comment.getOrderId(), 1);

        return commentResult > 0 && orderResult > 0;
    }

    /**
     * 2. 获取商家评价列表
     */
    @Override
    public List<CommentVO> getMerchantComments(Long merchantId) {
        return commentsMapper.selectByMerchantId(merchantId);
    }

    /**
     * 3. 获取商家平均分
     */
    @Override
    public Double getMerchantRating(Long merchantId) {
        Double rating = commentsMapper.getAverageScoreByMerchantId(merchantId);
        // 如果没人评价返回 5.0，保持数据美观
        return rating != null ? rating : 5.0;
    }

    /**
     * 4. 商家回复评价
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateReply(Long id, String replyContent) {
        if (id == null) {
            throw new IllegalArgumentException("评价ID不能为空");
        }

        // 构造只包含更新字段的 Comment 对象
        Comment comment = new Comment();
        comment.setId(id);
        comment.setReplyContent(replyContent);

        // 使用现代时间 API（和你 postComment 里的 LocalDateTime.now() 保持一致）
        comment.setReplyTime(LocalDateTime.now());

        // 调用 Mapper 中已有的局部更新方法
        int rows = commentsMapper.updateByPrimaryKeySelective(comment);

        return rows > 0;
    }
}