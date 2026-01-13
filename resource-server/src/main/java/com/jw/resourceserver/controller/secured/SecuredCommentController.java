package com.jw.resourceserver.controller.secured;

import com.jw.resourceserver.dto.BatchDeleteResult;
import com.jw.resourceserver.dto.request.CommentCreateRequest;
import com.jw.resourceserver.dto.response.CommentResponse;
import com.jw.resourceserver.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(SecuredCommentController.SECURED_API_PREFIX)
public class SecuredCommentController extends BaseSecuredController {
    public static final String SECURED_API_PREFIX = BaseSecuredController.API_PREFIX + "/boards/{boardId}/comments";

    private final CommentService commentService;

    SecuredCommentController(final CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable final Long boardId,
                                                         @RequestBody final CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.commentService.createComment(boardId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable final Long boardId) {
        return ResponseEntity.ok(this.commentService.getCommentsByBoard(boardId));
    }

    @DeleteMapping
    public ResponseEntity<Void> batchDeleteCommentByBoardId(@PathVariable final Long boardId) {
        this.commentService.batchDeleteCommentByBoardId(boardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable final Long boardId,
                                                      @PathVariable final Long commentId) {
        return ResponseEntity.ok(this.commentService.getCommentById(commentId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable final Long boardId,
                                              @PathVariable final Long commentId) {
        this.commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentIds}")
    public ResponseEntity<Object> batchDeleteComment(@PathVariable final Long boardId,
                                                     @PathVariable final List<Long> commentIds) {
        /*Replaced*/
        /*int deleted = this.commentService.batchDeleteComment(commentIds);

        if (deleted == 0) {
            return ResponseEntity.badRequest().build();
        } else if (commentIds.size() != deleted) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "일부 댓글만 삭제되었습니다.");
            response.put("requested", commentIds.size());
            response.put("deleted", deleted);
            response.put("failed", commentIds.size() - deleted);

            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }
        return ResponseEntity.noContent().build();*/
        BatchDeleteResult result = this.commentService.batchDeleteComment(commentIds);

        if (result.isCompleteFailure()) {
            return ResponseEntity.badRequest().body(result);
        } else if (result.isPartialSuccess()) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }
}
