package cn.yuan.scout.file.service.impl;

import cn.yuan.scout.file.entity.UploadBatchEntity;
import cn.yuan.scout.file.mapper.UploadBatchMapper;
import cn.yuan.scout.file.service.UploadBatchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UploadBatchServiceImpl extends ServiceImpl<UploadBatchMapper, UploadBatchEntity> implements UploadBatchService {
}
