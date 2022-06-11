/*==============================================================*/
/* Table: OYS_TUJIDAO_ALBUM                                     */
/*==============================================================*/
CREATE TABLE OYS_TUJIDAO_ALBUM_T
(
   ID                   BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID' ,
   STATE                INT  COMMENT '状态',
   TOTAL                INT  COMMENT '图片总数',
   ALBUM_NAME           VARCHAR(255)  COMMENT '相册名',
   ALBUM_ID             INT  COMMENT '相册id',
   CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
   LAST_UPDATED_DATE    DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY ALBUM_ID_UNIQUE (ALBUM_ID)
);

/*==============================================================*/
/* Table: OYS_CODEFORCES                                        */
/*==============================================================*/
CREATE TABLE OYS_CODEFORCES
(
    SUBMISSION_ID        VARCHAR(32) NOT NULL  COMMENT '提交 ID',
    NO                   NUMERIC(11,0)  COMMENT '序号',
    FRAGMENT             VARCHAR(1024)  COMMENT '片段值',
    PRIMARY KEY (SUBMISSION_ID)
);