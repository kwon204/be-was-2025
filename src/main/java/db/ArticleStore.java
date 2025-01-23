package db;

import model.Article;
import model.Comment;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.exception.UserNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArticleStore {
    private static final Logger logger = LoggerFactory.getLogger(ArticleStore.class);

    public static void addArticle(Article article) {
        String sql = "insert into ARTICLE values(?, ?, ?, ?)";
        try (Connection conn = Database.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, article.getArticleId());
            pstmt.setString(2, article.getContent());
            pstmt.setString(3, article.getImage());
            pstmt.setString(4, article.getUser().getUserId());

            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static Optional<Article> findArticleById(int articleId) {
        String sql = "select id, content, user_id, image from ARTICLE where id=?";
        try (Connection conn = Database.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, articleId);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.first()) {
                return Optional.empty();
            }
            int id = rs.getInt("id");
            String content = rs.getString("content");
            String userId = rs.getString("user_id");
            String image = rs.getString("image");

            User user = UserStore.findUserById(userId).orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

            List<Comment> comments = CommentStore.findAllByArticle(id);

            rs.close();
            pstmt.close();
            conn.close();
            return Optional.of(new Article(id, content, user, comments, image));
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    public static List<Article> findAll() {
        List<Article> articles = new ArrayList<>();
        String sql = "select id, content, image, user_id from ARTICLE ORDER BY id desc";
        try (Connection conn = Database.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("content");
                String userId = rs.getString("user_id");
                String image = rs.getString("image");

                User user = UserStore.findUserById(userId).orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

                List<Comment> comments = CommentStore.findAllByArticle(id);

                Article article = new Article(id, content, user, comments, image);
                articles.add(article);
            }
            rs.close();
            pstmt.close();
            conn.close();
            return articles;
        } catch (SQLException e){
            logger.error(e.getMessage());
        }
        return articles;
    }
}
