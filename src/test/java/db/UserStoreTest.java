package db;

import model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class UserStoreTest {

    @BeforeAll
    public static void beforeAll() throws SQLException {
        Connection conn;
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
    }

    @Test
    @DisplayName("유저 정보 저장 테스트")
    public void test1() throws SQLException {
        User user = new User("test1", "1234", "테스트", "email@a.c", "image.g");

        UserStore.addUser(user);
    }

    @Test
    @DisplayName("유저 정보 가져오기 테스트")
    public void test2() throws SQLException {
        User user = new User("test1", "1234", "테스트", "email@a.c", "image.g");

        UserStore.addUser(user);

        Optional<User> userOpt = UserStore.findUserById("test1");

        assertThat(userOpt.isPresent()).isTrue();

        User findUser = userOpt.get();

        assertThat(findUser.getUserId()).isEqualTo("test1");
    }

    @Test
    @DisplayName("유저 정보 가져오기 예외 테스트")
    public void test3() throws SQLException {
        User user = new User("test1", "1234", "테스트", "email@a.c", "image.g");

        UserStore.addUser(user);

        Optional<User> userOpt = UserStore.findUserById("test2");

        assertThat(userOpt.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("모든 유저 정보 가져오기 테스트")
    public void test4() throws SQLException {
        User user1 = new User("test1", "1234", "테스트", "email1@a.c", "image.g");
        User user2 = new User("test2", "1234", "테스트", "email2@a.c", "image.g");

        UserStore.addUser(user1);
        UserStore.addUser(user2);

        List<User> users = UserStore.findAll();

        assertThat(users.size()).isGreaterThan(0).isEqualTo(2);
    }

}