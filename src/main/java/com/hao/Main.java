package com.hao;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.h2.jdbc.JdbcBlob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:file:E:\\hcsp-java\\java-news-crawler\\src\\main\\resources\\db\\h2\\news",
                "root",
                "root");

        Set<String> processedLinks = new HashSet<>();
        String link;
        while ((link = getToBeProcessedLinkAndRemove(connection)) != null) {

            //判断link是否被处理过
            if (isLinkProcessed(connection, link)) {
                continue;
            }

            //是否是我们感兴趣的link
            if (isInserestedLink(link)) {
                System.out.println("link: " + link);
                Document doc = httpGetAndParseToDoc(link);
                // 将页面上所有的<a>连接放入pool中
                List<Element> aTags = doc.select("a");
                storeLinkToBeProcess(connection, aTags);

                if (isNewsPage(doc)) {
                    storeNewToDatabase(connection, doc);
                }
                addLintToProcessed(connection, link);
            }

        }
    }

    private static void addLintToProcessed(Connection connection, String link) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("delete from LINK_ALREADY_PROCESSED where link = ?");
        preparedStatement.setString(1, link);
        preparedStatement.executeUpdate();
    }

    private static void storeNewToDatabase(Connection connection, Document doc) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (title, content, created_at, updated_at) values (?, ?, now(), now())");
        String title = doc.select("h1").text();
        String content = doc.select(".art_content").text();
        preparedStatement.setString(1, title);
        preparedStatement.setString(2, content);
        preparedStatement.executeUpdate();
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select count(1) from LINK_ALREADY_PROCESSED where link = ?");
        preparedStatement.setString(1, link);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        return  count != 0;
    }

    private static String getToBeProcessedLinkAndRemove(Connection connection) throws SQLException {
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

    private static void storeLinkToBeProcess(Connection connection, List<Element> aTags) throws SQLException {
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
