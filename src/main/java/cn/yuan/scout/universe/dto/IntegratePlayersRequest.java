package cn.yuan.scout.universe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IntegratePlayersRequest {

    @NotBlank(message = "赛季不能为空")
    private String seasonName;

    @NotBlank(message = "模型类型不能为空")
    private String templateCode;
}
