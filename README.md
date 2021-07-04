# OpenYspider 3.x

千万级图片、视频爬虫 [开源版本]

![](swagger3.png)

## 简介

OpenYspider 是一个使用 Java 编写的简单爬虫。主要用到的技术栈有：

1. spring-boot-web
2. mybatis-plus
3. springfox-swagger3
4. lombok
5. jsoup
6. selenium/chrome-driver
7. freemarker

当前 LTS 的网站有：

1. `tujidao.com`
2. `meinvla.net`
3. `leetcode-cn.com`

Deprecated 的网站（请于历史提交中查看）：

1. `tangyun365.com`
2. `yalayi.com`
3. `rosmm88.com`
4. `mzsock.com`

## 使用

运行项目，浏览器访问 http://localhost:23333/swagger-ui/index.html#/

所需环境 jdk8 + mysql。由于爬取部分网站使用到了 selenium 和 chromedriver，需要下载驱动程序，版本需要和 chrome 匹配：

### Windows

下载安装 [chromedriver.exe](http://npm.taobao.org/mirrors/chromedriver)

### Mac OS X

安装 chromedriver：

```sh
brew cask install chromedriver
```

然后把插件改成对应路径，例如：

```java
System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
```

## 爬取网站

数据统计截止 2021-07-04

### 1 图集岛（原美图日） [ 1,631,937P / 522G ]

- 目标网站：[http://www.tujidao.com/](http://www.tujidao.com/)
- 特点：图片路径可遍历

### 2 美女啦 [ 图片+视频 约 783w P / 397G ]

- 目标网站：[http://www.meinvla.net/](http://www.meinvla.net/)

### 3 Leetcode 题集

- 目标网站：[https://leetcode-cn.com/problems](https://leetcode-cn.com/problems)
- apache common、freemarker 模板、自动化测试

## SQL 建表语句

- [MySQL 全量脚本](./SQLScripts/oys3_all_scripts_mysql.sql)

## 部分成果展示

![](result1.png)

![](result2.png)
