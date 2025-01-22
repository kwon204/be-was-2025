package db;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserStore {
    private static final Logger logger = LoggerFactory.getLogger(UserStore.class);

    public static void addUser(User user) {
        String sql = "insert into USERS values(?, ?, ?, ?)";
        try (Connection conn = Database.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getEmail());

            pstmt.executeUpdate();

            pstmt.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static Optional<User> findUserById(String userId) {
        String sql = "select id, name, password, email from USERS where id=?";
        try (Connection conn = Database.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.first()) {
                return Optional.empty();
            }
            String id = rs.getString("id");
            String name = rs.getString("name");
            String password = rs.getString("password");
            String email = rs.getString("email");

            rs.close();
            pstmt.close();
            conn.close();
            return Optional.of(new User(id, password, name, email));
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    public static List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "select id, name, password, email from USERS";
        try (Connection conn = Database.getConnection()){
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String email = rs.getString("email");
                User user = new User(id, password, name, email);
                users.add(user);
            }

            rs.close();
            stmt.close();
            conn.close();

            return users;
        } catch(SQLException e) {
            logger.error(e.getMessage());
        }
        return users;
    }
}
