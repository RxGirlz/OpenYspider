# OpenYspider 4.x

千万级图片、视频爬虫 [开源版本]

![](swagger3.png)

## 简介

OpenYspider 是一个使用 Java 编写的简单爬虫。主要用到的技术栈有：

1. spring-boot-starter-web
2. spring-boot-starter-test
3. mybatis-plus-boot-starter
4. springfox-boot-starter
5. lombok
6. jsoup
7. mockito + jacoco

当前 LTS 的网站有：

1. `tujidao.com`

Deprecated 的网站（请于历史提交中查看）：

1. `tangyun365.com`
2. `yalayi.com`
3. `rosmm88.com`
4. `mzsock.com`
5. `meinvla.net`
6. `leetcode-cn.com`

## 开发环境

`Windows 11` + `JDK 17` + `Mysql 8.x`

```sh
$ java --version
openjdk 17.0.1 2021-10-19
OpenJDK Runtime Environment (build 17.0.1+12-39)
OpenJDK 64-Bit Server VM (build 17.0.1+12-39, mixed mode, sharing)
```

运行启动类 `OpenYspiderApplication` 后，浏览器访问 [http://localhost:23333/swagger-ui/index.html#/](http://localhost:23333/swagger-ui/index.html#/)

数据库脚本: [sql_scripts](./sql_scripts/oys3_all_scripts_mysql.sql)

## 爬取网站

数据统计截止 2022-02-12

### 1 图集岛（原美图日） [ 2,647,717P / 905G ]

- 目标网站：[https://www.tujidao.com/](https://www.tujidao.com/)
- 特点：图片路径可遍历

```sql
select count(*) from oys_tujidao_album_t where album_id > 0 and album_id <= 10000; -- 9995 ok
select count(*) from oys_tujidao_album_t where album_id > 10000 and album_id <= 20000; -- 10000
select count(*) from oys_tujidao_album_t where album_id > 20000 and album_id <= 30000; -- 9999 [23001]
select count(*) from oys_tujidao_album_t where album_id > 30000 and album_id <= 40000; -- 10000
select count(*) from oys_tujidao_album_t where album_id > 40000 and album_id <= 50000; -- 8925 [46018]
```

## 部分成果展示

![](result1.png)

![](result2.png)
