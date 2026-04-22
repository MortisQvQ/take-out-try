package com.example.takeouttry.service;

import com.example.takeouttry.DTO.UserQuery;
import com.example.takeouttry.entity.User;
import com.example.takeouttry.mapper.AUsersMapper;
import com.example.takeouttry.security.JwtUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public class AUserServiceImpl implements AUserService {

    private final AUsersMapper ausersMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AUserServiceImpl(AUsersMapper ausersMapper,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil) {
        this.ausersMapper = ausersMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ====================== UserDetailsService ======================
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = ausersMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在:" + username);
        }

        String roleName = switch (user.getRole() != null ? user.getRole() : 1) {
            case 1  -> "ROLE_USER";
            case 2  -> "ROLE_MERCHANT";
            case 9  -> "ROLE_ADMIN";
            default -> "ROLE_USER";
        };

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(roleName)
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                authorities
        );
    }

    // ====================== 你原来的所有业务方法（完整保留） ======================
    @Override
    public List<User> listUsers() {
        return ausersMapper.selectAllUsers();
    }

    @Override
    public User getUserById(Long id) {
        return ausersMapper.selectUserById(id);
    }

    @Override
    public List<User> selectUsersByCondition(UserQuery query) {
        return ausersMapper.selectUsersByCondition(query);
    }

    @Override
    public Long getUserIdByUsername(String username) {
        return ausersMapper.findIdByUsername(username);
    }

    @Override
    @Transactional
    public User addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("用户对象不能为空");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        int rows = ausersMapper.addUser(user);
        if (rows != 1) {
            throw new RuntimeException("新增用户失败，影响行数：" + rows);
        }
        return user;
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("更新用户必须提供 id");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        int rows = ausersMapper.updateUser(user);
        if (rows == 0) {
            throw new RuntimeException("用户不存在，id = " + user.getId());
        }
        if (rows > 1) {
            throw new RuntimeException("更新异常，影响多行：" + rows);
        }
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户 ID 无效");
        }
        int rows = ausersMapper.deleteUser(id);
        if (rows == 0) {
            throw new RuntimeException("用户不存在或已被删除，id = " + id);
        }
        if (rows > 1) {
            throw new RuntimeException("删除异常，影响多行记录");
        }
    }

    @Override
    @Transactional
    public void register(User user) {
        if (ausersMapper.existsByUsername(user.getUsername()) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (ausersMapper.existsByPhone(user.getPhone()) > 0) {
            throw new IllegalArgumentException("手机号已被注册");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int rows = ausersMapper.addUser(user);
        if (rows != 1) {
            throw new RuntimeException("注册失败");
        }
    }

    @Override
    public String login(String username, String rawPassword, String loginType) {
        User user = ausersMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("该账号不存在");
        }

        if ("merchant".equals(loginType)) {
            if (user.getRole() == null || user.getRole() != 2) {
                throw new IllegalArgumentException("无权限登录商家端：您不是商家账号");
            }
        } else if ("user".equals(loginType)) {
            if (user.getRole() != null && user.getRole() == 2) {
                throw new IllegalArgumentException("商家请前往商家登录入口");
            }
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("密码输入错误");
        }

        return jwtUtil.generateToken(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }

        try {
            // ==================== 1. 定义保存目录 ====================
            String projectRoot = System.getProperty("user.dir");
            String uploadDirPath = projectRoot + "/src/main/resources/static/uploads/avatars/";

            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    throw new RuntimeException("无法创建上传目录: " + uploadDirPath);
                }
                System.out.println("创建头像上传目录成功: " + uploadDirPath);
            }

            // ==================== 2. 使用固定文件名（覆盖模式） ====================
            // 一个用户只保留一个头像文件，新上传会直接覆盖旧的
            String filename = "avatar_" + userId + ".jpg";

            // ==================== 3. 保存文件（覆盖同名文件） ====================
            File destFile = new File(uploadDirPath + filename);
            file.transferTo(destFile);

            // ==================== 4. 返回可访问的URL路径 ====================
            String avatarUrl = "/uploads/avatars/" + filename;

            // ==================== 5. 更新数据库 ====================
            User user = ausersMapper.selectUserById(userId);
            if (user != null) {
                user.setAvatarUrl(avatarUrl);
                ausersMapper.updateUser(user);
                System.out.println("数据库头像路径已更新为: " + avatarUrl);
            }

            System.out.println("头像覆盖保存成功！用户ID: " + userId + "，文件名: " + filename);
            return avatarUrl;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
    }
}