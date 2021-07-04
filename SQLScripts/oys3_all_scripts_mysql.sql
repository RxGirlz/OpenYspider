/*==============================================================*/
/* Table: OYS_LEETCODE_IMAGE                                    */
/*==============================================================*/
CREATE TABLE OYS_LEETCODE_IMAGE
(
   ID                   NUMERIC(22,0) NOT NULL  COMMENT '主键 ID',
   IMG_NAME             VARCHAR(255)  COMMENT '图片本地名',
   IMG_URL              VARCHAR(255)  COMMENT '图片远端名',
   QUESTION_ID          NUMERIC(22,0) NOT NULL  COMMENT '问题 ID',
   STATE                NUMERIC(3,0)  COMMENT '状态',
   CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
   LAST_UPDATED_BY      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY IMG_URL_UNIQUE (IMG_URL)
);

/*==============================================================*/
/* Table: OYS_LEETCODE_PROBLEM                                  */
/*==============================================================*/
CREATE TABLE OYS_LEETCODE_PROBLEM
(
   ID                   NUMERIC(22,0) NOT NULL  COMMENT '主键 ID',
   TITLE                VARCHAR(255)  COMMENT '标题名',
   TITLE_CN             VARCHAR(255)  COMMENT '中文标题名',
   TITLE_SLUG           VARCHAR(255)  COMMENT '路径名',
   PAID_ONLY            NUMERIC(3,0)  COMMENT '是否付费',
   QUESTION_ID          NUMERIC(22,0) NOT NULL  COMMENT '问题 ID',
   FE_QUESTION_ID       VARCHAR(64) NOT NULL  COMMENT '问题前端 ID',
   DIFFICULTY           NUMERIC(3,0)  COMMENT '难度',
   HAS_BUG              NUMERIC(3,0)  COMMENT '是否存在渲染bug',
   CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
   LAST_UPDATED_BY      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY QUESTION_ID_UNIQUE (QUESTION_ID)
);

/*==============================================================*/
/* Table: OYS_LEETCODE_PROBLEM_DETAIL                           */
/*==============================================================*/
CREATE TABLE OYS_LEETCODE_PROBLEM_DETAIL
(
   ID                   NUMERIC(22,0) NOT NULL  COMMENT '主键 ID',
   QUESTION_ID          NUMERIC(22,0) NOT NULL  COMMENT '问题 ID',
   HTML_CONTENT         TEXT NOT NULL  COMMENT 'HTML 问题内容',
   TXT_CONTENT          TEXT NOT NULL  COMMENT 'TXT 问题内容',
   CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
   LAST_UPDATED_BY      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY QUESTION_ID_UNIQUE (QUESTION_ID)
);

/*==============================================================*/
/* Table: OYS_MEINVLA_ALBUM                                     */
/*==============================================================*/
CREATE TABLE OYS_MEINVLA_ALBUM
(
   ID                   NUMERIC(22,0) NOT NULL  COMMENT '主键 ID',
   STATE                NUMERIC(3,0)  COMMENT '状态',
   ALBUM_NAME           VARCHAR(255)  COMMENT '相册名',
   ALBUM_ID             NUMERIC(22,0)  COMMENT '相册id',
   TYPE                 NUMERIC(3,0)  COMMENT '相册类型',
   TOTAL                NUMERIC(11,0)  COMMENT '图片总数',
   CUR_TOTAL            NUMERIC(11,0)  COMMENT '当前图片总数',
   CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
   LAST_UPDATED_BY      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY ALBUM_ID_UNIQUE (ALBUM_ID)
);

/*==============================================================*/
/* Table: OYS_MEINVLA_IMAGE                                     */
/*==============================================================*/
CREATE TABLE OYS_MEINVLA_IMAGE
(
   ID                   NUMERIC(22,0) NOT NULL  COMMENT '主键 ID',
   IMG_NAME             VARCHAR(64)  COMMENT '图片本地名',
   IMG_URL              VARCHAR(500)  COMMENT '图片远端名',
   ALBUM_ID             NUMERIC(22,0)  COMMENT '相册id',
   STATE                NUMERIC(3,0)  COMMENT '状态',
   CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
   LAST_UPDATED_BY      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY IMG_NAEM_UNIQUE (IMG_NAME)
);

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
   LAST_UPDATED_BY      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY ALBUM_ID_UNIQUE (ALBUM_ID)
);

/*==============================================================*/
/* Table: OYS_YANDE_IMAGE                                       */
/*==============================================================*/
CREATE TABLE OYS_YANDE_IMAGE
(
   ID                   NUMERIC(22,0) NOT NULL  COMMENT '主键 ID',
   IMG_NAME             VARCHAR(10)  COMMENT '图片本地名',
   IMG_URL              VARCHAR(500)  COMMENT '图片远端名',
   STATE                NUMERIC(3,0)  COMMENT '状态',
   CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
   LAST_UPDATED_BY      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
   PRIMARY KEY (ID),
   KEY IMG_NAEM_UNIQUE (IMG_NAME)
);

/*==============================================================*/
/* Table: OYS_JAV_T                                             */
/*==============================================================*/
CREATE TABLE OYS_LEETCODE_IMAGE
(
    ID                   VARCHAR(32) NOT NULL COMMENT '番号',
    studio               VARCHAR(128)  COMMENT '图片本地名',
    genre                VARCHAR(255)  COMMENT '图片远端名',
    label          NUMERIC(22,0) NOT NULL  COMMENT '问题 ID',
    STATE                NUMERIC(3,0)  COMMENT '状态',
    CREATION_DATE        DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    LAST_UPDATED_BY      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '最后修改时间',
    PRIMARY KEY (ID),
    KEY IMG_URL_UNIQUE (IMG_URL)
);