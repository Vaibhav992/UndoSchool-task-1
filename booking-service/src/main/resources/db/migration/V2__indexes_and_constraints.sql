CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_offerings_course_id ON offerings(course_id);
CREATE INDEX idx_offerings_teacher_id ON offerings(teacher_id);
CREATE INDEX idx_offerings_status ON offerings(status);
CREATE INDEX idx_sessions_offering_id ON sessions(offering_id);
CREATE INDEX idx_sessions_teacher_id ON sessions(teacher_id);
CREATE INDEX idx_sessions_start_at ON sessions(start_at);
CREATE INDEX idx_bookings_offering_id ON bookings(offering_id);
CREATE INDEX idx_bookings_parent_id ON bookings(parent_id);
CREATE INDEX idx_parent_time_blocks_parent_id ON parent_time_blocks(parent_id);
CREATE INDEX idx_parent_time_blocks_booking_id ON parent_time_blocks(booking_id);

CREATE UNIQUE INDEX uq_bookings_parent_offering_confirmed
    ON bookings(parent_id, offering_id)
    WHERE status = 'CONFIRMED';

ALTER TABLE parent_time_blocks
    ADD CONSTRAINT no_parent_time_overlap
    EXCLUDE USING gist (
        parent_id WITH =,
        tstzrange(start_at, end_at, '[)') WITH &&
    );
