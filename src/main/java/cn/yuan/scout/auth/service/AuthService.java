package cn.yuan.scout.auth.service;

import cn.yuan.scout.auth.dto.LoginRequest;
import cn.yuan.scout.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}