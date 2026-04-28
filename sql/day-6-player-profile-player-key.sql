USE universe_scout;

ALTER TABLE player_profile
    ADD COLUMN player_key VARCHAR(160) DEFAULT NULL COMMENT 'player english name plus jersey number key' AFTER id,
    ADD UNIQUE KEY uk_player_profile_key (player_key);

UPDATE player_profile
SET player_key = player_name
WHERE player_key IS NULL;
