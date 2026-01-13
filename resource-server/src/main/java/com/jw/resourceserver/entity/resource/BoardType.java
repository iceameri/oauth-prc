package com.jw.resourceserver.entity.resource;

import lombok.Getter;

@Getter
public enum BoardType {
    GENERAL("일반"),
    NOTICE("공지"),
    FAQ("FAQ"),
    EVENT("이벤트");

    private final String description;

    BoardType(final String description) {
        this.description = description;
    }
}
