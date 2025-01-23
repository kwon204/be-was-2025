package model;

import db.CommentStore;

public class Comment {
    private static int id = CommentStore.findAll().size();
    private final int commentId;
    private final String content;
    private final User user;
    private Article article;

    public Comment(String content, User user, Article article) {
        this.commentId = id;
        id += 1;
        this.content = content;
        this.user = user;
        this.article = article;
    }

    public Comment(int id, String content, User user) {
        this.commentId = id;
        this.content = content;
        this.user = user;
    }

    public int getCommentId() {
        return commentId;
    }

    public String getContent() {
        return content;
    }

    public User getUser() {
        return user;
    }

    public Article getArticle() {
        return article;
    }
}
