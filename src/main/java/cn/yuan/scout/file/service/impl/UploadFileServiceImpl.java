package cn.yuan.scout.file.service.impl;

import cn.yuan.scout.file.entity.UploadFileEntity;
import cn.yuan.scout.file.mapper.UploadFileMapper;
import cn.yuan.scout.file.service.UploadFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UploadFileServiceImpl extends ServiceImpl<UploadFileMapper, UploadFileEntity> implements UploadFileService {
}
