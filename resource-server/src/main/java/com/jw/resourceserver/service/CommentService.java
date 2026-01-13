package com.jw.resourceserver.service;

import com.jw.resourceserver.dto.BatchDeleteResult;
import com.jw.resourceserver.dto.request.CommentCreateRequest;
import com.jw.resourceserver.dto.response.CommentResponse;
import com.jw.resourceserver.entity.resource.Boards;
import com.jw.resourceserver.entity.resource.Comments;
import com.jw.resourceserver.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardService boardService;

    CommentService(final CommentRepository commentRepository,
                   final BoardService boardService) {
        this.commentRepository = commentRepository;
        this.boardService = boardService;
    }

    @Transactional
    public CommentResponse createComment(final Long boardId, final CommentCreateRequest request) {
        Boards board = this.findBoardById(boardId);
        Comments parent = null;

        if (request.parentId() != null && request.parentId() > 0) {
            parent = this.findParentCommentIfExists(request);
        }

        Comments comment = this.createCommentEntity(board, request, parent);

        return CommentResponse.from(this.commentRepository.save(comment));
    }

    public List<CommentResponse> getCommentsByBoard(final Long boardId) {
        List<Comments> comments = this.commentRepository.findByBoardsIdAndParentIsNullOrderByCreatedByAsc(boardId);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse getCommentById(final Long id) {
        return CommentResponse.from(this.findCommentById(id));
    }

    @Transactional
    public void deleteComment(final Long id) {
        Comments comment = this.findCommentById(id);
        comment.softDelete();
        this.commentRepository.save(comment);
    }

    @Transactional
    public BatchDeleteResult batchDeleteComment(final List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return BatchDeleteResult.builder()
                    .requested(0)
                    .deleted(0)
                    .successIds(List.of())
                    .failedIds(List.of())
                    .build();
        }

        // 삭제 전 존재하는 댓글 ID들 조회
        List<Long> existingIds = this.commentRepository.findExistingIds(ids);

        // 실제 삭제 실행
        int deletedCount = this.commentRepository.softDeleteByIds(ids);

        // 실패한 ID들 계산
        List<Long> failedIds = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        return BatchDeleteResult.builder()
                .requested(ids.size())
                .deleted(deletedCount)
                .successIds(existingIds)
                .failedIds(failedIds)
                .build();
    }

    @Transactional
    public void batchDeleteCommentByBoardId(final Long boardsId) {
        this.commentRepository.softDeleteByBoardId(boardsId);
    }

    private Boards findBoardById(final Long boardId) {
        return this.boardService.findBoardById(boardId);
    }

    private Comments findCommentById(final Long commentId) {
        return this.commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("댓글을 찾을 수 없습니다: %s", commentId)));
    }

    private List<Comments> findCommentByBoardsId(final Long boardId) {
        return this.commentRepository.findByBoardsId(boardId);
    }

    private Comments findParentCommentIfExists(final CommentCreateRequest request) {
        if (request.isReply()) {
            return this.findCommentById(request.parentId());
        }
        return null;
    }

    private Comments createCommentEntity(final Boards board,
                                         final CommentCreateRequest request,
                                         final Comments parent) {
        return Comments.builder()
                .boards(board)
                .content(request.content())
                .parent(parent)
                .isSecret(request.isSecret())
                .build();
    }
}
