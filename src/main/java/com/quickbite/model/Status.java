package com.quickbite.model;

/** The life cycle of an order. Rule BR-05 lives here, in one place. */
public enum Status {
    PENDING, PREPARING, READY, COMPLETED, CANCELLED;

    /** The next step in the normal flow, or null if there is none. */
    public Status next() {
        return switch (this) {
            case PENDING -> PREPARING;
            case PREPARING -> READY;
            case READY -> COMPLETED;
            default -> null;
        };
    }

    /** Allowed moves: one step forward, or PENDING to CANCELLED. */
    public boolean canTransitionTo(Status target) {
        if (target == CANCELLED) {
            return this == PENDING;
        }
        return target == next();
    }
}
