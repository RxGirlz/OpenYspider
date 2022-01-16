package com.devyy.oys.tujidao;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Jsoup.class)
public class TuJiDaoServiceImplTest {
    @InjectMocks
    private final TuJiDaoServiceImpl tuJiDaoService = new TuJiDaoServiceImpl();

    @Test
    public void doSyncRecords() throws IOException {
        URL url = getClass().getResource("/gengxin.html");
        Assert.assertNotNull(url);
        File file = new File(url.getPath());
        Document document = Jsoup.parse(file, StandardCharsets.UTF_8.name());

        PowerMockito.mockStatic(Jsoup.class);
        PowerMockito.when(Jsoup.connect("null1").get()).thenReturn(document);

//        PowerMockito.when(Jsoup.connect(Mockito.anyString()).get()).thenReturn(document);
//        Mockito.when(Jsoup.connect(Mockito.anyString()).cookies(Mockito.anyMap()).get()).thenReturn(document);

        tuJiDaoService.doSyncRecords();

        PowerMockito.verifyStatic(Jsoup.class);

    }
}