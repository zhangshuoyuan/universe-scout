USE universe_scout;

ALTER TABLE player
    ADD COLUMN avatar_path VARCHAR(512) DEFAULT NULL COMMENT '球员头像本地路径' AFTER player_name_cn,
    ADD COLUMN season_id BIGINT DEFAULT NULL COMMENT '所属赛季ID' AFTER avatar_path,
    ADD COLUMN team_id BIGINT DEFAULT NULL COMMENT '所属球队ID' AFTER season_id,
    ADD COLUMN source_result_id BIGINT DEFAULT NULL COMMENT '来源视觉解析结果ID' AFTER team_id,
    ADD COLUMN lineup_position VARCHAR(16) DEFAULT NULL COMMENT '首发位置：PG、SG、SF、PF、C' AFTER source_result_id,
    ADD COLUMN roster_status VARCHAR(16) NOT NULL DEFAULT 'CURRENT' COMMENT '名单状态：CURRENT当前，HISTORY历史' AFTER lineup_position,
    ADD COLUMN data_json JSON DEFAULT NULL COMMENT '球员解析原始数据' AFTER roster_status,
    ADD KEY idx_player_season_team (season_id, team_id),
    ADD KEY idx_player_roster_status (roster_status),
    ADD KEY idx_player_source_result (source_result_id),
    ADD UNIQUE KEY uk_player_season_team_name (season_id, team_id, player_name);

ALTER TABLE team
    MODIFY team_code VARCHAR(64) NOT NULL COMMENT '球队编码，如 HOU、DAL';
