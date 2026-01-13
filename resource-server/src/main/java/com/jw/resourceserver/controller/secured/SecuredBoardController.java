package com.jw.resourceserver.controller.secured;

import com.jw.resourceserver.dto.request.BoardCreateRequest;
import com.jw.resourceserver.dto.request.BoardUpdateRequest;
import com.jw.resourceserver.dto.response.BoardResponse;
import com.jw.resourceserver.entity.resource.BoardType;
import com.jw.resourceserver.service.BoardService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(SecuredBoardController.SECURED_API_PREFIX)
public class SecuredBoardController extends BaseSecuredController {

    public static final String SECURED_API_PREFIX = BaseSecuredController.API_PREFIX + "/boards";

    private final BoardService boardService;

    SecuredBoardController(final BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@RequestBody final BoardCreateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.boardService.createBoard(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable final Long id) {
        try {
            return ResponseEntity.ok(this.boardService.getBoardById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> getBoards(@RequestParam(required = false) final BoardType type) {
        List<BoardResponse> responses = type != null ?
                this.boardService.getBoardsByType(type) :
                this.boardService.getAllBoards();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> updateBoard(@PathVariable final Long id,
                                                     @RequestBody final BoardUpdateRequest request) {
        try {
            return ResponseEntity.ok(this.boardService.updateBoard(id, request));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable final Long id) {
        try {
            this.boardService.deleteBoard(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/pin")
    public ResponseEntity<BoardResponse> pinBoard(@PathVariable final Long id) {
        try {
            return ResponseEntity.ok(this.boardService.pinBoard(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/unpin")
    public ResponseEntity<BoardResponse> unpinBoard(@PathVariable final Long id) {
        try {
            return ResponseEntity.ok(this.boardService.unpinBoard(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
