package cn.yuan.scout.ai.client;

import cn.yuan.scout.ai.config.VisionModelProperties;
import cn.yuan.scout.ai.dto.VisionModelRequest;
import cn.yuan.scout.ai.dto.VisionModelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "vision.model", name = "provider", havingValue = "mock")
@RequiredArgsConstructor
public class MockVisionModelClient implements VisionModelClient {

    private final VisionModelProperties properties;

    @Override
    public VisionModelResponse parseImage(VisionModelRequest request) {
        String json = """
                {
                  "type": "LINEUP",
                  "season": null,
                  "snapshotDate": null,
                  "teams": [
                    {
                      "teamName": "Rockets",
                      "teamNameCn": "火箭",
                      "lineup": [
                        {"position": "PG", "playerName": "Damian Lillard", "playerNameCn": "达米安·利拉德"},
                        {"position": "SG", "playerName": "Victor Oladipo", "playerNameCn": "维克托·奥拉迪波"},
                        {"position": "SF", "playerName": "Mikal Bridges", "playerNameCn": "米卡尔·布里奇斯"},
                        {"position": "PF", "playerName": "Danilo Gallinari", "playerNameCn": "达尼罗·加里纳利"},
                        {"position": "C", "playerName": "Karl-Anthony Towns", "playerNameCn": "卡尔-安东尼·唐斯"}
                      ]
                    }
                  ]
                }
                """;
        return VisionModelResponse.builder()
                .modelName(properties.getModelName())
                .responseRaw(json)
                .build();
    }
}
