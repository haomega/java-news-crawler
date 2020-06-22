package com.hao.dao;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.sql.SQLException;

public interface CrawlerDao {
    String getAndRemoveToBeProcessedLink() throws SQLException;

    boolean isLinkAlreadyProcessed(String link) throws SQLException;

    default void addATagLinkToBeProcessed(Document document) throws SQLException{
        for (Element e : document.select("a")) {
            String href = e.attr("href");
            if (!href.startsWith("/") && !href.startsWith("#") && !href.isEmpty() && !href.contains("javascript")) {
                if (href.contains("news.sina")) {
                    addLinkToBeProcessed(href);
                }
            }
        }
    }

    void storeNews(Document doc) throws SQLException;

    void addLinkAlreadyProcessed(String link) throws SQLException;

    void addLinkToBeProcessed(String link) throws SQLException;
}
