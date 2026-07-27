UPDATE bike_repair_shop
SET is_onsite_service = CASE
    WHEN DBMS_RANDOM.VALUE < 0.5 THEN 1
    ELSE 0
END;

commit;


CREATE TABLE bike_repair_shop (
    repair_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),

    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,

    phone VARCHAR(50),
    open_time VARCHAR(50),

    is_onsite_service BOOLEAN DEFAULT FALSE,

    source VARCHAR(50) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 수리점
INSERT INTO bike_repair_shop
(name, address, lat, lng, phone, open_time, is_onsite_service, source)
VALUES
('한강 자전거 수리점', '서울 영등포구',
 37.5209, 126.9396,
 '02-123-4567', '10:00~19:00',
 TRUE,
 'KAKAO_MAP');