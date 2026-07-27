CREATE TABLE REGION (
    region_id      NUMBER PRIMARY KEY,
    region_name    VARCHAR2(100) NOT NULL
);

CREATE SEQUENCE seq_region START WITH 1 INCREMENT BY 1;

CREATE TABLE CITY (
    city_id      NUMBER PRIMARY KEY,
    region_id    NUMBER NOT NULL,
    city_name    VARCHAR2(100) NOT NULL,

    CONSTRAINT fk_city_region
        FOREIGN KEY (region_id) REFERENCES REGION(region_id)
);

CREATE SEQUENCE seq_city START WITH 1 INCREMENT BY 1;

INSERT INTO REGION (region_id, region_name)
VALUES (1, '서울특별시');

INSERT INTO REGION (region_id, region_name)
VALUES (2, '부산광역시');


INSERT INTO CITY (city_id, city_name, region_id)
VALUES (101, '강남구', 1);

INSERT INTO CITY (city_id, city_name, region_id)
VALUES (102, '서초구', 1);

INSERT INTO CITY (city_id, city_name, region_id)
VALUES (201, '해운대구', 2);
