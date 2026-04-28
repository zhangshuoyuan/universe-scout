package cn.yuan.scout.universe.service.impl;

import cn.yuan.scout.universe.entity.PlayerProfileEntity;
import cn.yuan.scout.universe.mapper.PlayerProfileMapper;
import cn.yuan.scout.universe.service.PlayerProfileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PlayerProfileServiceImpl extends ServiceImpl<PlayerProfileMapper, PlayerProfileEntity> implements PlayerProfileService {
}
