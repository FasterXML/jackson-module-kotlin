package com.fasterxml.jackson.module.kotlin.test.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Github165JavaTest
{
    @JsonProperty("name")
    public String showName;

    @JsonProperty("year")
    public String showYear;

    public boolean nameSetterCalled = false;
    public boolean yearSetterCalled = false;

    public Github165JavaTest(@JsonProperty("name") String name) {
        this.showName = name;
    }

    @JsonSetter("name")
    public void setName(String value) {
        nameSetterCalled = true;
        this.showName = value;
    }

    @JsonSetter("year")
    public void setYear(String value) {
        yearSetterCalled = true;
        this.showYear = value;
    }
}
