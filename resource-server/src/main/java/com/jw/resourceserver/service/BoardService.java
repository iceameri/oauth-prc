package com.jw.resourceserver.service;

import com.jw.resourceserver.dto.request.BoardCreateRequest;
import com.jw.resourceserver.dto.request.BoardUpdateRequest;
import com.jw.resourceserver.dto.response.BoardResponse;
import com.jw.resourceserver.entity.resource.BoardType;
import com.jw.resourceserver.entity.resource.Boards;
import com.jw.resourceserver.entity.resource.Comments;
import com.jw.resourceserver.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;

    public BoardService(final BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional
    public BoardResponse createBoard(final BoardCreateRequest request) {
        Boards board = this.createBoardEntity(request);
        return BoardResponse.from(this.boardRepository.save(board));
    }

    public BoardResponse getBoardById(final Long id) {
        Boards board = this.findBoardById(id);
        board.increaseViewCount();

        return BoardResponse.from(this.boardRepository.save(board));
    }

    public List<BoardResponse> getBoardsByType(final BoardType type) {
        List<Boards> boards = this.boardRepository.findByBoardTypeOrderByIsPinnedDescCreatedByDesc(type);
        return boards.stream()
                .map(BoardResponse::from)
                .toList();
    }

    @Transactional
    public BoardResponse updateBoard(final Long id, final BoardUpdateRequest request) {
        Boards board = this.findBoardById(id);
        this.updateBoardFields(board, request);

        return Optional.of(this.boardRepository.save(board))
                .map(BoardResponse::from)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void deleteBoard(final Long id) {
        Boards board = this.findBoardById(id);
        board.softDelete();
        
        this.boardRepository.save(board);
    }

    @Transactional
    public BoardResponse pinBoard(final Long id) {
        Boards board = this.findBoardById(id);
        board.pin();
        return BoardResponse.from(this.boardRepository.save(board));
    }

    @Transactional
    public BoardResponse unpinBoard(final Long id) {
        Boards board = this.findBoardById(id);
        board.unpin();
        return BoardResponse.from(this.boardRepository.save(board));
    }

    public List<BoardResponse> getAllBoards() {
        List<Boards> boards = this.boardRepository.findAllByOrderByIsPinnedDescCreatedByDesc();
        return boards.stream()
                .map(BoardResponse::from)
                .toList();
    }

    public Boards findBoardById(final Long id) {
        return this.boardRepository.findBoardWithComments(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + id));
    }

    private Boards createBoardEntity(final BoardCreateRequest request) {
        return Boards.builder()
                .boardType(request.boardType())
                .title(request.title())
                .content(request.content())
                .author(request.author())
                .isPinned(request.isPinned())
                .isSecret(request.isSecret())
                .attachmentUrl(request.attachmentUrl())
                .attachmentName(request.attachmentName())
                .build();
    }

    private void updateBoardFields(final Boards board, final BoardUpdateRequest request) {
        if (request.hasTitle()) {
            board.updateTitle(request.title());
        }
        if (request.hasContent()) {
            board.updateContent(request.content());
        }
        if (request.hasPinnedStatus()) {
            if (request.isPinned()) {
                board.pin();
            } else {
                board.unpin();
            }
        }
        if (request.hasSecretStatus()) {
            board.updateSecretStatus(request.isSecret());
        }
        if (request.hasAttachmentUrl()) {
            board.updateAttachmentUrl(request.attachmentUrl());
        }
        if (request.hasAttachmentName()) {
            board.updateAttachmentName(request.attachmentName());
        }
    }
}
