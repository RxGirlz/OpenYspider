package com.devyy.oys.srarter.main;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.normalize.UpperCaseNormalizer;
import com.kennycason.kumo.palette.ColorPalette;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @since 2021-05-01
 */
public class JavMain {
    /**
     * Jav 文件夹目录
     */
    private static final String JAV_BASE_DIR = "D:\\GITHUB\\Jav";
    /**
     * CMD 输出命令
     * dir /b > jav20220603.txt
     */
    private static final String JAV_FILE_NAME = "jav20220603";
    private static final String JAV_INPUT_FILE = String.format(Locale.ENGLISH, "%s/%s.txt", JAV_BASE_DIR, JAV_FILE_NAME);
    private static final String JAV_OUTPUT_FILE = String.format(Locale.ENGLISH, "%s/%s.sh", JAV_BASE_DIR, JAV_FILE_NAME);

    public static void main(String[] args) {
        // 打印命令行
        doPrintCommand();
        // 分析
//        doAnalyse();
        // 查重
//        doFindRepeat();
        // 词云
//        doWordCloud();
    }

    /**
     * 打印 echo 番号 shell 脚本
     */
    private static void doPrintCommand() {
        try {
            File inputFile = new File(JAV_INPUT_FILE);
            List<String> fanHao = FileUtils.readLines(inputFile, StandardCharsets.UTF_8.name());
            List<String> cmdList = fanHao.stream()
                    // 过滤掉 jav202xxxxx.txt
                    .filter(name -> !name.endsWith(".txt"))
                    .map(name -> String.format(Locale.ENGLISH, "echo > %s.txt", name))
                    .collect(Collectors.toList());
            File outputFile = new File(JAV_OUTPUT_FILE);
            FileUtils.writeLines(outputFile, cmdList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Stream<String> getJavStream() {
        File directory = new File(JAV_BASE_DIR);
        // 递归遍历 Jav txt 文件
        Collection<File> avFiles = FileUtils.listFiles(directory, new String[]{"txt"}, true);
        return avFiles.stream().map(File::getName);
    }

    /**
     * 统计分析 Jav 目录索引
     */
    private static void doAnalyse() {
        long totals = getJavStream().map(name -> name.split("-")[0]).count();
        System.out.printf(Locale.ENGLISH, "Jav totals: %s, Ranks:%n", totals);
        getJavStream()
                .map(name -> name.split("-")[0])
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
//                .filter(entry -> entry.getValue() >= 5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEach(System.out::println);
    }

    /**
     * 查重
     */
    private static void doFindRepeat() {
        getJavStream()
                .map(name -> name.split("\\.")[0])
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(System.out::println);
    }

    /**
     * 输出词云
     */
    private static void doWordCloud() {
        List<String> avPrefix = getJavStream()
                .map(name -> name.split("-")[0])
//                .filter(str -> !str.equals("SSNI") && !str.equals("IPX"))
                .collect(Collectors.toList());

        final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setNormalizer(new UpperCaseNormalizer());
        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(avPrefix);
        final Dimension dimension = new Dimension(600, 600);
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(0);
        wordCloud.setBackground(new CircleBackground(300));
        wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
        wordCloud.setFontScalar(new LinearFontScalar(20, 160));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile(JAV_BASE_DIR + "/jav-wordCloud.png");
    }
}
