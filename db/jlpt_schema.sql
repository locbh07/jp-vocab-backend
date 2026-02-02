-- JLPT exams + attempts schema (PostgreSQL)
-- Run manually with: psql -d jp_vocab_pg_new -f db/jlpt_schema.sql

-- 1) Exams (each JSON file = one part of one exam)
CREATE TABLE IF NOT EXISTS jlpt_exam (
    id           BIGSERIAL PRIMARY KEY,
    level        TEXT NOT NULL,              -- N1..N5
    exam_id      TEXT NOT NULL,              -- e.g. 201307
    part         SMALLINT NOT NULL,          -- 1,2,3
    source_file  TEXT NOT NULL,              -- original filename
    json_data    JSONB NOT NULL,             -- raw exam JSON
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_jlpt_exam_level_exam_part
    ON jlpt_exam(level, exam_id, part);

CREATE INDEX IF NOT EXISTS ix_jlpt_exam_level_exam
    ON jlpt_exam(level, exam_id);

-- 2) Enable exam access per user (admin decides)
ALTER TABLE useraccount
    ADD COLUMN IF NOT EXISTS exam_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE useraccount
    ADD COLUMN IF NOT EXISTS exam_code TEXT;

-- 5) Per-level exam code per user
CREATE TABLE IF NOT EXISTS user_exam_code (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES useraccount(id) ON DELETE CASCADE,
    level      TEXT NOT NULL,
    code       TEXT NOT NULL,
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, level)
);

CREATE INDEX IF NOT EXISTS ix_user_exam_code_user
    ON user_exam_code(user_id, level);

-- 6) Exam revision history (admin edits)
CREATE TABLE IF NOT EXISTS jlpt_exam_revision (
    id         BIGSERIAL PRIMARY KEY,
    level      TEXT NOT NULL,
    exam_id    TEXT NOT NULL,
    part       SMALLINT NOT NULL,
    editor_id  BIGINT,
    note       TEXT,
    json_data  JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_jlpt_exam_revision_exam
    ON jlpt_exam_revision(level, exam_id, part, created_at DESC);

-- 3) Attempts (one row per test attempt)
CREATE TABLE IF NOT EXISTS jlpt_attempt (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES useraccount(id) ON DELETE CASCADE,
    level         TEXT NOT NULL,            -- N1..N5
    exam_id       TEXT NOT NULL,            -- e.g. 201307
    started_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at   TIMESTAMPTZ,
    duration_sec  INTEGER,
    score_total   INTEGER NOT NULL DEFAULT 0,
    score_sec1    INTEGER NOT NULL DEFAULT 0,
    score_sec2    INTEGER NOT NULL DEFAULT 0,
    score_sec3    INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS ix_jlpt_attempt_user
    ON jlpt_attempt(user_id, started_at DESC);

CREATE INDEX IF NOT EXISTS ix_jlpt_attempt_exam
    ON jlpt_attempt(level, exam_id, started_at DESC);

-- 4) Attempt items (store user answers + correctness)
CREATE TABLE IF NOT EXISTS jlpt_attempt_item (
    id               BIGSERIAL PRIMARY KEY,
    attempt_id       BIGINT NOT NULL REFERENCES jlpt_attempt(id) ON DELETE CASCADE,
    part             SMALLINT NOT NULL,     -- 1,2,3
    section_index    INTEGER NOT NULL,
    question_index   INTEGER NOT NULL,
    question_id      TEXT,                  -- from JSON (qid) if present
    selected         TEXT,                  -- user choice
    correct_answer   TEXT,                  -- correct choice
    is_correct       BOOLEAN NOT NULL DEFAULT FALSE,
    question_json    JSONB                  -- snapshot for review
);

CREATE INDEX IF NOT EXISTS ix_jlpt_attempt_item_attempt
    ON jlpt_attempt_item(attempt_id, part, section_index, question_index);
