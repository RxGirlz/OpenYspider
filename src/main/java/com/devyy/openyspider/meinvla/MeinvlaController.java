package com.devyy.openyspider.meinvla;

import com.devyy.openyspider.common.ReptileUtil;
import com.devyy.openyspider.meinvla.dao.MeinvlaAlbumJpaDAO;
import com.devyy.openyspider.meinvla.dao.MeinvlaImageJpaDAO;
import com.devyy.openyspider.meinvla.model.MeinvlaAlbumDO;
import com.devyy.openyspider.meinvla.model.MeinvlaImgDO;
import com.devyy.openyspider.meinvla.model.MeinvlaTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * http://www.meinvla.net/
 *
 * @since 2019-11-23
 */
@RestController
@RequestMapping("/meinvla")
@Slf4j
public class MeinvlaController {

    private static final String HOST = "http://www.meinvla.net";
    /**
     * 文件不合法名正则
     */
    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|\\[\\]_]");

    private static final String MEINVLA_LOCAL_PREFIX = "D:/Meinvla爬虫/";

    private static final MeinvlaTypeEnum typeEnum = MeinvlaTypeEnum.TYPE_186;

    @Autowired
    private MeinvlaAlbumJpaDAO meinvlaAlbumJpaDAO;
    @Autowired
    private MeinvlaImageJpaDAO meinvlaImageJpaDAO;

    @PostMapping("/step1")
    public String step1() {
        this.getUrls(typeEnum).forEach(url -> {
            Document document = null;
            try {
                document = Jsoup.connect(HOST + url).get();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (Objects.nonNull(document)) {
                document.getElementsByClass("index-body-nr-left-1-li xl6 xs4 xm4 xb3").forEach(element -> {
                    String titleTmp = element.getElementsByClass("effect5").first().attr("title");
                    String title = rmIllegalName(titleTmp);
                    String href = element.getElementsByClass("effect5").first().attr("href");

                    MeinvlaAlbumDO meinvlaAlbumDO = new MeinvlaAlbumDO();
                    meinvlaAlbumDO.setAlbumName(title);
                    meinvlaAlbumDO.setAlbumType(typeEnum.getType());
                    meinvlaAlbumDO.setAlbumUrl(href);
                    if (Objects.isNull(meinvlaAlbumJpaDAO.findByAlbumUrlEquals(meinvlaAlbumDO.getAlbumUrl()))) {
                        meinvlaAlbumJpaDAO.save(meinvlaAlbumDO);
                        log.info("==>meinvla title={} href={}", title, href);
                    } else {
                        log.warn("记录已存在 meinvla title={} href={}", title, href);
                    }
                });
            }
        });

        return "success";
    }

    @PostMapping("/step2")
    public String step2() {
        meinvlaAlbumJpaDAO.findAll().forEach(vo -> {
            if (vo.getAlbumType().equals(typeEnum.getType())) {
                Document document = null;
                try {
                    document = Jsoup.connect(HOST + vo.getAlbumUrl()).get();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                if (Objects.nonNull(document)) {
                    document.getElementsByClass("img-responsive lazy img_lazy").forEach(element -> {
                        String src = element.getElementsByTag("img").first().attr("src");
                        String name = src.substring(17).replace(".JPG", "").replace(".jpg", "");

                        MeinvlaImgDO meinvlaImgDO = new MeinvlaImgDO();
                        meinvlaImgDO.setAlbumId(vo.getId());
                        meinvlaImgDO.setImgUrl(src);
                        meinvlaImgDO.setImgName(name);

                        if (Objects.isNull(meinvlaImageJpaDAO.findByImgUrlEquals(meinvlaImgDO.getImgUrl()))) {
                            meinvlaImageJpaDAO.save(meinvlaImgDO);
                            log.info("==>meinvla{} name={} src={}", vo.getId(), name, src);
                        } else {
                            log.warn("记录已存在 meinvla{} name={} src={}", vo.getId(), name, src);
                        }
                    });
                }
            }

        });
        return "success";
    }

    @PostMapping("/step3")
    public String step3() {
        String localFolder = MEINVLA_LOCAL_PREFIX + typeEnum.getDesc();
        // 若文件夹路径不存在，则新建
        File file = new File(localFolder);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                log.error("==>localFolder={} 创建文件路径失败", localFolder);
            }
        }

        meinvlaImageJpaDAO.findAll().forEach(vo -> {
            if (Objects.equals(meinvlaAlbumJpaDAO.findById(vo.getAlbumId()).get().getAlbumType(), typeEnum.getType())) {
                String onlinePath = "http:" + vo.getImgUrl();
                String localPath = localFolder + "/" + vo.getImgName() + ".jpg";

                // 幂等，若当前文件未下载，则进行下载
                File file2 = new File(localPath);
                if (!file2.exists()) {
                    ReptileUtil.asyncDownload(onlinePath, localPath);
                }
            }

        });
        return "success";
    }

    private List<String> getUrls(MeinvlaTypeEnum type) {
        List<String> urls = new ArrayList<>();
        for (int i = 1; i <= type.getPageNum(); i++) {
            urls.add(String.format("/video/%s/-----gold-%s.html", type.getType(), i));
        }
        return urls;
    }

    /**
     * 去除不合法文件名
     */
    private String rmIllegalName(String s) {
        return FILE_PATTERN.matcher(s).replaceAll("");
    }
}
