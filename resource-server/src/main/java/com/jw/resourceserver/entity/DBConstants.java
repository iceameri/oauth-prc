package com.jw.resourceserver.entity;

/*
*    java에서는 camel-case DB는 snake-case
*/
public final class DBConstants {
    DBConstants() {
    }

    public static final class Tables {
        public static final String Boards = "boards";
        public static final String Comments = "comments";
        public static final String BoardViewLogs = "board_view_logs";
    }

    public static final class Columns {
        /*common*/
        public static final String CreatedAt = "created_at";
        public static final String UpdatedAt = "updated_at";
        public static final String CreatedBy = "created_by";
        public static final String UpdatedBy = "updated_by";
        public static final String IsDeleted = "is_deleted";
        public static final String DeletedAt = "deleted_at";

        public static final String Id = "id";
        public static final String Title = "title";
        public static final String Content = "content";
        public static final String BoardType = "board_type";
        public static final String Author = "author";
        public static final String ViewCount = "view_count";
        public static final String IsPinned = "is_pinned";
        public static final String IsSecret = "is_secret";
        public static final String AttachmentUrl = "attachment_url";
        public static final String AttachmentName = "attachment_name";
        public static final String ViewedAt = "viewed_at";

        /*FK*/
        public static final String BoardId = "board_id";
        public static final String ParentId = "parent_id";
        public static final String UserId = "user_id";
    }

    public static class ColumnDefinitions {
        public static final String BIGINT = "BIGINT";
        public static final String NVARCHAR_50 = "NVARCHAR(50)";
        public static final String NVARCHAR_200 = "NVARCHAR(200)";
        public static final String NVARCHAR_2000 = "NVARCHAR(2000)";
        public static final String NVARCHAR_4000 = "NVARCHAR(4000)";
    }


    public static final class IndexNames {
        public static final String BoardsBoardType = "idx_01_board_type";
        public static final String BoardsSearch = "idx_02_boards_search";
    }

    public static final class IndexColumns {

        public static final String BoardsSearchColumns =
                Columns.Title + " ASC , " +
                Columns.Content + " ASC , " +
                Columns.Author + " ASC , " +
                Columns.CreatedBy + " ASC ";
    }
}
