package tools.jackson.module.kotlin.test.github;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

public class GitHub1064ExplicitContentNulls {
    @JsonSetter(contentNulls = Nulls.FAIL)
    public List<Map<String, Object>> values;
}
