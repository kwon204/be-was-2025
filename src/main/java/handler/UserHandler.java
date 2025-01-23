package handler;

import db.UserStore;
import db.SessionStore;
import http.constant.HttpHeader;
import http.HttpRequest;
import http.HttpResponse;
import model.Session;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Cookie;
import util.RequestParser;
import util.SessionUtils;
import util.exception.UserNotFoundException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

    public UserHandler() {
    }

    public void createUser(HttpRequest request, HttpResponse response) throws UnsupportedEncodingException {
        Map<String, String> data = RequestParser.parseBody(new String(request.getBody()));
        String userId = data.get("userId");
        String username = data.get("username");
        String password = data.get("password");

        if (!validateRegistrationInfo(userId, username, password)) {
            response.redirect("/registration/failed.html");
            return;
        }

        UserStore.findUserById(userId) .ifPresentOrElse(user -> {
                response.redirect("/registration/failed.html");
            }, () -> {
                User user = new User(userId, password, username, "", null);
                UserStore.addUser(user);

                response.redirect("/");
            }
        );

    }

    public void loginUser(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> data = RequestParser.parseRequestBody(request);

        String userId = data.get("userId");
        String password = data.get("password");

        if (!validateLoginInfo(userId, password)) {
            response.redirect("/login/failed.html");
            return;
        }

        UserStore.findUserById(userId).ifPresentOrElse(user -> {
                if (!user.getPassword().equals(password)) {
                    logger.debug("password not equal");
                    response.redirect("/login/failed.html");
                    return;
                }

                Cookie cookie = new Cookie();
                cookie.setPath("/");
                cookie.setMaxAge(360);
                cookie.setHttpOnly(true);
                Session session = new Session(cookie.getValue(), user.getUserId());

                SessionStore.addSession(cookie.getValue(), session);

                response.writeHeader(HttpHeader.SET_COOKIE, cookie.createCookieString());
                response.redirect("/main");
            }, () -> {
            logger.debug("user not found");
                response.redirect("/login/failed.html");
            }
        );
    }

    public void logoutUser(HttpRequest request, HttpResponse response) {
        Session session = SessionUtils.findSession(request);

        SessionStore.deleteBySessionId(session.sessionId());

        response.redirect("/");
    }

    public void profileUser(HttpRequest request, HttpResponse response) throws IOException {
        if (!SessionUtils.isLogin(request)) {
            response.redirect("/");
            return;
        }

        Session session = SessionUtils.findSession(request);
        User user = UserStore.findUserById(session.userId())
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

        Map<String, String> data = RequestParser.parseRequestBody(request);

        String filePath = data.get("image");

        UserStore.updateUserProfileImage(user, filePath);


        response.redirect("/mypage");
    }

    public void changeUser(HttpRequest request, HttpResponse response) throws IOException {
        if (!SessionUtils.isLogin(request)) {
            response.redirect("/");
            return;
        }

        Session session = SessionUtils.findSession(request);
        User user = UserStore.findUserById(session.userId())
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

        Map<String, String> data = RequestParser.parseRequestBody(request);

        String username = data.get("username");
        String password = data.get("password");
        String passwordRepeat = data.get("password_repeat");

        if (validateUsername(username)){
            UserStore.updateUsername(user, username);
        }
        if (validatePasswordChange(password, passwordRepeat)) {
            UserStore.updateUserPassword(user, password);
        }
        response.redirect("/main");
    }

    private boolean validatePasswordChange(String password, String passwordRepeat) {
        if (!validatePassword(password) || !validatePassword(passwordRepeat)) {
            return false;
        }

        if (!password.equals(passwordRepeat)) {
            return false;
        }
        return true;
    }

    private boolean validateRegistrationInfo(String userId, String username, String password) {
        return validateUserId(userId) && validatePassword(password) && validateUsername(username);
    }

    private boolean validateLoginInfo(String userId, String password) {
        return validateUserId(userId) && validatePassword(password);
    }

    private boolean validatePassword(String password) {
        if (password == null) return false;
        if (password.isBlank() || !password.matches("^[a-zA-Z\\d!@#$%^&*(),.?\":{}|<>]+$") || password.length() > 20) {
            return false;
        }
        return true;
    }

    private boolean validateUserId(String userId) {
        if (userId == null) {
            return false;
        }
        if (userId.isBlank() || !userId.matches("^[a-zA-Z][a-zA-Z0-9]*$") || userId.length() > 20) {
            return false;
        }
        return true;
    }

    private boolean validateUsername(String username) {
        if (username == null){
            return false;
        }
        if (username.isBlank()|| username.contains(" ") || username.length() > 100) {
            return false;
        }
        return true;
    }
}
