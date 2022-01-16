/*==============================================================*/
/* Table: OYS_TUJIDAO_ALBUM                                     */
/*==============================================================*/
CREATE TABLE OYS_TUJIDAO_ALBUM
(
   ID                   NUMERIC(22,0) NOT NULL  COMMENT '主键 ID',
   STATE                NUMERIC(3,0)  COMMENT '状态',
   TOTAL                NUMERIC(11,0)  COMMENT '图片总数',
   ALBUM_NAME           VARCHAR(255)  COMMENT '相册名',
   ALBUM_ID             NUMERIC(22,0)  COMMENT '相册id',
   TYPE                 NUMERIC(3,0)  COMMENT '相册类型',
   CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
   LAST_UPDATED_DATE    DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY ALBUM_ID_UNIQUE (ALBUM_ID)
);