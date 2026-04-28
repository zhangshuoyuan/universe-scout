package cn.yuan.scout.universe.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.yuan.scout.common.exception.BizException;
import cn.yuan.scout.common.exception.ErrorCode;
import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.file.config.UniverseFileProperties;
import cn.yuan.scout.parse.entity.VisionParseResultEntity;
import cn.yuan.scout.parse.service.VisionParseResultService;
import cn.yuan.scout.universe.dto.IntegratePlayersRequest;
import cn.yuan.scout.universe.dto.IntegratePlayersResponse;
import cn.yuan.scout.universe.dto.PlayerListItemResponse;
import cn.yuan.scout.universe.dto.SeasonResponse;
import cn.yuan.scout.universe.dto.TeamResponse;
import cn.yuan.scout.universe.entity.PlayerEntity;
import cn.yuan.scout.universe.entity.SeasonEntity;
import cn.yuan.scout.universe.entity.TeamEntity;
import cn.yuan.scout.universe.service.PlayerRecordService;
import cn.yuan.scout.universe.service.PlayerService;
import cn.yuan.scout.universe.service.SeasonService;
import cn.yuan.scout.universe.service.TeamService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Pattern SEASON_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})$");
    private static final Map<String, String> NBA_TEAM_CN_NAMES = Map.ofEntries(
            Map.entry("76ers", "76人"),
            Map.entry("Bucks", "雄鹿"),
            Map.entry("Bulls", "公牛"),
            Map.entry("Cavaliers", "骑士"),
            Map.entry("Celtics", "凯尔特人"),
            Map.entry("Clippers", "快船"),
            Map.entry("Grizzlies", "灰熊"),
            Map.entry("Hawks", "老鹰"),
            Map.entry("Heat", "热火"),
            Map.entry("Hornets", "黄蜂"),
            Map.entry("Jazz", "爵士"),
            Map.entry("Kings", "国王"),
            Map.entry("Knicks", "尼克斯"),
            Map.entry("Lakers", "湖人"),
            Map.entry("Magic", "魔术"),
            Map.entry("Mavericks", "独行侠"),
            Map.entry("Nets", "篮网"),
            Map.entry("Nuggets", "掘金"),
            Map.entry("Pacers", "步行者"),
            Map.entry("Pelicans", "鹈鹕"),
            Map.entry("Pistons", "活塞"),
            Map.entry("Raptors", "猛龙"),
            Map.entry("Rockets", "火箭"),
            Map.entry("Spurs", "马刺"),
            Map.entry("Suns", "太阳"),
            Map.entry("Thunder", "雷霆"),
            Map.entry("Timberwolves", "森林狼"),
            Map.entry("Trail Blazers", "开拓者"),
            Map.entry("Warriors", "勇士"),
            Map.entry("Wizards", "奇才")
    );

    private final SeasonService seasonService;
    private final TeamService teamService;
    private final PlayerRecordService playerRecordService;
    private final VisionParseResultService visionParseResultService;
    private final UniverseFileProperties fileProperties;
    private final ObjectMapper objectMapper;

    @Override
    public List<SeasonResponse> listSeasons() {
        return seasonService.list(
                        new LambdaQueryWrapper<SeasonEntity>()
                                .eq(SeasonEntity::getDeleted, 0)
                                .orderByDesc(SeasonEntity::getStartYear)
                                .orderByDesc(SeasonEntity::getCreatedAt)
                )
                .stream()
                .map(this::toSeasonResponse)
                .toList();
    }

    @Override
    public List<TeamResponse> listTeams(Long seasonId) {
        List<Long> teamIds = null;
        if (seasonId != null) {
            teamIds = playerRecordService.list(
                            new LambdaQueryWrapper<PlayerEntity>()
                                    .select(PlayerEntity::getTeamId)
                                    .eq(PlayerEntity::getDeleted, 0)
                                    .eq(PlayerEntity::getSeasonId, seasonId)
                                    .eq(PlayerEntity::getRosterStatus, "CURRENT")
                                    .isNotNull(PlayerEntity::getTeamId)
                                    .groupBy(PlayerEntity::getTeamId)
                    )
                    .stream()
                    .map(PlayerEntity::getTeamId)
                    .toList();
            if (teamIds.isEmpty()) {
                return List.of();
            }
        }

        return teamService.list(
                        new LambdaQueryWrapper<TeamEntity>()
                                .eq(TeamEntity::getDeleted, 0)
                                .in(teamIds != null, TeamEntity::getId, teamIds)
                                .orderByAsc(TeamEntity::getTeamNameEn)
                )
                .stream()
                .map(this::toTeamResponse)
                .toList();
    }

    @Override
    public PageResult<PlayerListItemResponse> pagePlayers(
            Long pageNum,
            Long pageSize,
            Long seasonId,
            Long teamId,
            String keyword,
            String rosterStatus
    ) {
        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 12 : Math.min(pageSize, 100);

        LambdaQueryWrapper<PlayerEntity> wrapper = new LambdaQueryWrapper<PlayerEntity>()
                .eq(PlayerEntity::getDeleted, 0)
                .eq(seasonId != null, PlayerEntity::getSeasonId, seasonId)
                .eq(teamId != null, PlayerEntity::getTeamId, teamId)
                .eq(StringUtils.hasText(rosterStatus), PlayerEntity::getRosterStatus, rosterStatus)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(PlayerEntity::getPlayerName, keyword)
                        .or()
                        .like(PlayerEntity::getPlayerNameCn, keyword)
                )
                .orderByAsc(PlayerEntity::getRosterStatus)
                .orderByAsc(PlayerEntity::getTeamId)
                .orderByAsc(PlayerEntity::getLineupPosition)
                .orderByAsc(PlayerEntity::getPlayerName);

        Page<PlayerEntity> page = playerRecordService.page(new Page<>(safePageNum, safePageSize), wrapper);
        List<PlayerListItemResponse> records = page.getRecords()
                .stream()
                .map(this::toPlayerResponse)
                .toList();
        return PageResult.of(records, page.getTotal(), safePageNum, safePageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IntegratePlayersResponse integratePlayers(IntegratePlayersRequest request) {
        String templateCode = request.getTemplateCode().trim().toUpperCase(Locale.ROOT);
        SeasonEntity season = getOrCreateSeason(request.getSeasonName().trim());
        List<VisionParseResultEntity> results = visionParseResultService.list(
                new LambdaQueryWrapper<VisionParseResultEntity>()
                        .eq(VisionParseResultEntity::getDeleted, 0)
                        .eq(VisionParseResultEntity::getTemplateCode, templateCode)
                        .eq(VisionParseResultEntity::getStatus, "SUCCESS")
                        .isNotNull(VisionParseResultEntity::getResponseJson)
                        .orderByAsc(VisionParseResultEntity::getCreatedAt)
        );

        int createdCount = 0;
        int updatedCount = 0;
        int historyCount = 0;
        int skippedCount = 0;
        Set<String> touched = new HashSet<>();

        for (VisionParseResultEntity result : results) {
            try {
                JsonNode root = objectMapper.readTree(result.getResponseJson());
                JsonNode teams = root.path("teams");
                if (!teams.isArray()) {
                    skippedCount++;
                    continue;
                }
                for (JsonNode teamNode : teams) {
                    String teamName = firstText(teamNode, "teamName", "teamNameEn", "teamEnglishName");
                    String teamNameCn = firstText(teamNode, "teamNameCn", "teamNameZh", "teamChineseName", "teamCnName");
                    if (!StringUtils.hasText(teamName) && !StringUtils.hasText(teamNameCn)) {
                        skippedCount++;
                        continue;
                    }
                    TeamEntity team = getOrCreateTeam(trimToNull(teamName), trimToNull(teamNameCn));
                    JsonNode lineup = teamNode.path("lineup");
                    if (!lineup.isArray()) {
                        skippedCount++;
                        continue;
                    }
                    for (JsonNode playerNode : lineup) {
                        IntegrateOneResult one = integrateOnePlayer(season, team, result, playerNode);
                        createdCount += one.created ? 1 : 0;
                        updatedCount += one.updated ? 1 : 0;
                        historyCount += one.historyCount;
                        skippedCount += one.skipped ? 1 : 0;
                        if (one.touchedKey != null) {
                            touched.add(one.touchedKey);
                        }
                    }
                }
            } catch (Exception e) {
                skippedCount++;
            }
        }

        return IntegratePlayersResponse.builder()
                .seasonId(season.getId())
                .seasonName(season.getSeasonName())
                .templateCode(templateCode)
                .resultCount(results.size())
                .createdCount(createdCount)
                .updatedCount(updatedCount)
                .historyCount(historyCount)
                .skippedCount(skippedCount)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long playerId, MultipartFile file) {
        PlayerEntity player = getPlayer(playerId);
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "请选择头像文件");
        }
        String extension = extension(file.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "头像仅支持 jpg、jpeg、png、webp");
        }

        Path directory = Path.of(fileProperties.getUploadPath()).resolve("avatars");
        createDirectories(directory);
        String storedName = IdUtil.fastSimpleUUID() + "." + extension;
        Path target = directory.resolve(storedName);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "头像保存失败", e);
        }

        player.setAvatarPath(target.toAbsolutePath().toString());
        playerRecordService.updateById(player);
        return avatarUrl(player.getId(), player.getAvatarPath());
    }

    @Override
    public ResponseEntity<Resource> previewAvatar(Long playerId) {
        PlayerEntity player = getPlayer(playerId);
        if (!StringUtils.hasText(player.getAvatarPath())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "球员暂无头像");
        }
        Path path = Path.of(player.getAvatarPath()).normalize();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "头像文件不存在");
        }
        return ResponseEntity.ok()
                .contentType(mediaType(extension(path.getFileName().toString())))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(new FileSystemResource(path));
    }

    private IntegrateOneResult integrateOnePlayer(
            SeasonEntity season,
            TeamEntity team,
            VisionParseResultEntity result,
            JsonNode playerNode
    ) throws Exception {
        String playerName = firstText(playerNode, "playerName", "playerNameEn", "playerEnglishName");
        String playerNameCn = firstText(playerNode, "playerNameCn", "playerNameZh", "playerChineseName", "playerCnName");
        if (!StringUtils.hasText(playerName) && StringUtils.hasText(playerNameCn)) {
            playerName = playerNameCn;
        }
        if (!StringUtils.hasText(playerName) || "null".equalsIgnoreCase(playerName.trim())) {
            return IntegrateOneResult.skipped();
        }

        String cleanPlayerName = playerName.trim();
        String cleanPlayerNameCn = trimToNull(playerNameCn);
        String position = text(playerNode, "position");
        String dataJson = objectMapper.writeValueAsString(playerNode);

        int historyCount = markOldTeamsAsHistory(season.getId(), team.getId(), cleanPlayerName);
        PlayerEntity existing = playerRecordService.getOne(
                new LambdaQueryWrapper<PlayerEntity>()
                        .eq(PlayerEntity::getDeleted, 0)
                        .eq(PlayerEntity::getSeasonId, season.getId())
                        .eq(PlayerEntity::getTeamId, team.getId())
                        .eq(PlayerEntity::getPlayerName, cleanPlayerName)
                        .last("limit 1")
        );

        if (existing == null) {
            PlayerEntity player = new PlayerEntity();
            player.setSeasonId(season.getId());
            player.setTeamId(team.getId());
            player.setPlayerName(cleanPlayerName);
            player.setPlayerNameCn(cleanPlayerNameCn);
            player.setLineupPosition(position);
            player.setPosition(position);
            player.setSourceResultId(result.getId());
            player.setRosterStatus("CURRENT");
            player.setDataJson(dataJson);
            player.setDeleted(0);
            playerRecordService.save(player);
            return IntegrateOneResult.created(historyCount, season.getId() + ":" + team.getId() + ":" + cleanPlayerName);
        }

        existing.setLineupPosition(position);
        existing.setPosition(position);
        if (StringUtils.hasText(cleanPlayerNameCn)) {
            existing.setPlayerNameCn(cleanPlayerNameCn);
        }
        existing.setSourceResultId(result.getId());
        existing.setRosterStatus("CURRENT");
        existing.setDataJson(dataJson);
        playerRecordService.updateById(existing);
        return IntegrateOneResult.updated(historyCount, season.getId() + ":" + team.getId() + ":" + cleanPlayerName);
    }

    private int markOldTeamsAsHistory(Long seasonId, Long currentTeamId, String playerName) {
        List<PlayerEntity> oldRecords = playerRecordService.list(
                new LambdaQueryWrapper<PlayerEntity>()
                        .eq(PlayerEntity::getDeleted, 0)
                        .eq(PlayerEntity::getSeasonId, seasonId)
                        .ne(PlayerEntity::getTeamId, currentTeamId)
                        .eq(PlayerEntity::getPlayerName, playerName)
                        .eq(PlayerEntity::getRosterStatus, "CURRENT")
        );
        if (oldRecords.isEmpty()) {
            return 0;
        }
        playerRecordService.update(
                new LambdaUpdateWrapper<PlayerEntity>()
                        .eq(PlayerEntity::getDeleted, 0)
                        .eq(PlayerEntity::getSeasonId, seasonId)
                        .ne(PlayerEntity::getTeamId, currentTeamId)
                        .eq(PlayerEntity::getPlayerName, playerName)
                        .eq(PlayerEntity::getRosterStatus, "CURRENT")
                        .set(PlayerEntity::getRosterStatus, "HISTORY")
        );
        return oldRecords.size();
    }

    private SeasonEntity getOrCreateSeason(String seasonName) {
        SeasonEntity season = seasonService.getOne(
                new LambdaQueryWrapper<SeasonEntity>()
                        .eq(SeasonEntity::getDeleted, 0)
                        .eq(SeasonEntity::getSeasonName, seasonName)
                        .last("limit 1")
        );
        if (season != null) {
            return season;
        }

        int startYear = LocalDate.now().getYear();
        int endYear = startYear + 1;
        Matcher matcher = SEASON_PATTERN.matcher(seasonName);
        if (matcher.matches()) {
            startYear = Integer.parseInt(matcher.group(1));
            endYear = Integer.parseInt(String.valueOf(startYear).substring(0, 2) + matcher.group(2));
        }

        season = new SeasonEntity();
        season.setSeasonName(seasonName);
        season.setStartYear(startYear);
        season.setEndYear(endYear);
        season.setDescription("解析数据自动创建");
        season.setStatus(1);
        season.setDeleted(0);
        seasonService.save(season);
        return season;
    }

    private TeamEntity getOrCreateTeam(String teamName, String teamNameCn) {
        String cleanTeamName = StringUtils.hasText(teamName) ? teamName : teamNameCn;
        String cleanTeamNameCn = StringUtils.hasText(teamNameCn) ? teamNameCn : NBA_TEAM_CN_NAMES.get(cleanTeamName);
        TeamEntity team = findTeam(cleanTeamName, cleanTeamNameCn);
        if (team != null) {
            if (!StringUtils.hasText(team.getTeamNameCn()) && StringUtils.hasText(cleanTeamNameCn)) {
                team.setTeamNameCn(cleanTeamNameCn);
                teamService.updateById(team);
            }
            return team;
        }

        team = new TeamEntity();
        team.setTeamNameEn(cleanTeamName);
        team.setTeamNameCn(cleanTeamNameCn);
        team.setTeamCode(generateTeamCode(cleanTeamName));
        team.setDeleted(0);
        teamService.save(team);
        return team;
    }

    private TeamEntity findTeam(String teamName, String teamNameCn) {
        LambdaQueryWrapper<TeamEntity> wrapper = new LambdaQueryWrapper<TeamEntity>()
                .eq(TeamEntity::getDeleted, 0)
                .and(query -> {
                    if (StringUtils.hasText(teamName)) {
                        query.eq(TeamEntity::getTeamNameEn, teamName);
                    }
                    if (StringUtils.hasText(teamNameCn)) {
                        if (StringUtils.hasText(teamName)) {
                            query.or();
                        }
                        query.eq(TeamEntity::getTeamNameCn, teamNameCn);
                    }
                })
                .last("limit 1");
        return teamService.getOne(wrapper);
    }

    private String generateTeamCode(String teamName) {
        String base = teamName.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (base.length() > 12) {
            base = base.substring(0, 12);
        }
        if (!StringUtils.hasText(base)) {
            base = "TEAM";
        }
        String teamCode = base;
        int index = 1;
        while (teamService.count(new LambdaQueryWrapper<TeamEntity>().eq(TeamEntity::getTeamCode, teamCode)) > 0) {
            teamCode = base + "_" + index++;
        }
        return teamCode;
    }

    private PlayerEntity getPlayer(Long playerId) {
        PlayerEntity player = playerRecordService.getOne(
                new LambdaQueryWrapper<PlayerEntity>()
                        .eq(PlayerEntity::getId, playerId)
                        .eq(PlayerEntity::getDeleted, 0)
                        .last("limit 1")
        );
        if (player == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "球员不存在");
        }
        return player;
    }

    private PlayerListItemResponse toPlayerResponse(PlayerEntity player) {
        SeasonEntity season = player.getSeasonId() == null ? null : seasonService.getById(player.getSeasonId());
        TeamEntity team = player.getTeamId() == null ? null : teamService.getById(player.getTeamId());
        return PlayerListItemResponse.builder()
                .playerId(player.getId())
                .playerName(player.getPlayerName())
                .playerNameCn(player.getPlayerNameCn())
                .avatarUrl(avatarUrl(player.getId(), player.getAvatarPath()))
                .seasonId(player.getSeasonId())
                .seasonName(season == null ? null : season.getSeasonName())
                .teamId(player.getTeamId())
                .teamName(teamName(team))
                .teamNameEn(team == null ? null : team.getTeamNameEn())
                .teamNameCn(teamNameCn(team))
                .lineupPosition(player.getLineupPosition())
                .position(player.getPosition())
                .overallRating(player.getOverallRating())
                .potentialRating(player.getPotentialRating())
                .rosterStatus(player.getRosterStatus())
                .dataJson(player.getDataJson())
                .remark(player.getRemark())
                .updatedAt(player.getUpdatedAt())
                .build();
    }

    private SeasonResponse toSeasonResponse(SeasonEntity season) {
        return SeasonResponse.builder()
                .id(season.getId())
                .seasonName(season.getSeasonName())
                .startYear(season.getStartYear())
                .endYear(season.getEndYear())
                .status(season.getStatus())
                .build();
    }

    private TeamResponse toTeamResponse(TeamEntity team) {
        return TeamResponse.builder()
                .id(team.getId())
                .teamCode(team.getTeamCode())
                .teamNameEn(team.getTeamNameEn())
                .teamNameCn(teamNameCn(team))
                .build();
    }

    private String teamName(TeamEntity team) {
        if (team == null) {
            return null;
        }
        return StringUtils.hasText(team.getTeamNameCn()) ? team.getTeamNameCn() : team.getTeamNameEn();
    }

    private String teamNameCn(TeamEntity team) {
        if (team == null) {
            return null;
        }
        if (StringUtils.hasText(team.getTeamNameCn())) {
            return team.getTeamNameCn();
        }
        return NBA_TEAM_CN_NAMES.get(team.getTeamNameEn());
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (StringUtils.hasText(value) && !"null".equalsIgnoreCase(value.trim())) {
                return value.trim();
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value) || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }

    private String avatarUrl(Long playerId, String avatarPath) {
        return StringUtils.hasText(avatarPath) ? "/api/players/" + playerId + "/avatar" : null;
    }

    private String extension(String filename) {
        String cleanName = filename == null ? "" : Path.of(filename).getFileName().toString();
        int index = cleanName.lastIndexOf('.');
        if (index < 0 || index == cleanName.length() - 1) {
            return "";
        }
        return cleanName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private MediaType mediaType(String extension) {
        if ("png".equals(extension)) {
            return MediaType.IMAGE_PNG;
        }
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return MediaType.IMAGE_JPEG;
        }
        if ("webp".equals(extension)) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "创建头像目录失败", e);
        }
    }

    private static class IntegrateOneResult {

        private final boolean created;
        private final boolean updated;
        private final boolean skipped;
        private final int historyCount;
        private final String touchedKey;

        private IntegrateOneResult(boolean created, boolean updated, boolean skipped, int historyCount, String touchedKey) {
            this.created = created;
            this.updated = updated;
            this.skipped = skipped;
            this.historyCount = historyCount;
            this.touchedKey = touchedKey;
        }

        private static IntegrateOneResult created(int historyCount, String touchedKey) {
            return new IntegrateOneResult(true, false, false, historyCount, touchedKey);
        }

        private static IntegrateOneResult updated(int historyCount, String touchedKey) {
            return new IntegrateOneResult(false, true, false, historyCount, touchedKey);
        }

        private static IntegrateOneResult skipped() {
            return new IntegrateOneResult(false, false, true, 0, null);
        }
    }
}
