package com.devyy.oys.meinvla;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devyy.oys.core.enums.StateTypeEnum;
import com.devyy.oys.core.util.ReptileUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @since 2019-12-01
 */
@Slf4j
@Service
public class MeinvlaService implements IMeinvlaService {
    /**
     * 文件不合法名正则
     */
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|\\[\\]_]");

    private static final String MEINVLA_LOCAL_PREFIX = "D:/Meinvla爬虫/";

    @Autowired
    private IMeinvlaAlbumMapper meinvlaAlbumMapper;
    @Autowired
    private IMeinvlaImageMapper meinvlaImageMapper;

    @Override
    public String doScanAlbums() {
        List<MeinvlaTypeEnum> typeEnumList = MeinvlaTypeEnum.getEnums();
        // 每一种相册类型
        for (MeinvlaTypeEnum typeEnum : typeEnumList) {
            doScanAlbumsOrVideos(typeEnum);
        }
        return "success";
    }

    private void doScanAlbumsOrVideos(MeinvlaTypeEnum typeEnum) {
        // 设 1000 页为结束页，获取末页 id
        String urlEnd = String.format(Locale.ENGLISH, "http://www.meinvla.net/video/type%s/-----gold-1000.html", typeEnum.getSeq());
        Integer endAlbumId = null;
        Integer curAlbumId = null;
        try {
            Document document = Jsoup.connect(urlEnd).get();
            String endEffect5Href = document
                    .getElementsByClass(
                            typeEnum.isAlbum()
                                    ? "index-body-nr-left-1-li xl6 xs4 xm4 xb3"
                                    : "index-body-nr-left-1-li xl6 xs4 xm4 xb2"
                    )
                    .last()
                    .getElementsByClass("effect5")
                    .first()
                    .attr("href");
            endAlbumId = Integer.parseInt(endEffect5Href.replace("/play/", "").replace(".html", ""));
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // 由于不知道具体多少页
        for (int i = 1; ; i++) {
            // 某一页
            String url = String.format(Locale.ENGLISH, "http://www.meinvla.net/video/type%s/-----gold-%s.html", typeEnum.getSeq(), i);

            Document document = null;
            try {
                document = Jsoup.connect(url).get();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (Objects.nonNull(document)) {
                for (Element element : document
                        .getElementsByClass(
                                typeEnum.isAlbum()
                                        ? "index-body-nr-left-1-li xl6 xs4 xm4 xb3"
                                        : "index-body-nr-left-1-li xl6 xs4 xm4 xb2"
                        )) {
                    String effect5Title = element.getElementsByClass("effect5").first().attr("title");
                    String effect5Href = element.getElementsByClass("effect5").first().attr("href");

                    String albumName = rmIllegalName(effect5Title);
                    Integer albumId = Integer.parseInt(effect5Href.replace("/play/", "").replace(".html", ""));
                    curAlbumId = albumId;

                    MeinvlaAlbumDO meinvlaAlbumDO = new MeinvlaAlbumDO();
                    meinvlaAlbumDO.setType(typeEnum.getSeq());
                    meinvlaAlbumDO.setAlbumName(albumName);
                    meinvlaAlbumDO.setAlbumId(albumId);
                    meinvlaAlbumDO.setState(StateTypeEnum.STARTED.getSeq());

                    Map<String, Object> queryMap = new HashMap<>(1);
                    queryMap.put("album_id", albumId);
                    List<MeinvlaAlbumDO> meinvlaAlbumDOList = meinvlaAlbumMapper.selectByMap(queryMap);
                    if (CollectionUtils.isEmpty(meinvlaAlbumDOList)) {
                        meinvlaAlbumMapper.insert(meinvlaAlbumDO);
                        log.info("==>typeEnum={} i={} albumId={} albumName={}", typeEnum, i, albumId, albumName);
                    } else {
                        log.warn("记录已存在 typeEnum={} i={} albumId={} albumName={}", typeEnum, i, albumId, albumName);
                    }
                }
                if (Objects.equals(curAlbumId, endAlbumId)) {
                    log.warn("==>curAlbumId==endAlbumId={} 跳出当前循环", endAlbumId);
                    break;
                }
            }
        }
    }

    @Override
    public String doScanImages() {
        // 配置 chromedriver.exe 路径
        System.setProperty("webdriver.chrome.driver", "C:/Users/DEVYY/Documents/chromedriver_win32/chromedriver.exe");
        // Mac 用户
        // todo 这些配置，最好放到application.properties文件里
        // System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        // 启动一个 chrome 实例
        WebDriver webDriver = new ChromeDriver();
        // 设置超时时间为 30 s
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        webDriver.get("http://www.meinvla.net/user-login.html");
        // wait 30s 输入账号密码
        this.waitSeconds(35);

        QueryWrapper<MeinvlaAlbumDO> wrapper = new QueryWrapper<>();
        wrapper.select()
                .ne("state", StateTypeEnum.BLACKLIST.getSeq())
                .isNull("total")
                .in("type", 87,89,95,96,97,112,113,116,117,118,119,120);
        meinvlaAlbumMapper.selectList(wrapper).forEach(vo -> {
            final Integer albumId = vo.getAlbumId();
            // http://www.meinvla.net/play/6048211.html
            String url = "http://www.meinvla.net/play/" + albumId + ".html";

            try {
                log.info("==>url={}", url);
                webDriver.get(url);
            }
            // 此处捕获所有 Throwable 因为并不需要关心，还会中断程序
            catch (Throwable e) {
                log.warn(e.getMessage().substring(0, 30));
            }

            Document document = Jsoup.parse(webDriver.getPageSource());

            if (Objects.nonNull(document) &&
                    Objects.nonNull(document.getElementsByClass("player tu_bodyplay")) &&
                    Objects.nonNull(document.getElementsByClass("player tu_bodyplay").first()) &&
                    Objects.nonNull(document.getElementsByClass("player tu_bodyplay").first().children())) {
                boolean is500 = false;
                Elements elements = document.getElementsByClass("player tu_bodyplay").first().children();
                for (Element element : elements) {
                    String src = element.getElementsByTag("a").first().attr("href");
                    // "//p.10019.net/tu/bcb2cf50509a1267f2b6d94f2530f5b814ApE01.jpg.jpg"
                    // 其中 "//p.10019.net/tu/" 长度为 17
                    String imgName = src.substring(17).replace(".JPG", "").replace(".jpg", "");
                    if (imgName.length() > 64) {
                        if (src.length() > 500) {
                            break;
                        }
                        imgName = imgName.substring(0, 63);
                    }
                    if (imgName.equals("r-login.html imgUrl=/index.php?s=/user-login.html")) {
                        log.warn("==>albumId={} 未加载完成", albumId);
                        is500 = true;
                        continue;
                    }

                    MeinvlaImageDO meinvlaImgDO = new MeinvlaImageDO();
                    meinvlaImgDO.setAlbumId(albumId);
                    meinvlaImgDO.setImgUrl(src);
                    meinvlaImgDO.setImgName(imgName);
                    meinvlaImgDO.setState(StateTypeEnum.STARTED.getSeq());

                    Map<String, Object> queryMap = new HashMap<>(1);
                    queryMap.put("img_name", imgName);
                    if (CollectionUtils.isEmpty(meinvlaImageMapper.selectByMap(queryMap))) {
                        meinvlaImageMapper.insert(meinvlaImgDO);
                        log.info("==>doScanImages albumId={} imgName={} imgUrl={}", albumId, imgName, src);
                    } else {
                        log.warn("记录已存在 albumId={} imgName={} imgUrl={}", albumId, imgName, src);
                    }
                }
                // update
                Map<String, Object> albumIdMap = new HashMap<>(1);
                albumIdMap.put("album_id", albumId);
                if (meinvlaImageMapper.selectByMap(albumIdMap).size() > 3 && !is500) {
                    vo.setState(StateTypeEnum.ANALYSIS.getSeq());
                } else {
                    log.warn("==>albumId={} 未达预期", albumId);
                    vo.setState(StateTypeEnum.EXCEPTION.getSeq());
                }
                vo.setTotal(elements.size());
                meinvlaAlbumMapper.updateById(vo);
            }
        });
        return "success";
    }

    @Override
    public String doDataClean() {
        // 500 => 404
//        QueryWrapper<MeinvlaAlbumDO> wrapper = new QueryWrapper<>();
//        wrapper.select()
//                .ne("state", StateTypeEnum.BLACKLIST.getSeq())
//                .in("type", MeinvlaTypeEnum.T184.getSeq(), MeinvlaTypeEnum.T191.getSeq());
//        meinvlaAlbumMapper.selectList(wrapper).forEach(vo -> {
//            final Integer albumId = vo.getAlbumId();
//            Map<String, Object> albumIdMap = new HashMap<>(1);
//            albumIdMap.put("album_id", albumId);
//            if ((meinvlaImageMapper.selectByMap(albumIdMap).size() == 0)) {
//                log.warn("==>albumId={} 404", albumId);
//                vo.setState(StateTypeEnum.NOTFOUND.getSeq());
//                meinvlaAlbumMapper.updateById(vo);
//            }
//        });

        // 刷新 cur_total
//        QueryWrapper<MeinvlaAlbumDO> wrapper = new QueryWrapper<>();
//        wrapper.select()
//                .ne("state", StateTypeEnum.BLACKLIST.getSeq())
//                .in("type", MeinvlaTypeEnum.T184.getSeq(), MeinvlaTypeEnum.T191.getSeq());
//        meinvlaAlbumMapper.selectList(wrapper).forEach(vo -> {
//            final Integer albumId = vo.getAlbumId();
//            int curTotal = meinvlaImageMapper.selectCount(new QueryWrapper<MeinvlaImageDO>().eq("album_id", albumId));
//            vo.setCurTotal(curTotal);
//            meinvlaAlbumMapper.updateById(vo);
//            log.info("==>doDataClean");
//        });
        return "success";
    }

    @Override
    public String doDownload() {
        ExecutorService service = Executors.newFixedThreadPool(16);

        QueryWrapper<MeinvlaAlbumDO> wrapper = new QueryWrapper<>();
        wrapper.select()
                .ne("state", StateTypeEnum.BLACKLIST.getSeq())
                .in("type", 87,89,95,96,97,112,113,116,117,118,119,120);
        meinvlaAlbumMapper.selectList(wrapper).forEach(vo -> {
            final int albumId = vo.getAlbumId();
            final String albumName = vo.getAlbumName();
            final int typeSeq = vo.getType();

            String localFolder = MEINVLA_LOCAL_PREFIX + MeinvlaTypeEnum.getEnumBySeq(typeSeq).getDesc() + "/" + albumName;
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }

            QueryWrapper<MeinvlaImageDO> wrapper2 = new QueryWrapper<>();
            wrapper2.select()
                    .eq("album_id", albumId)
                    .ne("state", StateTypeEnum.DONE.getSeq());
            meinvlaImageMapper.selectList(wrapper2).forEach(img -> {
                String onlinePath = "http:" + img.getImgUrl();
                String localPath = localFolder + "/" + img.getImgName() + ".jpg";

                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (file2.exists()) {
                    file2.delete();
                }
                service.execute(() -> {
                    // 下载中-便于线程宕掉后回溯
                    img.setState(StateTypeEnum.DOWNLOADING.getSeq());
                    meinvlaImageMapper.updateById(img);
                    // 下载
                    if (ReptileUtil.ioDownload(onlinePath, localPath)) {
                        img.setState(StateTypeEnum.DONE.getSeq());
                    } else {
                        img.setState(StateTypeEnum.STARTED.getSeq());
                    }
                    meinvlaImageMapper.updateById(img);
                });
            });

        });
        return "success";
    }

    /**
     * 去除不合法文件名
     */
    private String rmIllegalName(String s) {
        return FILE_PATTERN.matcher(s).replaceAll("");
    }

    /**
     * 线程睡眠
     *
     * @param seconds 秒
     */
    private void waitSeconds(int seconds) {
        try {
            log.info("==>waitSeconds {}s", seconds);
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String doScanVideo() {
        // 配置 chromedriver.exe 路径
        System.setProperty("webdriver.chrome.driver", "C:/Users/DEVYY/Documents/chromedriver_win32/chromedriver.exe");
        // Mac 用户
        // todo 这些配置，最好放到application.properties文件里
        // System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        // 启动一个 chrome 实例
        WebDriver webDriver = new ChromeDriver();
        // 设置超时时间为 30 s
        webDriver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
        webDriver.get("http://www.meinvla.net/user-login.html");
        // wait 30s 输入账号密码
        this.waitSeconds(35);

        // SELECT * FROM tbl_meinvla_album WHERE (total IS NULL AND state <> -1)
        QueryWrapper<MeinvlaAlbumDO> wrapper = new QueryWrapper<>();
        wrapper.select()
                .ne("state", StateTypeEnum.BLACKLIST.getSeq())
                .ne("state", StateTypeEnum.ANALYSIS.getSeq())
                .lt("type", 50);
        List<MeinvlaAlbumDO> meinvlaAlbumDOS = meinvlaAlbumMapper.selectList(wrapper);
        for (MeinvlaAlbumDO vo : meinvlaAlbumDOS) {
            final Integer albumId = vo.getAlbumId();
            // http://www.meinvla.net/play/6048211.html
            String url = "http://www.meinvla.net/play/" + albumId + ".html";

            try {
                log.info("==>url {}", url);
                webDriver.get(url);
                webDriver.switchTo().frame(webDriver.findElement(By.className("embed-responsive-item")));
            }
            // 此处捕获所有 Throwable 因为并不需要关心，还会中断程序
            catch (Throwable e) {
                log.warn(e.getMessage().substring(0, 30));
                continue;
            }

            Document document = Jsoup.parse(webDriver.getPageSource());

            if (Objects.nonNull(document)) {
                String src = document
                        .getElementsByClass("dplayer-video dplayer-video-current")
                        .first()
                        .attr("src");
                String imgName = src.replace("http://sp.10019.net/", "").replace(".mp4", "");
                if (imgName.length() <= 64) {
                    MeinvlaImageDO meinvlaImgDO = new MeinvlaImageDO();
                    meinvlaImgDO.setAlbumId(albumId);
                    meinvlaImgDO.setImgUrl(src);
                    meinvlaImgDO.setImgName(imgName);
                    meinvlaImgDO.setState(StateTypeEnum.STARTED.getSeq());

                    Map<String, Object> queryMap = new HashMap<>(1);
                    queryMap.put("img_name", imgName);
                    if (CollectionUtils.isEmpty(meinvlaImageMapper.selectByMap(queryMap))) {
                        meinvlaImageMapper.insert(meinvlaImgDO);
                        log.info("==>doScanImages albumId={} imgName={} imgUrl={}", albumId, imgName, src);
                    } else {
                        log.warn("记录已存在 albumId={} imgName={} imgUrl={}", albumId, imgName, src);
                    }
                }
                // update
                Map<String, Object> albumIdMap = new HashMap<>(1);
                albumIdMap.put("album_id", albumId);
                if (!CollectionUtils.isEmpty(meinvlaImageMapper.selectByMap(albumIdMap))) {
                    vo.setState(StateTypeEnum.ANALYSIS.getSeq());
                } else {
                    log.warn("==>albumId={} 未达预期", albumId);
                    vo.setState(StateTypeEnum.EXCEPTION.getSeq());
                }
                meinvlaAlbumMapper.updateById(vo);
            }
        }
        return "success";
    }

    @Override
    public String doDownloadVideo() {
        ExecutorService service = Executors.newFixedThreadPool(8);

        QueryWrapper<MeinvlaAlbumDO> wrapper = new QueryWrapper<>();
        wrapper.select()
                .ne("state", StateTypeEnum.BLACKLIST.getSeq())
                .lt("type", 50);
        meinvlaAlbumMapper.selectList(wrapper).forEach(vo -> {
            final int albumId = vo.getAlbumId();
            final String albumName = vo.getAlbumName();
            final int typeSeq = vo.getType();

            String localFolder = MEINVLA_LOCAL_PREFIX + MeinvlaTypeEnum.getEnumBySeq(typeSeq).getDesc();
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }

            QueryWrapper<MeinvlaImageDO> wrapper2 = new QueryWrapper<>();
            wrapper2.select()
                    .eq("album_id", albumId)
                    .ne("state", StateTypeEnum.DONE.getSeq());
            meinvlaImageMapper.selectList(wrapper2).forEach(img -> {
                String onlinePath = img.getImgUrl();
                String localPath = localFolder + "/" + albumName + ".mp4";

                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (file2.exists()) {
                    file2.delete();
                }
                service.execute(() -> {
                    // 下载中-便于线程宕掉后回溯
                    img.setState(StateTypeEnum.DOWNLOADING.getSeq());
                    meinvlaImageMapper.updateById(img);
                    // 下载
                    if (ReptileUtil.ioDownload(onlinePath, localPath)) {
                        img.setState(StateTypeEnum.DONE.getSeq());
                    } else {
                        img.setState(StateTypeEnum.STARTED.getSeq());
                    }
                    meinvlaImageMapper.updateById(img);
                });
            });

        });
        return "success";
    }
}
