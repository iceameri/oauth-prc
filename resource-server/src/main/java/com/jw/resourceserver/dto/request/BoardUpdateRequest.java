package com.jw.resourceserver.dto.request;

public record BoardUpdateRequest(
        String title,
        String content,
        Boolean isPinned,
        Boolean isSecret,
        String attachmentUrl,
        String attachmentName
) {
    public BoardUpdateRequest {
        if (title != null && title.length() > 200) {
            throw new IllegalArgumentException("제목은 200자를 초과할 수 없습니다.");
        }
    }

    public boolean hasTitle() {
        return title != null;
    }

    public boolean hasContent() {
        return content != null;
    }

    public boolean hasPinnedStatus() {
        return isPinned != null;
    }

    public boolean hasSecretStatus() {
        return isSecret != null;
    }

    public boolean hasAttachmentUrl() {
        return attachmentUrl != null;
    }

    public boolean hasAttachmentName() {
        return attachmentName != null;
    }
}
