package com.fasterxml.jackson.module.kotlin.test.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class GitHub922RequiredCollectionsDtoJava {
    private final List<String> list;
    private final Map<String, String> map;

    @JsonCreator
    public GitHub922RequiredCollectionsDtoJava(
            @JsonProperty(value = "list", required = true) List<String> list,
            @JsonProperty(value = "map", required = true) Map<String, String> map
    ) {
        this.list = list;
        this.map = map;
    }

    public List<String> getList() {
        return list;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
