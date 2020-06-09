package com.hao;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        linkPool.add("https://news.sina.cn/");
        Set<String> processedLinks = new HashSet<>();

        while (true) {
            if (linkPool.size() == 0) {
                break;
            }
            String link = linkPool.remove(linkPool.size() - 1);

            //判断link是否被处理过
            if (processedLinks.contains(link)) {
                continue;
            }

            //是否是我们感兴趣的link
            if (isInserestedLink(link)) {
                System.out.println("link: " + link);
                Document doc = httpGetAndParseToDoc(link);
                // 将页面上所有的<a>连接放入pool中
                List<Element> aTags = doc.select("a");
                addLinkToPool(linkPool, aTags);

                if (isNewsPage(doc)) {
                    System.out.println(doc.selectFirst("h1").text());
                }
                processedLinks.add(link);
            }

        }
    }

    private static void addLinkToPool(List<String> linkPool, List<Element> aTags) {
        for (Element e : aTags) {
            String href = e.attr("href");
            if (href.startsWith("/") || href.startsWith("#") || href.isEmpty() || href.contains("javascript")) {
                continue;
            }
            if (href.contains("news.sina")) {
                linkPool.add(href);
            }
        }
    }

    private static Document httpGetAndParseToDoc(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try(CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8));
        }
    }


    private static boolean isNewsPage(Document document) {
        Elements articleBody = document.select("article");
        return !articleBody.isEmpty();
    }

    private static boolean isInserestedLink(String link) {
        return isSinaNewsPage(link) && isNotPassportLink(link) && isNotShtml(link);


    }

    private static boolean isNotShtml(String link) {
        return !link.endsWith("shtml");
    }

    private static boolean isNotPassportLink(String link) {
        return !link.contains("passport");
    }

    private static boolean isSinaNewsPage(String link) {
        return link.startsWith("https://news.sina")
                || link.startsWith("https://news.sina");
    }
}
