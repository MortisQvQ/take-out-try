package com.example.takeouttry.controller;

import com.example.takeouttry.DTO.CommentVO;
import com.example.takeouttry.DTO.Result;
import com.example.takeouttry.entity.Comment;
import com.example.takeouttry.security.JwtUser;
import com.example.takeouttry.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor  // 自动生成构造器注入，代替 @Autowired
public class CommentController {

    private final CommentService commentService;

    /**
     * 添加评价
     * 使用 @AuthenticationPrincipal 直接注入解析好的 JwtUser
     */
    @PostMapping("/add")
    public Result<String> add(
            @AuthenticationPrincipal JwtUser user,
            @RequestBody Comment comment) {

        // 登录校验
        if (user == null) {
            log.warn("评价提交失败：未获取到当前用户信息");
            return Result.error("登录已失效，请重新登录", 401);
        }

        // 自动填充从 Token 中解析出来的用户 ID
        // 对应 JwtUser 中的 private final Long id;
        comment.setUserId(user.getId());

        log.info("用户 {} (ID: {}) 提交了订单 {} 的评价", user.getUsername(), user.getId(), comment.getOrderId());

        // 执行业务逻辑
        try {
            boolean success = commentService.postComment(comment);
            if (success) {
                return Result.success("评价发表成功！");
            } else {
                return Result.error("评价发表失败，请重试");
            }
        } catch (RuntimeException e) {
            // 捕获 Service 层抛出的业务逻辑异常（如订单已评价、订单状态不对等）
            log.error("评价业务拦截: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("评价系统异常: ", e);
            return Result.error("服务器开小差了，请稍后再试");
        }
    }

    /**
     * 获取商家的所有评价（给前端商家详情页用）
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<CommentVO>> listByMerchant(@PathVariable Long merchantId) {
        log.info("查询商家 ID: {} 的评价列表", merchantId);
        List<CommentVO> comments = commentService.getMerchantComments(merchantId);
        return Result.success(comments);
    }

    /**
     * 获取商家平均评分
     */
    @GetMapping("/rating/{merchantId}")
    public Result<Double> getRating(@PathVariable Long merchantId) {
        Double rating = commentService.getMerchantRating(merchantId);
        // 如果没有评分，默认返回 5.0
        return Result.success(rating != null ? rating : 5.0);
    }

    /**
     * 商家回复评价
     */
    @PutMapping("/reply")
    public Result<String> reply(@RequestBody Comment comment) {
        // 校验基本数据
        if (comment.getId() == null || comment.getReplyContent() == null) {
            return Result.error("参数不完整");
        }

        log.info("商家正在回复评价 ID: {}, 内容: {}", comment.getId(), comment.getReplyContent());

        try {
            // 调用 service 更新回复
            boolean success = commentService.updateReply(comment.getId(), comment.getReplyContent());
            return success ? Result.success("回复成功") : Result.error("回复失败");
        } catch (Exception e) {
            log.error("回复评价异常", e);
            return Result.error("回复失败：" + e.getMessage());
        }
    }
}