package com.jw.resourceserver.entity.resource;

import com.jw.resourceserver.entity.DBConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jdk.jfr.Enabled;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;


@Enabled
@Table(name = DBConstants.Tables.BoardViewLogs)
@Getter
@NoArgsConstructor
public class BoardViewLogs {
    @Id
    @Column(name = DBConstants.Columns.BoardId, nullable = false)
    private Long boardId;

    @Id
    @Column(name = DBConstants.Columns.UserId, nullable = false)
    private String userId;

    @CreatedDate
    @Column(name = DBConstants.Columns.ViewedAt, nullable = false, updatable = false)
    private LocalDateTime viewedAt;
}
