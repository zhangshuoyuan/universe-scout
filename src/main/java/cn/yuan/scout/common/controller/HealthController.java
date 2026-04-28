package cn.yuan.scout.common.controller;

import cn.yuan.scout.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: universe-scout
 * @BelongsPackage: cn.yuan.scout.common.controller
 * @Author: zsy
 * @CreateTime: 2026-04-28  21:10
 * @Description: 健康检查接口
 * @Version: 1.0
 */
@RestController
public class HealthController {
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Universe Scout server is running");
    }
}
