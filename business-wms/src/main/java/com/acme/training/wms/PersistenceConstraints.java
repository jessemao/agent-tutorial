package com.acme.training.wms;

import org.hibernate.exception.ConstraintViolationException;

public final class PersistenceConstraints {

    private PersistenceConstraints() {
    }

    public static boolean hasName(Throwable exception, String constraintName) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException) {
                String actualName = ((ConstraintViolationException) cause).getConstraintName();
                if (actualName != null && actualName.replace("\"", "").matches(
                        "(?is)(?:\\w+\\.)?" + constraintName + "(?:_index_\\w+)?(?: on .*|$)")) {
                    return true;
                }
            }
        }
        return false;
    }
}
