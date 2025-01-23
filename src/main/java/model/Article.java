package model;

import db.ArticleStore;

import java.util.ArrayList;
import java.util.List;

public class Article {
    private static int id = ArticleStore.findAll().size();
    private final int articleId;
    private final String content;
    private final User user;
    private final List<Comment> comments;
    private final String image;

    public Article(String content, User user, String image) {
        this.articleId = id;
        id += 1;
        this.content = content;
        this.user = user;
        this.comments = new ArrayList<>();
        this.image = image;
    }

    public Article(int id, String content, User user, List<Comment> comments, String image) {
        this.articleId = id;
        this.content = content;
        this.user = user;
        this.comments = comments;
        this.image = image;
    }

    public int getArticleId() {
        return articleId;
    }

    public String getContent() {
        return content;
    }

    public User getUser() {
        return user;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public String getImage() {
        return image;
    }
}
