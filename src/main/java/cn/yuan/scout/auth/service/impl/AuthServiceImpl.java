package cn.yuan.scout.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.yuan.scout.auth.dto.LoginRequest;
import cn.yuan.scout.auth.dto.LoginResponse;
import cn.yuan.scout.auth.entity.AdminUser;
import cn.yuan.scout.auth.mapper.AdminUserMapper;
import cn.yuan.scout.auth.service.AuthService;
import cn.yuan.scout.common.exception.BizException;
import cn.yuan.scout.common.exception.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminUserMapper adminUserMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, request.getUsername())
                        .eq(AdminUser::getDeleted, 0)
                        .last("limit 1")
        );

        if (user == null) {
            throw new BizException(ErrorCode.FORBIDDEN, "username or password is incorrect");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BizException(ErrorCode.FORBIDDEN, "username or password is incorrect");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ErrorCode.FORBIDDEN, "account is disabled");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .roleCode(user.getRoleCode())
                .build();
    }
}
