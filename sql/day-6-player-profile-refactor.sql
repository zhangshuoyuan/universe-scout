USE universe_scout;

CREATE TABLE IF NOT EXISTS player_profile (
    id BIGINT PRIMARY KEY COMMENT 'player profile id',
    player_name VARCHAR(128) NOT NULL COMMENT 'english player name',
    player_name_cn VARCHAR(128) DEFAULT NULL COMMENT 'chinese player name',
    avatar_path VARCHAR(512) DEFAULT NULL COMMENT 'local avatar path',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'logic delete flag',
    UNIQUE KEY uk_player_profile_name (player_name, player_name_cn),
    KEY idx_profile_player_name (player_name),
    KEY idx_profile_player_name_cn (player_name_cn)
) COMMENT='player profile and avatar table';

INSERT INTO player_profile (
    id, player_name, player_name_cn, avatar_path, created_at, updated_at, deleted
)
SELECT
    MIN(id) AS id,
    player_name,
    player_name_cn,
    MAX(avatar_path) AS avatar_path,
    MIN(created_at) AS created_at,
    MAX(updated_at) AS updated_at,
    0 AS deleted
FROM player
WHERE deleted = 0
  AND player_name IS NOT NULL
GROUP BY player_name, player_name_cn
ON DUPLICATE KEY UPDATE
    avatar_path = COALESCE(player_profile.avatar_path, VALUES(avatar_path)),
    updated_at = CURRENT_TIMESTAMP;

ALTER TABLE player
    ADD COLUMN player_profile_id BIGINT DEFAULT NULL COMMENT 'player profile id' AFTER id;

UPDATE player p
JOIN player_profile pp
  ON pp.player_name = p.player_name
 AND (pp.player_name_cn <=> p.player_name_cn)
SET p.player_profile_id = pp.id
WHERE p.player_profile_id IS NULL;

ALTER TABLE player
    DROP INDEX idx_player_name,
    DROP INDEX uk_player_season_team_name,
    DROP COLUMN player_name,
    DROP COLUMN player_name_cn,
    DROP COLUMN avatar_path,
    ADD KEY idx_player_profile_id (player_profile_id),
    ADD KEY idx_player_position (position),
    ADD UNIQUE KEY uk_player_season_team_profile (season_id, team_id, player_profile_id);
