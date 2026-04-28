package cn.yuan.scout.universe.controller;

import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.common.result.Result;
import cn.yuan.scout.universe.dto.IntegratePlayersRequest;
import cn.yuan.scout.universe.dto.IntegratePlayersResponse;
import cn.yuan.scout.universe.dto.PlayerListItemResponse;
import cn.yuan.scout.universe.dto.PlayerProfileResponse;
import cn.yuan.scout.universe.dto.SeasonResponse;
import cn.yuan.scout.universe.dto.TeamResponse;
import cn.yuan.scout.universe.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/seasons")
    public Result<List<SeasonResponse>> listSeasons() {
        return Result.success(playerService.listSeasons());
    }

    @GetMapping("/teams")
    public Result<List<TeamResponse>> listTeams(@RequestParam(required = false) Long seasonId) {
        return Result.success(playerService.listTeams(seasonId));
    }

    @GetMapping
    public Result<PageResult<PlayerListItemResponse>> pagePlayers(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "12") Long pageSize,
            @RequestParam(required = false) Long seasonId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String rosterStatus,
            @RequestParam(required = false) String position
    ) {
        return Result.success(playerService.pagePlayers(pageNum, pageSize, seasonId, teamId, keyword, rosterStatus, position));
    }

    @GetMapping("/profiles")
    public Result<PageResult<PlayerProfileResponse>> pageProfiles(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "12") Long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(playerService.pageProfiles(pageNum, pageSize, keyword));
    }

    @PostMapping("/integrate")
    public Result<IntegratePlayersResponse> integratePlayers(@Valid @RequestBody IntegratePlayersRequest request) {
        return Result.success(playerService.integratePlayers(request));
    }

    @PostMapping("/profiles/{profileId}/avatar")
    public Result<String> uploadAvatar(@PathVariable Long profileId, @RequestPart("file") MultipartFile file) {
        return Result.success(playerService.uploadAvatar(profileId, file));
    }

    @GetMapping("/profiles/{profileId}/avatar")
    public ResponseEntity<Resource> previewAvatar(@PathVariable Long profileId) {
        return playerService.previewAvatar(profileId);
    }
}
