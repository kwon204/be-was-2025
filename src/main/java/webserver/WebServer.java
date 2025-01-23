package webserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String args[]) throws Exception {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        createTables();

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                executor.execute(new RequestHandler(connection));
            }
            executor.shutdown();
        }
    }

    private static void createTables() {
        try(Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();

            String query = "create table if not exists  USERS (" +
                    "ID varchar(20) NOT NULL, " +
                    "NAME varchar(100) NOT NULL, " +
                    "PASSWORD varchar(20) NOT NULL, " +
                    "EMAIL varchar(320), " +
                    "PROFILE varchar(255), " +
                    "PRIMARY KEY (ID))";

            stmt.execute(query);

            query = "create table if not exists ARTICLE (" +
                    "ID int NOT NULL, " +
                    "CONTENT varchar(1000) NOT NULL, " +
                    "IMAGE varchar(255) NOT NULL, " +
                    "USER_ID varchar(20) NOT NULL, " +
                    "PRIMARY KEY (ID), " +
                    "FOREIGN KEY (USER_ID) REFERENCES USERS (ID))";

            stmt.execute(query);

            query = "create table if not exists COMMENT (" +
                    "ID int NOT NULL, " +
                    "CONTENT varchar(1000) NOT NULL, " +
                    "USER_ID varchar(20) NOT NULL, " +
                    "ARTICLE_ID int NOT NULL, " +
                    "PRIMARY KEY (ID), " +
                    "FOREIGN KEY (USER_ID) REFERENCES USERS (ID), " +
                    "FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID))";

            stmt.execute(query);

        } catch(SQLException e) {
            logger.error(e.getMessage());
        }
    }
}
