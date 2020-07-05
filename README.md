# OpenYspider 3.1

千万级图片、视频爬虫 [开源版本]：`tujidao.com`、`meinvla.net`：

![](swagger-ui.png)

注: `tangyun365.com`、`yalayi.com`、`rosmm88.com`、`mzsock.com`、`m7.22c.im` 请切换至 `1.x` 分支查看。

## 3.1 版本新特性

1. `Spring Boot` 版本升级: `2.2.1` => `2.3.1`；
2. `MySQL` 版本升级: `5.7` => `8.0`；
3. 适配 Oracle；
4. 工程模块化，整洁架构；
5. 数据库全量脚本规范化；

## 使用

部分网站使用到了 selenium 和 chromedriver，需要下载一下驱动程序，注意需要和 chrome 版本匹配：

### Windows

下载安装 chromedriver.exe

[http://npm.taobao.org/mirrors/chromedriver](http://npm.taobao.org/mirrors/chromedriver)

### Mac OS X

安装 chromedriver：

```sh
brew cask install chromedriver
```

然后把插件改成对应路径，例如：

```sh
System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
```

## 爬取网站

### 1 图集岛（原美图日） [ 1,631,937P / 522G ]

- 目标网站：[http://www.tujidao.com/](http://www.tujidao.com/)
- 特点：图片路径可遍历

### 2 美女啦 [ 统计中 约 300w P ]

- 目标网站：[http://www.meinvla.net/](http://www.meinvla.net/)

### 3 Leetcode 题集

- 目标网站：[https://leetcode-cn.com/problems](https://leetcode-cn.com/problems)
- apache common、freemarker 模板、自动化测试

## SQL 建表语句

详见 [SQLScripts](./SQLScripts/) 目录

- [MySQL 全量脚本](./SQLScripts/oys3_all_scripts_mysql.sql)
- [Oracle 全量脚本](./SQLScripts/oys3_all_scripts_oracle.sql)

## 部分成果展示

![](result1.png)

![](result2.png)
