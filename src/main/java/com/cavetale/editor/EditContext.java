package com.cavetale.editor;

public interface EditContext {
    default void save() {
        throw new IllegalStateException("Saving not implemented");
    }
}
