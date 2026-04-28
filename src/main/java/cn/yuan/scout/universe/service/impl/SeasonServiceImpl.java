package cn.yuan.scout.universe.service.impl;

import cn.yuan.scout.universe.entity.SeasonEntity;
import cn.yuan.scout.universe.mapper.SeasonMapper;
import cn.yuan.scout.universe.service.SeasonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SeasonServiceImpl extends ServiceImpl<SeasonMapper, SeasonEntity> implements SeasonService {
}
