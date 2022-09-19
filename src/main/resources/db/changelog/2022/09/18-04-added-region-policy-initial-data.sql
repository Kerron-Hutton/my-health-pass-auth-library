INSERT INTO region(id, name, code, session_duration, account_lock_duration, max_failed_login, min_password_length,
                   max_password_length, include_digit, include_special_character)
VALUES (1, 'Canada', 'CAN', 60, 10, 3, 8, 18, true, false),
       (2, 'Jamaica', 'JAM', 120, 5, 3, 8, 14, true, true),
       (3, 'United States', 'USA', 90, 10, 3, 8, 12, true, false);