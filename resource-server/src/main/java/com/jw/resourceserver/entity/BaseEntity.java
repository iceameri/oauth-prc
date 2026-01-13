package com.jw.resourceserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = DBConstants.Columns.CreatedAt, nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = DBConstants.Columns.CreatedBy, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(name = DBConstants.Columns.UpdatedAt, nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = DBConstants.Columns.UpdatedBy, length = 100)
    private String updatedBy;

    @Column(name = DBConstants.Columns.IsDeleted, nullable = false)
    private Boolean isDeleted = false;

    @Column(name = DBConstants.Columns.DeletedAt)
    private LocalDateTime deletedAt;

    // Soft Delete 메서드
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
