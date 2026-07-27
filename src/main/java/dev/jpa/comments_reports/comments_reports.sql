  ---------------------------------------------------------
  -- COMMENTS_REPORTS (댓글 신고)
  ---------------------------------------------------------
  CREATE TABLE COMMENTS_REPORTS (
      report_id        NUMBER PRIMARY KEY,
  
      reporter_id      VARCHAR2(50) NOT NULL,   -- 신고자(USER_TB.USER_ID)
      USER_ID          VARCHAR2(50),            -- 처리자(USER_TB.USER_ID)
  
      report_category  VARCHAR2(50),
      reason           CLOB,
      evidence_url     VARCHAR2(500),
  
      status           VARCHAR2(20)
                       DEFAULT 'PENDING'
                       CHECK (status IN ('PENDING','IN_REVIEW','APPROVED','REJECTED')),
  
      created_at       DATE DEFAULT SYSDATE,
      processed_at     DATE,
  
      comment_id       NUMBER(10) NOT NULL      -- 신고 대상 댓글
  );
  
  
  ---------------------------------------------------------
  -- FK
  ---------------------------------------------------------
  ALTER TABLE COMMENTS_REPORTS
  ADD CONSTRAINT fk_comments_reports_reporter
  FOREIGN KEY (reporter_id)
  REFERENCES USER_TB(USER_ID);
  
  ALTER TABLE COMMENTS_REPORTS
  ADD CONSTRAINT fk_comments_reports_admin
  FOREIGN KEY (USER_ID)
  REFERENCES USER_TB(USER_ID);
  
  ALTER TABLE COMMENTS_REPORTS
  ADD CONSTRAINT fk_comments_reports_comment
  FOREIGN KEY (comment_id)
  REFERENCES COMMENTS(comment_id);
  
  
  ---------------------------------------------------------
  -- 중복 신고 방지
  ---------------------------------------------------------
  ALTER TABLE COMMENTS_REPORTS
  ADD CONSTRAINT uq_comments_report UNIQUE (reporter_id, comment_id);
  
  
  ---------------------------------------------------------
  -- INDEX
  ---------------------------------------------------------
  CREATE INDEX idx_comments_reports_status   ON COMMENTS_REPORTS(status);
  CREATE INDEX idx_comments_reports_category ON COMMENTS_REPORTS(report_category);
  CREATE INDEX idx_comments_reports_comment  ON COMMENTS_REPORTS(comment_id);
  CREATE INDEX idx_comments_reports_reporter ON COMMENTS_REPORTS(reporter_id);
  
  
  ---------------------------------------------------------
  -- SEQUENCE
  ---------------------------------------------------------
  CREATE SEQUENCE SEQ_COMMENTS_REPORTS;
  
  
   /********************************************************************
   *  COMMENTS_REPORTS CRUD
   ********************************************************************/
  
  ---------------------------------------------------------
  -- CREATE (신고 등록)
  ---------------------------------------------------------
  INSERT INTO COMMENTS_REPORTS (
      report_id,
      reporter_id,
      report_category,
      reason,
      evidence_url,
      comment_id
  )
  VALUES (
      SEQ_COMMENTS_REPORTS.NEXTVAL,
      :reporter_id,
      :report_category,
      :reason,
      :evidence_url,
      :comment_id
  );
  
  
  ---------------------------------------------------------
  -- READ
  ---------------------------------------------------------
  
  -- 전체 목록 (최신순)
  SELECT * FROM COMMENTS_REPORTS
  ORDER BY created_at DESC;
  
  -- 신고 상세
  SELECT * FROM COMMENTS_REPORTS
  WHERE report_id = :report_id;
  
  -- 특정 댓글 신고 목록
  SELECT * FROM COMMENTS_REPORTS
  WHERE comment_id = :comment_id
  ORDER BY created_at DESC;
  
  -- 특정 유저 신고 목록
  SELECT * FROM COMMENTS_REPORTS
  WHERE reporter_id = :reporter_id;
  
  
  ---------------------------------------------------------
  -- UPDATE (관리자 처리)
  ---------------------------------------------------------
  UPDATE COMMENTS_REPORTS
  SET status = :status,        -- 'PENDING','IN_REVIEW','APPROVED','REJECTED'
      USER_ID = :admin_id,     -- 처리자
      processed_at = SYSDATE
  WHERE report_id = :report_id;
  
  
  ---------------------------------------------------------
  -- DELETE
  ---------------------------------------------------------
  DELETE FROM COMMENTS_REPORTS WHERE report_id = :report_id;
  
  DELETE FROM COMMENTS_REPORTS WHERE comment_id = :comment_id;
  
  DELETE FROM COMMENTS_REPORTS WHERE reporter_id = :reporter_id;
  
}

