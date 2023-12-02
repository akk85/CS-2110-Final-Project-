package cms.util;

/**
 * An exception that is fast to throw but doesn't have very good debugging
 * support unless its withStackTrace() method is used explicitly.
 */
public abstract class FastException extends Exception {
    /** Initialize a FastException */
    protected FastException() {}

    /** Create a FastException */
    protected abstract FastException create();

    /**
     * Effect: fills in the stack trace of this exception.
     * Returns: this
     */
    public FastException withStackTrace() {
        FastException e = create();
        return (FastException) e.customFillInStackTrace();
    }

    /**
     * There is no meaningful stack trace for this exception because it is supposed to always be handled.
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    /**
     * Effect: Fill in the stack trace like a normal (expensive) exception.
     * Returns: a Throwable with a filled-in stack trace.
     */
    public synchronized Throwable customFillInStackTrace() {
        return super.fillInStackTrace();
    }
}
