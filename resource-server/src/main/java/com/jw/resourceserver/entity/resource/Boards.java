package com.jw.resourceserver.entity.resource;

import com.jw.resourceserver.entity.BaseEntity;
import com.jw.resourceserver.entity.DBConstants;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = DBConstants.Tables.Boards,
        indexes = {
        @Index(name = DBConstants.IndexNames.BoardsSearch, columnList = DBConstants.IndexColumns.BoardsSearchColumns),
        @Index(name = DBConstants.IndexNames.BoardsBoardType, columnList = DBConstants.Columns.BoardType),
})
@Getter
@NoArgsConstructor
public class Boards extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = DBConstants.Columns.Id, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = DBConstants.Columns.BoardType, nullable = false, length = 20)
    private BoardType boardType;

    @Column(name = DBConstants.Columns.Title, nullable = false, columnDefinition = DBConstants.ColumnDefinitions.NVARCHAR_200)
    private String title;

    @Column(name = DBConstants.Columns.Content, nullable = false, columnDefinition = DBConstants.ColumnDefinitions.NVARCHAR_4000)
    private String content;

    @Column(name = DBConstants.Columns.Author, nullable = false, columnDefinition = DBConstants.ColumnDefinitions.NVARCHAR_50)
    private String author;

    @Column(name = DBConstants.Columns.ViewCount, nullable = false)
    private Long viewCount = 0L;

    @Column(name = DBConstants.Columns.IsPinned, nullable = false)
    private Boolean isPinned = false;

    @Column(name = DBConstants.Columns.IsSecret, nullable = false)
    private Boolean isSecret = false;

    @OneToMany(mappedBy = DBConstants.Tables.Boards, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Comments> comments = new ArrayList<>();

    @Column(name = DBConstants.Columns.AttachmentUrl, length = 500)
    private String attachmentUrl;

    @Column(name = DBConstants.Columns.AttachmentName, columnDefinition = DBConstants.ColumnDefinitions.NVARCHAR_200)
    private String attachmentName;

    @Builder
    public Boards(
            final Long id,
            final BoardType boardType,
            final String title,
            final String content,
            final String author,
            final Long viewCount,
            final Boolean isPinned,
            final Boolean isSecret,
            final String attachmentUrl,
            final String attachmentName
    ) {
        this.id = id;
        this.boardType = boardType;
        this.title = title;
        this.content = content;
        this.author = author;
        this.viewCount = viewCount == null ? 0 : viewCount;
        this.isPinned = isPinned;
        this.isSecret = isSecret;
        this.attachmentUrl = attachmentUrl;
        this.attachmentName = attachmentName;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void pin() {
        this.isPinned = true;
    }

    public void unpin() {
        this.isPinned = false;
    }

    public int getCommentCount() {
        return comments.size();
    }

    public int getReplyCount() {
        return (int) comments.stream()
                .mapToLong(comments -> comments.getReplies().size())
                .sum();
    }

    public void updateTitle(final String title) {
        if (title != null && !title.trim().isEmpty() && title.length() <= 200) {
            this.title = title;
        }
    }

    public void updateContent(final String content) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content;
        }
    }

    public void updateSecretStatus(final Boolean isSecret) {
        this.isSecret = isSecret != null ? isSecret : false;
    }

    public void updateAttachmentUrl(final String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public void updateAttachmentName(final String attachmentName) {
        this.attachmentName = attachmentName;
    }
}
