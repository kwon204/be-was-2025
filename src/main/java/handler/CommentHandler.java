package handler;

import db.ArticleStore;
import db.CommentStore;
import db.UserStore;
import http.HttpRequest;
import http.HttpResponse;
import http.constant.HttpStatus;
import model.Article;
import model.Comment;
import model.Session;
import model.User;
import util.*;
import util.exception.ArticleNotFoundException;
import util.exception.UserNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class CommentHandler implements Handler {
    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if (!SessionUtils.isLogin(httpRequest)) {
            httpResponse.redirect("/");
            return;
        }
        String path = httpRequest.getPath().toLowerCase();

        File file = FileUtils.findFile(path);
        String content = FileUtils.convertToString(file);
        String extension = FileUtils.getExtension(file);
        MimeType mimeType = MimeType.valueOf(extension.toUpperCase());

        String articleId = httpRequest.getQueries().get("article");

        content = DynamicHtmlEditor.edit(content, "articleId", articleId);

        byte[] body = content.getBytes();
        httpResponse.writeStatusLine(HttpStatus.OK);
        httpResponse.writeBody(body, mimeType.getMimeType());
        httpResponse.send();
    }

    public void postComment(HttpRequest httpRequest, HttpResponse httpResponse) throws UnsupportedEncodingException {
        if (!SessionUtils.isLogin(httpRequest)) {
            httpResponse.redirect("/");
            return;
        }
        Session session = SessionUtils.findSession(httpRequest);
        User user = UserStore.findUserById(session.userId())
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));
        Map<String, String> data = RequestParser.parseBody(new String(httpRequest.getBody()));

        String commentContent = data.get("content");
        String articleId = data.get("article");

        if (!validateComment(commentContent)) {
            httpResponse.redirect(String.format("/comment?article=%s", articleId));
        }

        Article article = ArticleStore.findArticleById(Integer.parseInt(articleId))
                .orElseThrow(() -> new ArticleNotFoundException("해당 게시글이 없습니다."));

        Comment comment = new Comment(commentContent, user, article);
        CommentStore.addComment(comment);

        httpResponse.redirect("/main");
    }

    private boolean validateComment(String content) {
        if (content == null) {
            return false;
        }
        if (content.length() > 1000) {
            return false;
        }

        return true;
    }
}
