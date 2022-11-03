package tools.jackson.module.kotlin.test.github;

import tools.jackson.annotation.JsonCreator;

public class CloneableJavaObj implements Cloneable {
    public final String id;

    @JsonCreator
    public CloneableJavaObj(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
