package db;

import model.Article;
import model.Comment;
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

class CommentStoreTest {
    Connection conn;

    private User user;

    private Article article;

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
                "IMAGE varchar(255) NOT NULL, " +
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

        article = new Article("testArticle", user, "image.g");
        ArticleStore.addArticle(article);
    }

    @Test
    @DisplayName("댓글 추가하기 테스트")
    public void test1() throws SQLException {
        Comment comment = new Comment("test comment", user, article);

        CommentStore.addComment(comment);

        Optional<Comment> commentOpt = CommentStore.findCommentById(comment.getCommentId());

        assertThat(commentOpt.isPresent()).isTrue();
    }

    @Test
    @DisplayName("특정 게시글의 댓글 가져오기 테스트")
    public void test2() throws SQLException {
        Comment comment1 = new Comment("test comment", user, article);
        Comment comment2 = new Comment("test comment2", user, article);

        CommentStore.addComment(comment1);
        CommentStore.addComment(comment2);

        List<Comment> comments = CommentStore.findAllByArticle(article.getArticleId());

        assertThat(comments.size()).isGreaterThan(0).isEqualTo(2);

        Comment findComment1 = comments.get(0);
        Comment findComment2 = comments.get(1);

        assertThat(findComment1).isNotNull();
        assertThat(findComment2).isNotNull();

        assertThat(findComment2.getCommentId()).isGreaterThan(findComment1.getCommentId());
    }

    @AfterEach
    public void cleanUp() throws SQLException {
        conn.close();
    }
}