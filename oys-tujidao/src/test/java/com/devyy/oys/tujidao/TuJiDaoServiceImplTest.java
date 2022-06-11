package com.devyy.oys.tujidao;

import com.devyy.oys.tujidao.dao.TuJiDaoAlbumMapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * TuJiDaoServiceImpl UT
 *
 * @since 2022-01-18
 */
@ExtendWith(MockitoExtension.class)
public class TuJiDaoServiceImplTest {
    @InjectMocks
    private final TuJiDaoServiceImpl tuJiDaoService = new TuJiDaoServiceImpl();

    @Mock
    private TuJiDaoAlbumMapper tuJiDaoAlbumMapper;

    @Test
    public void doSyncRecords() throws IOException {
        // mock 网页
        URL url = getClass().getResource("/gengxin2.html");
        Assertions.assertNotNull(url);
        File file = new File(url.getPath());
        Document document = Jsoup.parse(file, StandardCharsets.UTF_8.name());

        // mock Jsoup.connect().cookies().get()
        MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class, Mockito.RETURNS_DEEP_STUBS);
        jsoupMockedStatic.when(() -> Jsoup.connect(Mockito.anyString()).cookies(Mockito.anyMap()).get())
                .thenReturn(document);

        // mock tuJiDaoAlbumMapper.selectByMap()
        Mockito.when(tuJiDaoAlbumMapper.selectByMap(Mockito.anyMap())).thenReturn(new ArrayList<>());

        // mock tuJiDaoAlbumMapper.insert()
        Mockito.when(tuJiDaoAlbumMapper.insert(Mockito.any())).thenReturn(1);

        // doTest
        tuJiDaoService.doSyncRecords();
        // verify
        jsoupMockedStatic.verify(() -> Jsoup.connect(Mockito.anyString()), Mockito.atLeastOnce());
        Mockito.verify(tuJiDaoAlbumMapper, Mockito.atLeastOnce()).insert(Mockito.any());
    }
}