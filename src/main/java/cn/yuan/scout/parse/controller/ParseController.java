package cn.yuan.scout.parse.controller;

import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.common.result.Result;
import cn.yuan.scout.parse.dto.CreateParseTaskRequest;
import cn.yuan.scout.parse.dto.CreateParseTaskResponse;
import cn.yuan.scout.parse.dto.ParseTaskDetailResponse;
import cn.yuan.scout.parse.dto.ParseTaskListItemResponse;
import cn.yuan.scout.parse.dto.ParseTemplateResponse;
import cn.yuan.scout.parse.service.ParseTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/parse")
@RequiredArgsConstructor
public class ParseController {

    private final ParseTaskService parseTaskService;

    @GetMapping("/templates")
    public Result<List<ParseTemplateResponse>> listTemplates() {
        return Result.success(parseTaskService.listEnabledTemplates());
    }

    @PostMapping("/tasks")
    public Result<CreateParseTaskResponse> createTask(@Valid @RequestBody CreateParseTaskRequest request) {
        return Result.success(parseTaskService.createTask(request));
    }

    @GetMapping("/tasks")
    public Result<PageResult<ParseTaskListItemResponse>> pageTasks(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) String status
    ) {
        return Result.success(parseTaskService.pageTasks(pageNum, pageSize, templateCode, status));
    }

    @GetMapping("/tasks/{taskId}")
    public Result<ParseTaskDetailResponse> getTaskDetail(@PathVariable Long taskId) {
        return Result.success(parseTaskService.getTaskDetail(taskId));
    }

    @PostMapping("/tasks/{taskId}/start")
    public Result<Void> startTask(@PathVariable Long taskId) {
        parseTaskService.startTask(taskId);
        return Result.success();
    }
}
