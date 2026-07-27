package dev.jpa.tags;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagPopularDTO {
    private Long tagId;
    private String name;
    private Long count;
}
