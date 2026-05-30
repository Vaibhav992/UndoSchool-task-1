CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE user_profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_user_id    VARCHAR(255) NOT NULL UNIQUE,
    role            VARCHAR(20) NOT NULL CHECK (role IN ('TEACHER', 'PARENT')),
    timezone        VARCHAR(64) NOT NULL,
    display_name    VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE courses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    teacher_id      UUID NOT NULL REFERENCES user_profiles(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE offerings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id           UUID NOT NULL REFERENCES courses(id),
    teacher_id          UUID NOT NULL REFERENCES user_profiles(id),
    name                VARCHAR(255) NOT NULL,
    teacher_timezone    VARCHAR(64) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offering_id     UUID NOT NULL REFERENCES offerings(id) ON DELETE CASCADE,
    teacher_id      UUID NOT NULL REFERENCES user_profiles(id),
    start_at        TIMESTAMPTZ NOT NULL,
    end_at          TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT sessions_end_after_start CHECK (end_at > start_at)
);

CREATE TABLE bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offering_id     UUID NOT NULL REFERENCES offerings(id),
    parent_id       UUID NOT NULL REFERENCES user_profiles(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE parent_time_blocks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id       UUID NOT NULL REFERENCES user_profiles(id),
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    session_id      UUID NOT NULL REFERENCES sessions(id),
    start_at        TIMESTAMPTZ NOT NULL,
    end_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT parent_time_blocks_end_after_start CHECK (end_at > start_at)
);
