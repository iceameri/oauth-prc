package com.jw.resourceserver.dto.request;

import com.jw.resourceserver.entity.resource.BoardType;


public record BoardCreateRequest(
        BoardType boardType,
        String title,
        String content,
        String author,
        Boolean isPinned,
        Boolean isSecret,
        String attachmentUrl,
        String attachmentName
) {
    public BoardCreateRequest {
        this.validateTitle(title);
        this.validateContent(content);
        this.validateAuthor(author);

        isPinned = isPinned != null ? isPinned : false;
        isSecret = isSecret != null ? isSecret : false;
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("제목은 200자를 초과할 수 없습니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
    }

    private void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }
        if (author.length() > 50) {
            throw new IllegalArgumentException("작성자는 50자를 초과할 수 없습니다.");
        }
    }
}
