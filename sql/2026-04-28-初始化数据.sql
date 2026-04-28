USE universe_scout;

INSERT INTO admin_user (
    id, username, password, nickname, role_code, status
) VALUES (
    1, 'admin', 'admin123456', 'Super Admin', 'SUPER_ADMIN', 1
) ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    nickname = VALUES(nickname),
    role_code = VALUES(role_code),
    status = VALUES(status),
    deleted = 0;

INSERT INTO season (
    id, season_name, start_year, end_year, description, status
) VALUES (
    1, '2021-22', 2021, 2022, 'Test season', 1
) ON DUPLICATE KEY UPDATE
    season_name = VALUES(season_name),
    start_year = VALUES(start_year),
    end_year = VALUES(end_year),
    description = VALUES(description),
    status = VALUES(status),
    deleted = 0;

INSERT INTO parse_template (
    id, template_code, template_name, prompt, json_schema, status
) VALUES (
    1,
    'LINEUP',
    'Lineup parse template',
    'You are an NBA2K simulation universe screenshot data parser. Extract team starting lineup data from the image and return strict JSON only. Do not explain, do not output Markdown, and do not output code blocks. Extract only data that appears in the image. Do not guess from real NBA rosters. If unclear, use null. Team names should use English names such as Rockets, Pistons, Mavericks. Positions must be PG, SG, SF, PF, C. Each team returns at most 5 starters. Format: {"type":"LINEUP","season":null,"snapshotDate":null,"teams":[{"teamName":"","lineup":[{"position":"PG","playerName":""},{"position":"SG","playerName":""},{"position":"SF","playerName":""},{"position":"PF","playerName":""},{"position":"C","playerName":""}]}]}',
    JSON_OBJECT(
        'type', 'LINEUP',
        'season', NULL,
        'snapshotDate', NULL
    ),
    1
) ON DUPLICATE KEY UPDATE
    template_code = VALUES(template_code),
    template_name = VALUES(template_name),
    prompt = VALUES(prompt),
    json_schema = VALUES(json_schema),
    status = VALUES(status),
    deleted = 0;
