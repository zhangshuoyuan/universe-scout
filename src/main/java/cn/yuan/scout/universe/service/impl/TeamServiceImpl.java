package cn.yuan.scout.universe.service.impl;

import cn.yuan.scout.universe.entity.TeamEntity;
import cn.yuan.scout.universe.mapper.TeamMapper;
import cn.yuan.scout.universe.service.TeamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, TeamEntity> implements TeamService {
}
