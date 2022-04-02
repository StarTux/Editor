package com.cavetale.editor.reflect;

/**
 * An exception thrown when an error occurs while the user clicks in
 * the editor menu.  The message is intended to be displayed to them.
 *
 * However, the error is not supposed to print to console or cause any
 * other disruption.
 */
public final class MenuException extends RuntimeException {
    public MenuException(final String message) {
        super(message);
    }
}
