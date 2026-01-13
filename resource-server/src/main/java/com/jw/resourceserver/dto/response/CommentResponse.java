package com.jw.resourceserver.dto.response;

import com.jw.resourceserver.entity.resource.Comments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentResponse(
        Long id,
        String content,
        Boolean isSecret,
        Long parentId,
        int depth,
        int replyCount,
        LocalDateTime created,
        String createdBy,
        LocalDateTime updated,
        String updatedBy,
        Boolean isDeleted,
        List<CommentResponse> replies
) {

    public CommentResponse {
        replies = replies != null ? new ArrayList<>(replies) : new ArrayList<>();
    }

    public static CommentResponse from(final Comments comment) {
        List<CommentResponse> replyResponses = comment.getReplies().stream()
                .map(CommentResponse::from)
                .toList();

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getIsSecret(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getParent() != null ? 1 : 0,
                comment.getReplyCount(),
                comment.getCreatedAt(),
                comment.getCreatedBy(),
                comment.getUpdatedAt(),
                comment.getUpdatedBy(),
                comment.getIsDeleted(),
                replyResponses
        );
    }

    // 편의 메서드들 (필요시 추가)
    public boolean hasReplies() {
        return replies != null && !replies.isEmpty();
    }

    public boolean isReply() {
        return parentId != null;
    }

    public boolean isRootComment() {
        return parentId == null;
    }

    public List<CommentResponse> replies() {
        return new ArrayList<>(replies);
    }
}
