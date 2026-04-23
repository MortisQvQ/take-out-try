package com.example.takeouttry.mapper;

import com.example.takeouttry.entity.Comment;
import com.example.takeouttry.DTO.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
/**
 * @author 33126
 * @description 针对表【comments(订单评价表)】的数据库操作Mapper
 * @Entity com.example.takeouttry.entity.Comment
 */
@Mapper
public interface CommentsMapper {

    int deleteByPrimaryKey(Long id);

    // 注意：这里要把 Comments 改成 Comment (单数)，和你实体类名保持一致
    int insert(Comment record);

    int insertSelective(Comment record);

    Comment selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Comment record);

    int updateByPrimaryKey(Comment record);

    /**
     * 根据商家ID查询评价列表
     */
    List<CommentVO> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 根据用户ID查询评价记录
     */
    List<Comment> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据订单ID查询评价
     */
    Comment selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 获取商家的平均评分
     */
    Double getAverageScoreByMerchantId(@Param("merchantId") Long merchantId);

}