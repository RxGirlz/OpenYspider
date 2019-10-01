package com.devyy.openyspider.tangyun;

import com.devyy.openyspider.common.ReptileUtil;
import com.devyy.openyspider.tangyun.model.TangYunAlbumDO;
import com.devyy.openyspider.tangyun.dao.TangYunAlbumJpaDAO;
import com.devyy.openyspider.tangyun.model.TangYunTypeEnum;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 美图日-爬虫 HTTP 调用接口
 *
 * @author zhangyiyang
 * @since 2019-06-20
 */
@RestController
@RequestMapping("/tangyun")
public class TangYunController {

    private static final Logger logger = LoggerFactory.getLogger(TangYunController.class);

    @Autowired
    private TangYunAlbumJpaDAO albumJpaDAO;

    /**
     * 正则表达式 2019-05-03/smallac046d1afaca1115f61f6357c297b786.jpg
     */
    private static final String PATTENS = "((?:19|20)\\d\\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])/(small)*[a-f0-9]{32}.jpg";
    /**
     * 唐韵文化-本地存储路径前缀
     */
    private static final String TANGYUN_LOCAL_PREFIX = "F:/唐韵爬虫/电子杂志/";

    /**
     * 根据 SQL 文件下载图片（SQL 文件数据获取方式略）
     */
    @PostMapping("/step1")
    public String step1() throws IOException {

        List<TangYunAlbumDO> albumDOList = albumJpaDAO.findAll();

        for (TangYunAlbumDO albumDO : albumDOList) {
            // 文件名
            String fileName = albumDO.getName();
            // 线上相册路径
            String url = TangYunTypeEnum.getEnumBySeq(albumDO.getSourceType()).getWebUrl() + albumDO.getUrl();

            this.doBatchDownload(fileName, url);
        }

        return "success";
    }

    /**
     * 批量下载
     *
     * @param name 文件名
     * @param url  线上相册路径
     */
    private void doBatchDownload(String name, String url) throws IOException {
        logger.info("==>doBatchDownload name={},url={}", name, url);
        // 若文件夹路径不存在，则新建
        File file = new File(TANGYUN_LOCAL_PREFIX + name);
        if (!file.exists()) {
            file.mkdirs();
        }

        // Jsoup 获取 HTML 并解析
        Document document = Jsoup.connect(url).get();
        String[] jpgUrls = this.convertToStrArr(PATTENS, document.outerHtml());

        // HashSet 去重
        Set<String> urlSet = new HashSet<>();
        for (String jpgUrl : jpgUrls) {
            String trustUrl = jpgUrl.replaceAll("small", "");
            urlSet.add(trustUrl);
        }
        for (String s : urlSet) {
            String onlinePath = TangYunTypeEnum.MAGEZINE.getDesc() + s;
            String localPath = TANGYUN_LOCAL_PREFIX + name + "/" + s
                    .replaceAll("/", "-");

            // 幂等，若当前文件为下载，则进行下载
            File file2 = new File(localPath);
            if (!file2.exists()) {
                ReptileUtil.syncDownload(onlinePath, localPath);
                logger.info("==>step8() localPath={} 下载成功", localPath);
            }
        }
    }

    private String[] convertToStrArr(String patternStr, String inputStr) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inputStr);
        ArrayList<String> tmpList = new ArrayList<>();
        while (matcher.find()) {
            tmpList.add(matcher.group());
        }
        String[] res = new String[tmpList.size()];
        int i = 0;
        for (String tmp : tmpList) {
            res[i] = tmp;
            i++;
        }
        return res;
    }
}
