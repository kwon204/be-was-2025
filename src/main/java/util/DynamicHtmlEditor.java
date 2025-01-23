package util;

import model.Article;
import model.Comment;

import java.util.List;

public class DynamicHtmlEditor {
    private static final String DYNAMIC_PREFIX = "dynamic";
    private static final int DEFAULT_VISIBLE_COMMENT_SIZE = 3;

    public static String edit(String content, String field, String value) {
        String target = String.format("{{ %s:%s }}", DYNAMIC_PREFIX, field);
        StringBuilder sb = new StringBuilder(content);
        int index;
        int from = 0;

        while((index = sb.indexOf(target, from)) != -1) {
            sb.replace(index, index + target.length(), value);
            from = index;
        }

        return sb.toString();
    }

    public static String commentElement(Article article, boolean showAll, int page, String path) {
        if (article == null) {
            return "";
        }
        StringBuilder commentBuilder = new StringBuilder();

        List<Comment> comments = article.getComments();

        if (comments.isEmpty()) {
            commentBuilder.append("<p class=\"comment__item__article\"> 댓글이 없습니다. </p>");
            return commentBuilder.toString();
        }

        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);
            String element;
            if (i > DEFAULT_VISIBLE_COMMENT_SIZE - 1 && !showAll) {
                element = getHiddenCommentElement();
            } else {
                element = getCommentElement();
            }
            element = edit(element, "comment_user", comment.getUser().getName());
            element = edit(element, "comment_content", comment.getContent());
            element = edit(element, "comment_user_image", comment.getUser().getImagePath());
            commentBuilder.append(element);
        }

        if (comments.size() > DEFAULT_VISIBLE_COMMENT_SIZE && !showAll) {
            String element = getShowAllCommentsElement();
            element = edit(element, "showAll", "all");
            element = edit(element, "path", path);
            element = edit(element, "page", String.valueOf(page));
            commentBuilder.append(element);
        }
        return commentBuilder.toString();
    }

    public static String articleElement(Article article) {
        if (article == null) {
            return "등록된 게시글이 없습니다.";
        }
        String element = getArticleElement();
        element = edit(element, "author", article.getUser().getName());
        element = edit(element, "content", article.getContent());
        element = edit(element, "authorImage", article.getUser().getImagePath());
        element = edit(element, "articleImage", article.getImage());

        return element;
    }

    public static String navigationElement(Article article, int prevPage, int nextPage, String path) {
        if (article == null) {
            return "";
        }
        String content = getNavigationElement();

        content = edit(content, "prevPage", String.valueOf(prevPage));
        content = edit(content, "nextPage", String.valueOf(nextPage));
        content = edit(content, "articleId", String.valueOf(article.getArticleId()));
        content = edit(content, "path", path);

        return content;
    }


    private static String getArticleElement() {
        return
                """
                    <div class="post__account">
                      <img class="post__account__img" src="./img/bucket/{{ dynamic:authorImage }}" />
                          <p class="post__account__nickname">{{ dynamic:author }}</p>
                    </div>
                    <img class="post__img" src="./img/bucket/{{ dynamic:articleImage }}"/>
                    <div class="post__menu">
                      <ul class="post__menu__personal">
                        <li>
                          <button class="post__menu__btn">
                            <img src="./img/like.svg" />
                          </button>
                        </li>
                        <li>
                          <button class="post__menu__btn">
                            <img src="./img/sendLink.svg" />
                          </button>
                        </li>
                      </ul>
                      <button class="post__menu__btn">
                        <img src="./img/bookMark.svg" />
                      </button>
                    </div>
                    <p class="post__article">
                
                    <pre>{{ dynamic:content }}</pre>
                
                    </p>
                """;
    }

    private static String getCommentElement() {
        return """
        <li class="comment__item">
            <div class="comment__item__user">
              <img class="comment__item__user__img" src="./img/bucket/{{ dynamic:comment_user_image }}"/>
              <p class="comment__item__user__nickname">{{ dynamic:comment_user }}</p>
            </div>
            <p class="comment__item__article">
              <pre>{{ dynamic:comment_content }}</pre>
            </p>
        </li>
        """;
    }

    private static String getHiddenCommentElement() {
        return """
                  <li class="comment__item hidden">
                    <div class="comment__item__user">
                      <img class="comment__item__user__img" src="./img/bucket/{{ dynamic:comment_user_image }}"/>
                      <p class="comment__item__user__nickname">{{ dynamic:comment_user }}</p>
                    </div>
                    <p class="comment__item__article">{{ dynamic:comment_content }}</p>
                  </li>
                """;
    }

    private static String getShowAllCommentsElement() {
        return """
                <a id="show-all-btn" class="btn btn_ghost btn_size_m" href="{{ dynamic:path }}?page={{ dynamic:page }}&comment={{ dynamic:showAll }}">
                    모든 댓글 보기
                </a>
                """;
    }

    private static String getNavigationElement() {
        return
                """
                <ul class="nav__menu">
                <li class="nav__menu__item">
                    <a class="nav__menu__item__btn" href="{{ dynamic:path }}?page={{ dynamic:prevPage }}">
                      <img
                        class="nav__menu__item__img"
                        src="./img/ci_chevron-left.svg"
                      />
                      이전 글
                    </a>
                  </li>
                  <li class="nav__menu__item">
                    <a class="btn btn_ghost btn_size_m" href="/comment?article={{ dynamic:articleId }}">댓글 작성</a>
                  </li>
                  <li class="nav__menu__item">
                    <a class="nav__menu__item__btn" href="{{ dynamic:path }}?page={{ dynamic:nextPage }}">
                      다음 글
                      <img
                        class="nav__menu__item__img"
                        src="./img/ci_chevron-right.svg"
                      />
                    </a>
                  </li>
                </ul>
                """;
    }
}
