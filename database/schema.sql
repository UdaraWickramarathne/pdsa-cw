-- PDSA Coursework — Database Schema
-- Run: mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS pdsa_cw;
USE pdsa_cw;

-- ============================================================
-- Game 1: Minimum Cost (Assignment Problem)
-- ============================================================
CREATE TABLE IF NOT EXISTS game1_rounds (
    round_id        INT AUTO_INCREMENT PRIMARY KEY,
    n               INT        NOT NULL COMMENT 'Number of tasks/employees (50-100)',
    min_cost        BIGINT     NOT NULL COMMENT 'Optimal total assignment cost',
    hungarian_ms    BIGINT     COMMENT 'Hungarian algorithm time in milliseconds',
    branch_bound_ms BIGINT     COMMENT 'Branch & Bound algorithm time in milliseconds',
    played_at       TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Game 2: Snake & Ladder
-- ============================================================
CREATE TABLE IF NOT EXISTS game2_rounds (
    round_id    INT AUTO_INCREMENT PRIMARY KEY,
    board_size  INT       NOT NULL COMMENT 'N (board is NxN)',
    min_throws  INT       NOT NULL COMMENT 'Minimum dice throws to reach last cell',
    bfs_ms      BIGINT    COMMENT 'BFS algorithm time in milliseconds',
    dp_ms       BIGINT    COMMENT 'Dynamic Programming time in milliseconds',
    played_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game2_winners (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    round_id    INT          NOT NULL,
    player_name VARCHAR(100) NOT NULL,
    answer      INT          NOT NULL,
    won_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (round_id) REFERENCES game2_rounds(round_id)
);

-- ============================================================
-- Game 3: Traffic Simulation (Max Flow)
-- ============================================================
CREATE TABLE IF NOT EXISTS game3_rounds (
    round_id           INT AUTO_INCREMENT PRIMARY KEY,
    max_flow           INT       NOT NULL COMMENT 'Correct max flow from A to T',
    ford_fulkerson_ms  BIGINT    COMMENT 'Ford-Fulkerson time in milliseconds',
    edmonds_karp_ms    BIGINT    COMMENT 'Edmonds-Karp time in milliseconds',
    played_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game3_winners (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    round_id    INT          NOT NULL,
    player_name VARCHAR(100) NOT NULL,
    answer      INT          NOT NULL,
    won_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (round_id) REFERENCES game3_rounds(round_id)
);

-- ============================================================
-- Game 4: Knight's Tour
-- ============================================================
CREATE TABLE IF NOT EXISTS game4_rounds (
    round_id        INT AUTO_INCREMENT PRIMARY KEY,
    board_size      INT       NOT NULL COMMENT '8 or 16',
    start_row       INT       NOT NULL,
    start_col       INT       NOT NULL,
    warnsdorff_ms   BIGINT    COMMENT 'Warnsdorff heuristic time in milliseconds',
    backtracking_ms BIGINT    COMMENT 'Backtracking time in milliseconds (-1 if timed out)',
    played_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game4_winners (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    round_id      INT          NOT NULL,
    player_name   VARCHAR(100) NOT NULL,
    tour_sequence TEXT         COMMENT 'Comma-separated cell indices of the tour',
    won_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (round_id) REFERENCES game4_rounds(round_id)
);

-- ============================================================
-- Game 5: Sixteen Queens
-- ============================================================
CREATE TABLE IF NOT EXISTS game5_solutions (
    solution_id INT AUTO_INCREMENT PRIMARY KEY,
    placement   VARCHAR(64)  NOT NULL COMMENT 'Space-separated row positions per column, e.g. "1 5 8 6 3 7 2 4"',
    claimed     BOOLEAN      DEFAULT FALSE,
    computed_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_placement (placement)
);

CREATE TABLE IF NOT EXISTS game5_winners (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    solution_id INT          NOT NULL,
    player_name VARCHAR(100) NOT NULL,
    won_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (solution_id) REFERENCES game5_solutions(solution_id)
);

CREATE TABLE IF NOT EXISTS game5_timing (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    sequential_ms    BIGINT    COMMENT 'Single-threaded solver time in milliseconds',
    threaded_ms      BIGINT    COMMENT 'Multi-threaded solver time in milliseconds',
    total_solutions  INT       COMMENT 'Total solutions found',
    recorded_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
