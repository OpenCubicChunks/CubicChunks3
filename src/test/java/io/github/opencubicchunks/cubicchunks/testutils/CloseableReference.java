package io.github.opencubicchunks.cubicchunks.testutils;

/**
 * A record wrapping a value and storing any {@link AutoCloseable}s that need to be closed once the value is no longer in use.
 * <br>
 * This is necessary because Java does not have multi-return; if a function returns the value, it also needs a way to return the associated {@link AutoCloseable}s.
 * <br><br>
 * This should be used with try with resources as follows:
 * <pre>
 * {@code
 * // createFooReference() returns CloseableReference<Foo>
 * try(var fooReference = createFooReference()) {
 *     var foo = fooReference.value();
 *     // code using foo
 * }
 * }
 * </pre>
 * @param value The value that is referenced
 * @param toClose Instances of {@link AutoCloseable} that must be closed when value is no longer in use
 * @param <T> The type of the referenced value
 *
 * @author Builderb0y
 */
public record CloseableReference<T>(T value, AutoCloseable... toClose) implements AutoCloseable {
    @Override public void close() throws Exception {
        // Close all closeables catching any exceptions, then re-throw any exceptions that occurred
        Exception e = null;
        for (AutoCloseable closeable : this.toClose) {
            try {
                closeable.close();
            }
            catch (Throwable throwable) {
                if (e == null) {
                    e = new Exception();
                }
                e.addSuppressed(throwable);
            }
        }
        if (e != null) {
            throw e;
        }
    }
}
