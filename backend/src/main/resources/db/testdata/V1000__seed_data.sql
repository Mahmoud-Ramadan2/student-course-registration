

INSERT INTO roles (role_name, description) VALUES
    ('ADMIN', 'Full system access'),
    ('REGISTRAR',      'Manages courses, offerings, and enrollments'),
    ('INSTRUCTOR',     'Views/manages own courses'),
    ('STUDENT',        'Registers for and drops own courses');

INSERT INTO users (username, email, password_hash, is_active) VALUES
    ('admin1',     'admin1@school.edu',     '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('registrar1', 'registrar1@school.edu', '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('dsmith',     'dsmith@school.edu',     '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('rjones',     'rjones@school.edu',     '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('student1',   'student1@school.edu',   '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('student2',   'student2@school.edu',   '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('student3',   'student3@school.edu',   '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('student4',   'student4@school.edu',   '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('student5',   'student5@school.edu',   '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE),
    ('student6',   'student6@school.edu',   '$2a$12$OcUIN0P0TYL5RKboDFx0Xu0izoK0zEHyJPbautRpaGSrk0f4jQj4u', TRUE);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE (u.username = 'admin1'     AND r.role_name = 'ADMIN')
   OR (u.username = 'registrar1' AND r.role_name = 'REGISTRAR')
   OR (u.username = 'dsmith'     AND r.role_name = 'INSTRUCTOR')
   OR (u.username = 'rjones'     AND r.role_name = 'INSTRUCTOR')
   OR (u.username IN ('student1','student2','student3','student4','student5','student6')
       AND r.role_name = 'STUDENT');

INSERT INTO staff (id, employee_number, first_name, last_name, title, department, hire_date)
SELECT id, 'EMP-1001', 'Dana', 'Smith', 'Associate Professor', 'Computer Science', DATE '2018-08-15'
FROM users WHERE username = 'dsmith';

INSERT INTO staff (id, employee_number, first_name, last_name, title, department, hire_date)
SELECT id, 'EMP-1002', 'Robin', 'Jones', 'Professor', 'Mathematics', DATE '2012-01-10'
FROM users WHERE username = 'rjones';

INSERT INTO students (id, student_number, first_name, last_name, date_of_birth, status)
SELECT id, 'STU-0001', 'Alex',   'Nguyen',   DATE '2006-03-12', 'ACTIVE' FROM users WHERE username = 'student1'
UNION ALL
SELECT id, 'STU-0002', 'Bianca', 'Kowalski', DATE '2005-11-02', 'ACTIVE' FROM users WHERE username = 'student2'
UNION ALL
SELECT id, 'STU-0003', 'Carlos', 'Mendez',   DATE '2006-07-19', 'ACTIVE' FROM users WHERE username = 'student3'
UNION ALL
SELECT id, 'STU-0004', 'Deepa',  'Rao',      DATE '2005-05-30', 'ACTIVE' FROM users WHERE username = 'student4'
UNION ALL
SELECT id, 'STU-0005', 'Ethan',  'Walker',   DATE '2006-01-08', 'ACTIVE' FROM users WHERE username = 'student5'
UNION ALL
SELECT id, 'STU-0006', 'Farida', 'Haidari',  DATE '2005-09-23', 'ACTIVE' FROM users WHERE username = 'student6';

INSERT INTO terms (name, start_date, end_date, registration_start, registration_end, is_active) VALUES
    ('Fall 2026',   DATE '2026-08-24', DATE '2026-12-12',
        TIMESTAMPTZ '2026-07-01 00:00:00+00', TIMESTAMPTZ '2026-09-04 23:59:59+00', TRUE),
    ('Spring 2027', DATE '2027-01-12', DATE '2027-05-01',
        TIMESTAMPTZ '2026-11-01 00:00:00+00', TIMESTAMPTZ '2027-01-23 23:59:59+00', FALSE);

INSERT INTO courses (code, title, description, credit_hours, department, is_active) VALUES
    ('CS101',   'Introduction to Programming', 'Fundamentals of programming using Java.', 3, 'Computer Science', TRUE),
    ('CS201',   'Data Structures',             'Core data structures and algorithm analysis.', 3, 'Computer Science', TRUE),
    ('MATH101', 'Calculus I',                  'Limits, derivatives, and integrals.', 4, 'Mathematics', TRUE);
INSERT INTO course_prerequisites (course_id, prerequisite_id)
SELECT c1.id, c2.id FROM courses c1, courses c2
WHERE c1.code = 'CS201' AND c2.code = 'CS101';

INSERT INTO course_offerings (course_id, term_id, section_number, instructor_id, capacity, waitlist_capacity, room, days_of_week, start_time, end_time, status)
SELECT c.id, t.id, '001', s.id, 3, 2, 'ENG-210', 'MWF', TIME '09:00', TIME '09:50', 'OPEN'
FROM courses c, terms t, staff s
WHERE c.code = 'CS101' AND t.name = 'Fall 2026' AND s.employee_number = 'EMP-1001';
INSERT INTO course_offerings (course_id, term_id, section_number, instructor_id, capacity, waitlist_capacity, room, days_of_week, start_time, end_time, status)
SELECT c.id, t.id, '001', s.id, 30, 5, 'ENG-214', 'TTh', TIME '11:00', TIME '12:15', 'OPEN'
FROM courses c, terms t, staff s
WHERE c.code = 'CS201' AND t.name = 'Fall 2026' AND s.employee_number = 'EMP-1001';
INSERT INTO course_offerings (course_id, term_id, section_number, instructor_id, capacity, waitlist_capacity, room, days_of_week, start_time, end_time, status)
SELECT c.id, t.id, '001', s.id, 2, 3, 'MTH-105', 'MWF', TIME '10:00', TIME '10:50', 'OPEN'
FROM courses c, terms t, staff s
WHERE c.code = 'MATH101' AND t.name = 'Fall 2026' AND s.employee_number = 'EMP-1002';

INSERT INTO enrollments (student_id, offering_id, course_id, term_id, status, enrolled_at)
SELECT st.id, o.id, o.course_id, o.term_id, 'ENROLLED', now() - INTERVAL '5 days'
FROM students st, course_offerings o, courses c
WHERE st.id = (SELECT id FROM users WHERE username = 'student1')
  AND c.code = 'MATH101' AND o.course_id = c.id;

INSERT INTO enrollments (student_id, offering_id, course_id, term_id, status, enrolled_at)
SELECT st.id, o.id, o.course_id, o.term_id, 'ENROLLED', now() - INTERVAL '4 days'
FROM students st, course_offerings o, courses c
WHERE st.id = (SELECT id FROM users WHERE username = 'student2')
  AND c.code = 'MATH101' AND o.course_id = c.id;

INSERT INTO enrollments (student_id, offering_id, course_id, term_id, status, waitlist_position, enrolled_at)
SELECT st.id, o.id, o.course_id, o.term_id, 'WAITLISTED', 1, now() - INTERVAL '3 days'
FROM students st, course_offerings o, courses c
WHERE st.id = (SELECT id FROM users WHERE username = 'student3')
  AND c.code = 'MATH101' AND o.course_id = c.id;

INSERT INTO enrollments (student_id, offering_id, course_id, term_id, status, enrolled_at)
SELECT st.id, o.id, o.course_id, o.term_id, 'ENROLLED', now() - INTERVAL '6 days'
FROM students st, course_offerings o, courses c
WHERE st.id = (SELECT id FROM users WHERE username = 'student1')
  AND c.code = 'CS101' AND o.course_id = c.id;

INSERT INTO enrollments (student_id, offering_id, course_id, term_id, status, enrolled_at)
SELECT st.id, o.id, o.course_id, o.term_id, 'ENROLLED', now() - INTERVAL '6 days'
FROM students st, course_offerings o, courses c
WHERE st.id = (SELECT id FROM users WHERE username = 'student4')
  AND c.code = 'CS101' AND o.course_id = c.id;

-- student5: dropped CS101, then re-registered later (two rows, one DROPPED, one ENROLLED)
INSERT INTO enrollments (student_id, offering_id, course_id, term_id, status, enrolled_at, dropped_at)
SELECT st.id, o.id, o.course_id, o.term_id, 'DROPPED', now() - INTERVAL '6 days', now() - INTERVAL '4 days'
FROM students st, course_offerings o, courses c
WHERE st.id = (SELECT id FROM users WHERE username = 'student5')
  AND c.code = 'CS101' AND o.course_id = c.id;

INSERT INTO enrollments (student_id, offering_id, course_id, term_id, status, enrolled_at)
SELECT st.id, o.id, o.course_id, o.term_id, 'ENROLLED', now() - INTERVAL '1 day'
FROM students st, course_offerings o, courses c
WHERE st.id = (SELECT id FROM users WHERE username = 'student5')
  AND c.code = 'CS101' AND o.course_id = c.id;

INSERT INTO enrollments (student_id, offering_id, course_id, term_id, status, enrolled_at)
SELECT st.id, o.id, o.course_id, o.term_id, 'ENROLLED', now() - INTERVAL '1 day'
FROM students st, course_offerings o, courses c
WHERE st.id = (SELECT id FROM users WHERE username = 'student2')
  AND c.code = 'CS201' AND o.course_id = c.id;
