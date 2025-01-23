package db;

import model.Article;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class ArticleStoreTest {

    Connection conn;

    private User user;

    @BeforeEach
    public void setUp() throws SQLException {
        conn = Database.getConnection();

        Statement stmt = conn.createStatement();

        String query = "create table if not exists  USERS (" +
                "ID varchar(10) NOT NULL, " +
                "NAME varchar(100) NOT NULL, " +
                "PASSWORD varchar(100) NOT NULL, " +
                "EMAIL varchar(320), " +
                "PROFILE varchar(255), " +
                "PRIMARY KEY (ID))";

        stmt.execute(query);

        query = "create table if not exists ARTICLE (" +
                "ID int NOT NULL, " +
                "CONTENT varchar(1000) NOT NULL, " +
                "USER_ID varchar(10) NOT NULL, " +
                "PRIMARY KEY (ID), " +
                "FOREIGN KEY (USER_ID) REFERENCES USERS (ID))";

        stmt.execute(query);

        query = "create table if not exists COMMENT (" +
                "ID int NOT NULL, " +
                "CONTENT varchar(1000) NOT NULL, " +
                "USER_ID varchar(10) NOT NULL, " +
                "ARTICLE_ID int NOT NULL, " +
                "PRIMARY KEY (ID), " +
                "FOREIGN KEY (USER_ID) REFERENCES USERS (ID), " +
                "FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID))";

        stmt.execute(query);

        user = new User("testUser", "1234", "testUser", "abc@a.c", "image.g");
        UserStore.addUser(user);
        stmt.close();
    }

    @Test
    @DisplayName("게시글 등록 테스트")
    public void test1() {
        Article article = new Article("test content", user);

        ArticleStore.addArticle(article);

        Optional<Article> articleOpt = ArticleStore.findArticleById(article.getArticleId());

        assertThat(articleOpt.isPresent()).isTrue();

        Article findArticle = articleOpt.get();

        assertThat(findArticle).isNotNull();
        assertThat(findArticle.getArticleId()).isEqualTo(article.getArticleId());
        assertThat(findArticle.getUser().getUserId()).isEqualTo(article.getUser().getUserId());
    }

    @Test
    @DisplayName("모든 게시글 보기 테스트")
    public void test2() {
        Article article1 = new Article("test content1", user);
        Article article2 = new Article("test content2", user);


        ArticleStore.addArticle(article1);
        ArticleStore.addArticle(article2);

        List<Article> articleList = ArticleStore.findAll();

        assertThat(articleList.size()).isGreaterThan(0).isEqualTo(2);

        Article findArticle1 = articleList.get(0);
        Article findArticle2 = articleList.get(1);

        assertThat(findArticle1.getArticleId()).isGreaterThan(findArticle2.getArticleId());
    }

    @AfterEach
    public void cleanUp() throws SQLException{
        conn.close();
    }
}