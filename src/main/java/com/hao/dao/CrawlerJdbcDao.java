package com.hao.dao;

import org.jsoup.nodes.Document;

import java.sql.*;

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

    public void addLinkAlreadyProcessed(String link) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into LINK_ALREADY_PROCESSED (LINK) values (?)");
        preparedStatement.setString(1, link);
        preparedStatement.executeUpdate();
    }

    @Override
    public void addLinkToBeProcessed(String link) throws SQLException {
        String insertSql = "insert into LINK_TOBE_PROCESSED (link) values (?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
        preparedStatement.setString(1, link);
        preparedStatement.executeUpdate();
    }

    public void storeNews(Document doc) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (title, content, created_at, updated_at) values (?, ?, now(), now())");
        String title = doc.select("h1").text();
        String content = doc.select(".art_content").text();
        preparedStatement.setString(1, title);
        preparedStatement.setString(2, content);
        preparedStatement.executeUpdate();
    }
    public boolean isLinkAlreadyProcessed(String link) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select count(1) from LINK_ALREADY_PROCESSED where link = ?");
        preparedStatement.setString(1, link);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        return  count != 0;
    }
    public String getAndRemoveToBeProcessedLink() throws SQLException {
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

}
