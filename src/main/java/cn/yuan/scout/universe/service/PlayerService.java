package cn.yuan.scout.universe.service;

import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.universe.dto.IntegratePlayersRequest;
import cn.yuan.scout.universe.dto.IntegratePlayersResponse;
import cn.yuan.scout.universe.dto.PlayerListItemResponse;
import cn.yuan.scout.universe.dto.PlayerProfileResponse;
import cn.yuan.scout.universe.dto.SeasonResponse;
import cn.yuan.scout.universe.dto.TeamResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlayerService {

    List<SeasonResponse> listSeasons();

    List<TeamResponse> listTeams(Long seasonId);

    PageResult<PlayerListItemResponse> pagePlayers(Long pageNum, Long pageSize, Long seasonId, Long teamId, String keyword, String rosterStatus, String position);

    PageResult<PlayerProfileResponse> pageProfiles(Long pageNum, Long pageSize, String keyword);

    IntegratePlayersResponse integratePlayers(IntegratePlayersRequest request);

    String uploadAvatar(Long profileId, MultipartFile file);

    ResponseEntity<Resource> previewAvatar(Long profileId);
}
