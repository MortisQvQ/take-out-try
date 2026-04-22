package com.example.takeouttry.service;

import com.example.takeouttry.DTO.MerchantDTO.RegisterMerchantRequest;
import com.example.takeouttry.DTO.MerchantDTO.MerchantResponse;
import com.example.takeouttry.entity.Merchant;
import com.example.takeouttry.entity.User;
import com.example.takeouttry.mapper.AUsersMapper;
import com.example.takeouttry.mapper.MerchantMapper;
import com.example.takeouttry.security.JwtUtil;
import com.example.takeouttry.service.MerchantService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private AUsersMapper usersMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 注册用户 + 创建商家   失败则全部回滚
     * 数据库不会留下半条数据
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String registerMerchant(RegisterMerchantRequest request){
        //校验 用户名/手机号 唯一
        if (usersMapper.existsByUsername(request.getUsername()) > 0){
            throw new RuntimeException("用户已存在");
        }
        if (usersMapper.existsByPhone(request.getPhone()) > 0){
            throw new RuntimeException("手机号已被注册");
        }

        //创建用户账号
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(2);
        usersMapper.addUser(user);

        //user

        //创建商家记录
        Merchant merchant = new Merchant();

        merchant.setUserId(user.getId());                    //绑定（用户*商家）
        merchant.setName(request.getUsername());
        merchant.setAddress(request.getShopAddress());
        merchant.setPhone(request.getShopPhone());
        //merchant.setDescription(request.getDescription());
        merchant.setStatus(0);  // 待审核或初始状态
        merchant.setCreateTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());

        merchantMapper.insert(merchant);

        //生成 token
        return jwtUtil.generateToken(user);

    }

    @Override
    public MerchantResponse getByUserId(Long userId){
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if(merchant == null){
            return null;
        }

        MerchantResponse response = new MerchantResponse();
        BeanUtils.copyProperties(merchant,response);
        return response;
    }

    @Override
    public boolean updateMerchant(Long userId, Merchant merchant){
        Merchant existing =  merchantMapper.selectByUserId(userId);
        if(existing == null || !existing.getUserId().equals(userId)){
            return false;
        }

        existing.setName(merchant.getName());
        existing.setAddress(merchant.getAddress());
        existing.setPhone(merchant.getPhone());
        existing.setBusinessHours(merchant.getBusinessHours());
        existing.setLogo(merchant.getLogo());
        //existing.setDescription(merchant.getDescription());
        existing.setUpdateTime(LocalDateTime.now());

        return merchantMapper.updateById(existing) > 0;
    }

    @Override
    public Merchant getById(Long id){
        return merchantMapper.selectById(id);
    }

    @Override
    @Transactional
    public String uploadLogo(Long merchantId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }

        try {
            String projectRoot = System.getProperty("user.dir");
            String uploadDirPath = projectRoot + "/src/main/resources/static/uploads/logos/";

            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("【Logo目录创建】结果: " + (created ? "成功" : "失败")
                        + " → " + uploadDir.getAbsolutePath());

                if (!created) {
                    throw new RuntimeException("无法创建Logo目录: " + uploadDirPath);
                }
            }

            String filename = "logo_" + merchantId + ".jpg";
            File destFile = new File(uploadDirPath + filename);

            // 保存文件
            file.transferTo(destFile);
            System.out.println("【文件保存成功】路径: " + destFile.getAbsolutePath());

            String logoUrl = "/uploads/logos/" + filename;
            System.out.println("【生成Logo路径】: " + logoUrl);

            // 更新数据库
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant != null) {
                String oldLogo = merchant.getLogo();
                merchant.setLogo(logoUrl);

                int updateRows = merchantMapper.updateById(merchant);

                System.out.println("【数据库更新】旧Logo: " + oldLogo
                        + " | 新Logo: " + logoUrl
                        + " | 影响行数: " + updateRows);

                if (updateRows > 0) {
                    System.out.println("✅ Logo路径已成功保存到数据库！");
                } else {
                    System.out.println("❌ 更新失败，数据库没有发生变化！");
                }
            } else {
                System.out.println("❌ 未找到商家ID = " + merchantId + " 的记录");
            }

            return logoUrl;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("商家Logo上传失败: " + e.getMessage());
        }
    }
}
