package com.jw.resourceserver.repository;

import com.jw.resourceserver.entity.resource.BoardType;
import com.jw.resourceserver.entity.resource.Boards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Boards, Long> {

    List<Boards> findByBoardTypeOrderByIsPinnedDescCreatedByDesc(final BoardType boardType);

    List<Boards> findAllByOrderByIsPinnedDescCreatedByDesc();

    @Query("""
            SELECT          A
            FROM            Boards AS A
            LEFT JOIN FETCH A.comments AS B
            WHERE           A.id = :id
            """)
    Optional<Boards> findBoardWithComments(final Long id);
}
