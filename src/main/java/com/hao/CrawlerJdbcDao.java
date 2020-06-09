package com.hao;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.sql.*;
import java.util.List;

public class CrawlerJdbcDao implements CrawlerDao{
    private Connection connection;

    public CrawlerJdbcDao() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:E:\\hcsp-java\\java-news-crawler\\src\\main\\resources\\db\\h2\\news",
                    "root",
                    "root");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void addLintToProcessed(String link) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("delete from LINK_ALREADY_PROCESSED where link = ?");
        preparedStatement.setString(1, link);
        preparedStatement.executeUpdate();
    }
    public void storeNewToDatabase(Document doc) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (title, content, created_at, updated_at) values (?, ?, now(), now())");
        String title = doc.select("h1").text();
        String content = doc.select(".art_content").text();
        preparedStatement.setString(1, title);
        preparedStatement.setString(2, content);
        preparedStatement.executeUpdate();
    }
    public boolean isLinkProcessed(String link) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select count(1) from LINK_ALREADY_PROCESSED where link = ?");
        preparedStatement.setString(1, link);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        return  count != 0;
    }
    public String getToBeProcessedLinkAndRemove() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select link from LINK_TOBE_PROCESSED limit 1");
        if (resultSet.next()) {
            String link =  resultSet.getString(1);
            if (link != null) {
                PreparedStatement preparedStatement = connection.prepareStatement("delete from LINK_TOBE_PROCESSED where link = ?");
                preparedStatement.setString(1, link);
                preparedStatement.executeUpdate();
            }
            return link;
        }
        return null;
    }

    public void storeLinkToBeProcess(List<Element> aTags) throws SQLException {
        String insertSql = "insert into LINK_TOBE_PROCESSED (link) values (?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
        for (Element e : aTags) {
            String href = e.attr("href");
            if (href.startsWith("/") || href.startsWith("#") || href.isEmpty() || href.contains("javascript")) {
                continue;
            }
            if (href.contains("news.sina")) {
                preparedStatement.setString(1, href);
                preparedStatement.executeUpdate();
            }
        }
    }
}
