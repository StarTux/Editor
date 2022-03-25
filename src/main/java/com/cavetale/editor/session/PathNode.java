package com.cavetale.editor.session;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public final class PathNode {
    protected String name;
    protected int page;
}
