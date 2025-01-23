package db;

import model.Comment;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.exception.InternalServerException;
import util.exception.UserNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentStore {
    private static final Logger logger = LoggerFactory.getLogger(CommentStore.class);

    public static void addComment(Comment comment) {
        String sql = "insert into COMMENT values(?, ?, ?, ?)";
        try (Connection conn = Database.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, comment.getCommentId());
            pstmt.setString(2, comment.getContent());
            pstmt.setString(3, comment.getUser().getUserId());
            pstmt.setInt(4, comment.getArticle().getArticleId());

            pstmt.executeUpdate();

            pstmt.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new InternalServerException(e.getMessage());
        }
    }

    public static Optional<Comment> findCommentById(int commentId) {
        String sql = "select id, content, user_id, article_id from COMMENT where id=?";

        try (Connection conn = Database.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentId);

            ResultSet rs = pstmt.executeQuery();
            if(!rs.first()) {
                return Optional.empty();
            }
            int id = rs.getInt("id");
            String content = rs.getString("content");
            String userId = rs.getString("user_id");

            User user = UserStore.findUserById(userId)
                    .orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

            rs.close();
            pstmt.close();
            conn.close();
            return Optional.of(new Comment(id, content, user));
        } catch(SQLException e) {
            logger.error(e.getMessage());
            throw new InternalServerException(e.getMessage());
        }
    }

    public static List<Comment> findAllByArticle(int articleId) {
        String sql = "select id, content, user_id, article_id from COMMENT where article_id=? order by id asc";
        List<Comment> comments = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, articleId);

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("content");
                String userId = rs.getString("user_id");

                User user = UserStore.findUserById(userId)
                        .orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

                Comment comment = new Comment(id, content, user);
                comments.add(comment);
            }

            rs.close();
            pstmt.close();
            conn.close();
            return comments;
        } catch(SQLException e) {
            logger.error(e.getMessage());
            throw new InternalServerException(e.getMessage());
        }
    }

    public static List<Comment> findAll() {
        List<Comment> comments = new ArrayList<>();
        String sql = "select id, content, user_id from COMMENT ORDER BY id desc";
        try (Connection conn = Database.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("content");
                String userId = rs.getString("user_id");

                User user = UserStore.findUserById(userId).orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

                Comment comment = new Comment(id, content, user);
                comments.add(comment);
            }
            return comments;
        } catch (SQLException e){
            logger.error(e.getMessage());
            throw new InternalServerException(e.getMessage());
        }
    }
}
