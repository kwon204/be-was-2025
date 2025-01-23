package handler;

import db.ArticleStore;
import db.UserStore;
import http.HttpRequest;
import http.HttpResponse;
import http.constant.HttpStatus;
import model.Article;
import model.Session;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtils;
import util.RequestParser;
import util.SessionUtils;
import util.exception.UserNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class ArticleHandler implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(ArticleHandler.class);

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if (!SessionUtils.isLogin(httpRequest)) {
            httpResponse.redirect("/login");
            return;
        }

        String path = httpRequest.getPath().toLowerCase();

        File file = FileUtils.findFile(path);

        httpResponse.writeStatusLine(HttpStatus.OK);
        httpResponse.writeBody(file);
        httpResponse.send();
    }

    public void postArticle(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if (!SessionUtils.isLogin(httpRequest)) {
            httpResponse.redirect("/login");
            return;
        }
        Session session = SessionUtils.findSession(httpRequest);
        User user = UserStore.findUserById(session.userId())
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

        Map<String, String> data = RequestParser.parseRequestBody(httpRequest);

        String content = data.get("content");
        String imagePath = data.get("image");

        Article article = new Article(content, user, imagePath);
        logger.debug("Created Article= id:{}, content:{}, username:{}, image: {}", article.getArticleId(), article.getContent(),
                article.getUser().getName(), imagePath);
        ArticleStore.addArticle(article);

        httpResponse.redirect("/main");
    }
}
