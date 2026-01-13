package com.jw.resourceserver.repository;

import com.jw.resourceserver.entity.resource.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comments, Long> {

    List<Comments> findByBoardsIdAndParentIsNullOrderByCreatedByAsc(final Long boardId);

    List<Comments> findByBoardsId(Long boardsId);

    /*DB 친화적*/
    @Modifying
    @Query(value = """
            UPDATE comments
            SET    is_deleted = 1,
                   deletedAt = GETDATE()
            WHERE  board_id = :boardId
            """, nativeQuery = true)
    void softDeleteByBoardId(@Param("boardId") Long boardId);

    /*JPA 친화적*/
    @Modifying
    @Query("""
            UPDATE  Comments AS A
            SET     A.isDeleted = true,
                    A.deletedAt = CURRENT_TIMESTAMP
            WHERE   A.id IN (:ids)
            """)
    int softDeleteByIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.id FROM Comments c WHERE c.id IN (:ids) AND c.isDeleted = false")
    List<Long> findExistingIds(@Param("ids") List<Long> ids);
}
