package cn.yuan.scout.parse.service;

import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.parse.dto.CreateParseTaskRequest;
import cn.yuan.scout.parse.dto.CreateParseTaskResponse;
import cn.yuan.scout.parse.dto.ParseTaskDetailResponse;
import cn.yuan.scout.parse.dto.ParseTaskListItemResponse;
import cn.yuan.scout.parse.dto.ParseTemplateResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ParseTaskService {

    List<ParseTemplateResponse> listEnabledTemplates();

    CreateParseTaskResponse createTask(CreateParseTaskRequest request);

    PageResult<ParseTaskListItemResponse> pageTasks(Long pageNum, Long pageSize, String templateCode, String status);

    ParseTaskDetailResponse getTaskDetail(Long taskId);

    void startTask(Long taskId);

    ResponseEntity<Resource> exportFailedImages(Long taskId);
}
