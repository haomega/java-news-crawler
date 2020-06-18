package com.hao.dao;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.sql.SQLException;
import java.util.List;

public interface CrawlerDao {
    void addLinkToProcessed(String link) throws SQLException;

    void storeNewToDatabase(Document doc) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    String getToBeProcessedLinkAndRemove() throws SQLException;

    void storeLinkToBeProcess(List<Element> aTags) throws SQLException;
}
