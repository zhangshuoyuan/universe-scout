package cn.yuan.scout;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @BelongsProject: universe-scout
 * @BelongsPackage: cn.yuan.scout
 * @Author: zsy
 * @CreateTime: 2026-04-28  21:05
 * @Description: 启动类
 * @Version: 1.0
 */
@MapperScan("cn.yuan.scout.**.mapper")
@SpringBootApplication
public class UniverseScoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(UniverseScoutApplication.class,args);
    }
}
