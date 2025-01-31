CREATE TABLE leaderboard (
    username VARCHAR(255) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (username)
);

-- Create an index to ensure efficient ordering by score (descending)
CREATE INDEX idx_leaderboard_score ON leaderboard (score DESC);

-- Create an index to ensure efficient ordering by score (ascending)
CREATE INDEX idx_leaderboard_score_asc ON leaderboard (score ASC);