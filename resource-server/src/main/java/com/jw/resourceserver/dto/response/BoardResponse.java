package com.jw.resourceserver.dto.response;

import com.jw.resourceserver.entity.resource.BoardType;
import com.jw.resourceserver.entity.resource.Boards;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public record BoardResponse(
        Long id,
        BoardType boardType,
        String title,
        String content,
        String author,
        Long viewCount,
        Boolean isPinned,
        Boolean isSecret,
        String attachmentUrl,
        String attachmentName,
        int commentCount,
        int replyCount,
        LocalDateTime created,
        String createdBy,
        LocalDateTime updated,
        String updatedBy,
        Boolean isDeleted,
        List<CommentResponse> commentResponses
) {
    @Builder
    public BoardResponse {
        // 필요시 유효성 검증 로직
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("ID는 양수여야 합니다.");
        }
        if (viewCount != null && viewCount < 0) {
            throw new IllegalArgumentException("조회수는 음수일 수 없습니다.");
        }
        if (commentCount < 0) {
            throw new IllegalArgumentException("댓글 수는 음수일 수 없습니다.");
        }
        if (replyCount < 0) {
            throw new IllegalArgumentException("답글 수는 음수일 수 없습니다.");
        }
    }

    public static BoardResponse from(final Boards board) {
        return BoardResponse.builder()
                .id(board.getId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getAuthor())
                .viewCount(board.getViewCount())
                .isPinned(board.getIsPinned())
                .isSecret(board.getIsSecret())
                .attachmentUrl(board.getAttachmentUrl())
                .attachmentName(board.getAttachmentName())
                .commentCount(board.getCommentCount())
                .replyCount(board.getReplyCount())
                .created(board.getCreatedAt())
                .createdBy(board.getCreatedBy())
                .updated(board.getUpdatedAt())
                .updatedBy(board.getUpdatedBy())
                .updated(board.getUpdatedAt())
                .isDeleted(board.getIsDeleted())
                .commentResponses(board.getComments() != null ? board.getComments().stream().map(CommentResponse::from).toList(): null)
                .build();
    }

    // 비즈니스 로직 메서드들
    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.trim().isEmpty();
    }

    public boolean isPopularPost() {
        return viewCount != null && viewCount >= 100;
    }

    public boolean hasComments() {
        return commentCount > 0;
    }

    public int getTotalInteractionCount() {
        return commentCount + replyCount;
    }

    // 제목 요약
    public String getTitleSummary(final int maxLength) {
        if (title == null) return "";
        return title.length() > maxLength ?
                title.substring(0, maxLength) + "..." : title;
    }

    // 내용 미리보기
    public String getContentPreview(final int maxLength) {
        if (content == null) return "";
        String plainText = content.replaceAll("<[^>]*>", ""); // HTML 태그 제거
        return plainText.length() > maxLength ?
                plainText.substring(0, maxLength) + "..." : plainText;
    }
}


