-- 임시 작성(수정 필요)


-- =========================================
-- Index_Infos (지수 정보)
-- =========================================
CREATE TABLE IF NOT EXISTS "Index_Infos" (
    "id"                  BIGSERIAL PRIMARY KEY,
    "indexName"           VARCHAR(255)    NOT NULL,
    "indexClassification" VARCHAR(255)    NOT NULL,
    "sourceType"          VARCHAR(50)     NOT NULL DEFAULT 'USER', -- USER | OPEN_API
    "employedItemsCount"  INTEGER         NOT NULL DEFAULT 0,
    "baseIndex"           NUMERIC(18,4)   NOT NULL DEFAULT 1000,
    "basePointInTime"     DATE            NOT NULL,
    "favorite"            BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT "UQ_Index_Infos_Name_Cls"
    UNIQUE ("indexName", "indexClassification")
    );

-- =========================================
-- Index_Data (지수 데이터)
-- =========================================
CREATE TABLE IF NOT EXISTS "Index_Data" (
    "id"                 BIGSERIAL PRIMARY KEY,
    "indexInfoId"        BIGINT          NOT NULL,
    "baseDate"           DATE            NOT NULL,
    "marketPrice"        NUMERIC(18,4),        -- 시가
    "closingPrice"       NUMERIC(18,4),        -- 종가
    "highPrice"          NUMERIC(18,4),        -- 고가
    "lowPrice"           NUMERIC(18,4),        -- 저가
    "versus"             NUMERIC(18,4),        -- 대비
    "fluctuationRate"    NUMERIC(9,4),         -- 등락률(%)  e.g. 1.23
    "tradingQuantity"    BIGINT,               -- 거래량
    "tradingPrice"       NUMERIC(20,2),        -- 거래대금
    "marketTotalAmount"  NUMERIC(20,2),        -- 상장 시가 총액
    CONSTRAINT "FK_Index_Data_Index_Infos"
    FOREIGN KEY ("indexInfoId") REFERENCES "Index_Infos"("id") ON DELETE CASCADE,
    CONSTRAINT "UQ_Index_Data_Info_Date"
    UNIQUE ("indexInfoId", "baseDate")
    );

-- 조회 성능용 인덱스
CREATE INDEX IF NOT EXISTS "IDX_Index_Data_Info"      ON "Index_Data" ("indexInfoId");
CREATE INDEX IF NOT EXISTS "IDX_Index_Data_BaseDate"  ON "Index_Data" ("baseDate");
CREATE INDEX IF NOT EXISTS "IDX_Index_Data_Info_Date" ON "Index_Data" ("indexInfoId", "baseDate" DESC);

-- =========================================
-- Sync_Jobs (지수 연동 작업)
-- =========================================
CREATE TABLE IF NOT EXISTS "Sync_Jobs" (
    "id"          BIGSERIAL PRIMARY KEY,
    "indexInfoId" BIGINT         NOT NULL,
    "jobType"     VARCHAR(50)    NOT NULL,     -- FULL | INCREMENTAL | MANUAL (필요시 ENUM 대체 가능)
    "targetDate"  DATE           NOT NULL,
    "worker"      VARCHAR(100)   NOT NULL,
    "jobTime"     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    "result"      BOOLEAN        NOT NULL,
    CONSTRAINT "FK_Sync_Jobs_Index_Infos"
    FOREIGN KEY ("indexInfoId") REFERENCES "Index_Infos"("id") ON DELETE CASCADE
    );

-- 조회 성능용 인덱스
CREATE INDEX IF NOT EXISTS "IDX_Sync_Jobs_Info"      ON "Sync_Jobs" ("indexInfoId");
CREATE INDEX IF NOT EXISTS "IDX_Sync_Jobs_Target"    ON "Sync_Jobs" ("targetDate");
CREATE INDEX IF NOT EXISTS "IDX_Sync_Jobs_Time_Desc" ON "Sync_Jobs" ("jobTime" DESC);
