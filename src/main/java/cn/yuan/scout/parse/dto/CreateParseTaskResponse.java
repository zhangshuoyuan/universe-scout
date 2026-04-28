package cn.yuan.scout.parse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateParseTaskResponse {

    private Long taskId;

    private String taskNo;

    private String status;
}
