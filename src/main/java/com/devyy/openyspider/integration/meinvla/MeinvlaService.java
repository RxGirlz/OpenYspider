package com.devyy.openyspider.integration.meinvla;

import com.devyy.openyspider.base.StateTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
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

    @Resource
    private IMeinvlaAlbumMapper meinvlaAlbumMapper;
    @Resource
    private IMeinvlaImageMapper meinvlaImageMapper;

    // 数量级：
    @Override
    public String doScanAlbums() {
        List<MeinvlaTypeEnum> typeEnumList = MeinvlaTypeEnum.getEnums();
        // 每一种相册类型
        for (MeinvlaTypeEnum typeEnum : typeEnumList) {
            // 设 1000 页为结束页，获取末页 id
            String urlEnd = String.format(Locale.ENGLISH, "http://www.meinvla.net/video/type%s/-----gold-1000.html", typeEnum.getSeq());
            Integer endAlbumId = null;
            Integer curAlbumId = null;
            try {
                Document document = Jsoup.connect(urlEnd).get();
                String endEffect5Href = document.getElementsByClass("index-body-nr-left-1-li xl6 xs4 xm4 xb3").last()
                        .getElementsByClass("effect5").first().attr("href");
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
                    for (Element element : document.getElementsByClass("index-body-nr-left-1-li xl6 xs4 xm4 xb3")) {// 相册名
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
        return "success";
    }

    @Override
    public String doScanImages() {
        // 配置 chromedriver.exe 路径
        System.setProperty("webdriver.chrome.driver", "C:/Users/DEVYY/Documents/chromedriver_win32/chromedriver.exe");
        // 启动一个 chrome 实例
        WebDriver webDriver = new ChromeDriver();
        // 设置超时时间为 10s
        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        webDriver.get("http://www.meinvla.net");
        // wait 35s 输入账号密码
        this.waitSeconds(30);

        Map<String, Object> findAllByState = new HashMap<>(1);
        findAllByState.put("state", StateTypeEnum.EXCEPTION.getSeq());
        meinvlaAlbumMapper.selectByMap(findAllByState).forEach(vo -> {
            final Integer albumId = vo.getAlbumId();
            // http://www.meinvla.net/play/6048211.html
            String url = "http://www.meinvla.net/play/" + albumId + ".html";

            try {
                webDriver.get(url);
            }
            // 此处捕获所有 Throwable 因为并不需要关心，还会中断程序
            catch (Throwable e) {
                log.warn(e.getMessage().substring(0, 30));
            }

            // wait 3s 加载动态页面
            this.waitSeconds(2);
            Document document = Jsoup.parse(webDriver.getPageSource());

            if (Objects.nonNull(document)) {
                Elements elements = document.getElementsByClass("img-responsive lazy img_lazy");
                for (Element element : elements) {
                    String src = element.getElementsByTag("img").first().attr("src");
                    // "//p.10019.net/tu/bcb2cf50509a1267f2b6d94f2530f5b814ApE01.jpg.jpg"
                    // 其中 "//p.10019.net/tu/" 长度为 17
                    String imgName = src.substring(17).replace(".JPG", "").replace(".jpg", "");
                    if (imgName.length() > 64) {
                        break;
                    }
                    if(imgName.equals("src=http://www.meinvla.net/Tpl/mm/style/index.gif&h=200&w=240")){
                        log.warn("==>albumId={} 未加载完成", albumId);
                        vo.setState(StateTypeEnum.STARTED.getSeq());
                        meinvlaAlbumMapper.updateById(vo);
                        break;
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
                if (meinvlaImageMapper.selectByMap(albumIdMap).size() > 3) {
                    vo.setState(StateTypeEnum.ANALYSIS.getSeq());
                } else {
                    log.warn("==>albumId={} 未达预期", albumId);
                    vo.setState(StateTypeEnum.EXCEPTION.getSeq());
                }
                meinvlaAlbumMapper.updateById(vo);
            }
        });
        return "success";
    }

    @Override
    public String doDataClean() {
        // 500 => 404
        Map<String, Object> findAllByState = new HashMap<>(1);
        findAllByState.put("state", StateTypeEnum.EXCEPTION.getSeq());
        meinvlaAlbumMapper.selectByMap(findAllByState).forEach(vo -> {
            final Integer albumId = vo.getAlbumId();
            Map<String, Object> albumIdMap = new HashMap<>(1);
            albumIdMap.put("album_id", albumId);
            if ((meinvlaImageMapper.selectByMap(albumIdMap).size() == 0)) {
                log.warn("==>albumId={} 404", albumId);
                vo.setState(StateTypeEnum.NOTFOUND.getSeq());
                meinvlaAlbumMapper.updateById(vo);
            }
        });
        return "success";
    }

    @Override
    public String doDownload() {
        Map<String, Object> findAllByState = new HashMap<>(1);
        findAllByState.put("state", StateTypeEnum.STARTED.getSeq());
        meinvlaImageMapper.selectByMap(findAllByState).forEach(image -> {
            Map<String, Object> findOneAlbumByAlbumId = new HashMap<>(1);
            findOneAlbumByAlbumId.put("album_id", image.getAlbumId());
            MeinvlaAlbumDO meinvlaAlbumDO = meinvlaAlbumMapper.selectByMap(findOneAlbumByAlbumId).get(0);
            String localFolder = MEINVLA_LOCAL_PREFIX + meinvlaAlbumDO.getAlbumName();
            // 若文件夹路径不存在，则新建
            File file = new File(localFolder);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    log.error("==>localFolder={} 创建文件路径失败", localFolder);
                }
            }
        });

//
//        meinvlaImageJpaDAO.findAll().forEach(vo -> {
//            if (Objects.equals(meinvlaAlbumJpaDAO.findById(vo.getAlbumId()).get().getAlbumType(), typeEnum.getType())) {
//                String onlinePath = "http:" + vo.getImgUrl();
//                String localPath = localFolder + "/" + vo.getImgName() + ".jpg";
//
//                // 幂等，若当前文件未下载，则进行下载
//                File file2 = new File(localPath);
//                if (!file2.exists()) {
//                    ReptileUtil.asyncDownload(onlinePath, localPath);
//                }
//            }
//
//        });
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
}
