package com.jw.resourceserver.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BatchDeleteResult {
    private int requested;
    private int deleted;
    private List<Long> successIds;
    private List<Long> failedIds;
    private String message;

    BatchDeleteResult() {}

    @Builder
    public BatchDeleteResult(int requested, int deleted, List<Long> successIds, List<Long> failedIds) {
        this.requested = requested;
        this.deleted = deleted;
        this.successIds = successIds;
        this.failedIds = failedIds;
        this.message = createMessage();
    }

    private String createMessage() {
        if (deleted == 0) {
            return "모든 댓글 삭제에 실패했습니다.";
        } else if (requested == deleted) {
            return "모든 댓글이 성공적으로 삭제되었습니다.";
        } else {
            return String.format("총 %d개 중 %d개 댓글이 삭제되었습니다.", requested, deleted);
        }
    }

    public int getFailed() { return requested - deleted; }
    public boolean isPartialSuccess() { return deleted > 0 && deleted < requested; }
    public boolean isCompleteSuccess() { return deleted == requested; }
    public boolean isCompleteFailure() { return deleted == 0; }
}

