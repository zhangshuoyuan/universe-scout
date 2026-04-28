package cn.yuan.scout.parse.service.impl;

import cn.yuan.scout.parse.entity.ParseTemplateEntity;
import cn.yuan.scout.parse.mapper.ParseTemplateMapper;
import cn.yuan.scout.parse.service.ParseTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ParseTemplateServiceImpl extends ServiceImpl<ParseTemplateMapper, ParseTemplateEntity> implements ParseTemplateService {
}
