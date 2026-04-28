package cn.yuan.scout.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @BelongsProject: universe-scout
 * @BelongsPackage: cn.yuan.scout.auth.entity
 * @Author: zsy
 * @CreateTime: 2026-04-28  21:12
 * @Description: 用户实体
 * @Version: 1.0
 */
@Data
@TableName("admin_user")
public class AdminUser {
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String roleCode;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}
