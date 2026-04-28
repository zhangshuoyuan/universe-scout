package cn.yuan.scout.universe.service.impl;

import cn.yuan.scout.universe.entity.PlayerEntity;
import cn.yuan.scout.universe.mapper.PlayerMapper;
import cn.yuan.scout.universe.service.PlayerRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PlayerRecordServiceImpl extends ServiceImpl<PlayerMapper, PlayerEntity> implements PlayerRecordService {
}
