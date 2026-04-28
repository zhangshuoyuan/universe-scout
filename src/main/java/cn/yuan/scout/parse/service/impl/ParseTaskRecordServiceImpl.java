package cn.yuan.scout.parse.service.impl;

import cn.yuan.scout.parse.entity.ParseTaskEntity;
import cn.yuan.scout.parse.mapper.ParseTaskMapper;
import cn.yuan.scout.parse.service.ParseTaskRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ParseTaskRecordServiceImpl extends ServiceImpl<ParseTaskMapper, ParseTaskEntity> implements ParseTaskRecordService {
}
