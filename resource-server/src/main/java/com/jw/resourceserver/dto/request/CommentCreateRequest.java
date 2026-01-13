package com.jw.resourceserver.dto.request;

public record CommentCreateRequest(
        String content,
        Long parentId,
        Boolean isSecret
) {
    public CommentCreateRequest {
        this.validateContent(content);

        isSecret = isSecret != null ? isSecret : false;
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("댓글은 1000자를 초과할 수 없습니다.");
        }
    }

    public boolean isReply() {
        return parentId != null;
    }
}
