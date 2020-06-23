package com.hao;

import com.hao.dao.CrawlerDao;
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
import java.sql.SQLException;
import java.util.List;

public class Crawler extends Thread {
    private final CrawlerDao crawlerDao;

    public Crawler(CrawlerDao crawlerDao) {
        this.crawlerDao = crawlerDao;
    }

    public void run() {
        String link;
        try {
            while ((link = crawlerDao.getAndRemoveToBeProcessedLink()) != null) {
                //判断link是否被处理过
                if (crawlerDao.isLinkAlreadyProcessed(link)) {
                    continue;
                }
                //是否是我们感兴趣的link
                if (isInserestedLink(link)) {
                    System.out.println("Thread: " + this.getName() + " link: " + link);
                    Document doc = httpGetAndParseToDoc(link);
                    // 将页面上所有的<a>连接放入pool中
                    List<Element> aTags = doc.select("a");
                    crawlerDao.addATagLinkToBeProcessed(doc);
                    if (isNewsPage(doc)) {
                        crawlerDao.storeNews(doc);
                    }
                }
                crawlerDao.addLinkAlreadyProcessed(link);
            }
        } catch (SQLException | IOException throwables) {
            throw new RuntimeException(throwables);
        }
    }


    private Document httpGetAndParseToDoc(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8));
        }
    }


    private boolean isNewsPage(Document document) {
        Elements articleBody = document.select("article");
        return !articleBody.isEmpty();
    }

    private boolean isInserestedLink(String link) {
        return isSinaNewsPage(link) && isNotPassportLink(link) && isNotShtml(link);
    }

    private boolean isNotShtml(String link) {
        return !link.endsWith("shtml");
    }

    private boolean isNotPassportLink(String link) {
        return !link.contains("passport");
    }

    private boolean isSinaNewsPage(String link) {
        return link.startsWith("https://news.sina")
                || link.startsWith("https://news.sina");
    }
}
