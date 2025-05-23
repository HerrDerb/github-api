package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An abstract data object builder/updater.
 *
 * This class can be use to make a Builder that supports both batch and single property changes.
 * <p>
 * Batching looks like this:
 * </p>
 *
 * <pre>
 * update().someName(value).otherName(value).done()
 * </pre>
 * <p>
 * Single changes look like this:
 * </p>
 *
 * <pre>
 * set().someName(value);
 * set().otherName(value);
 * </pre>
 * <p>
 * If {@code S} is the same as {@code R}, {@link #with(String, Object)} will commit changes after the first value change
 * and return a {@code R} from {@link #done()}.
 * </p>
 * <p>
 * If {@code S} is not the same as {@code R}, {@link #with(String, Object)} will batch together multiple changes and let
 * the user call {@link #done()} when they are ready.
 *
 * @author Liam Newman
 * @param <R>
 *            Final return type built by this builder returned when {@link #done()}} is called.
 * @param <S>
 *            Intermediate return type for this builder returned by calls to {@link #with(String, Object)}. If {@code S}
 *            the same as {@code R}, this builder will commit changes after each call to {@link #with(String, Object)}.
 */
abstract class AbstractBuilder<R, S> extends GitHubInteractiveObject implements GitHubRequestBuilderDone<R> {

    @CheckForNull
    private final R baseInstance;

    private final boolean commitChangesImmediately;

    @Nonnull
    private final Class<R> returnType;

    /** The requester. */
    @Nonnull
    protected final Requester requester;

    // TODO: Not sure how update-in-place behavior should be controlled
    // However, it certainly can be controlled dynamically down to the instance level or inherited for all children of
    // some connection.

    /** The update in place. */
    protected boolean updateInPlace;

    /**
     * Creates a builder.
     *
     * @param finalReturnType
     *            the final return type for built by this builder returned when {@link #done()}} is called.
     * @param intermediateReturnType
     *            the intermediate return type of type {@code S} returned by calls to {@link #with(String, Object)}.
     *            Must either be equal to {@code builtReturnType} or this instance must be castable to this class. If
     *            not, the constructor will throw {@link IllegalArgumentException}.
     * @param root
     *            the GitHub instance to connect to.
     * @param baseInstance
     *            optional instance on which to base this builder.
     */
    @SuppressFBWarnings(value = { "CT_CONSTRUCTOR_THROW" }, justification = "argument validation, internal class")
    protected AbstractBuilder(@Nonnull Class<R> finalReturnType,
            @Nonnull Class<S> intermediateReturnType,
            @Nonnull GitHub root,
            @CheckForNull R baseInstance) {
        super(root);
        this.requester = root.createRequest();
        this.returnType = finalReturnType;
        this.commitChangesImmediately = returnType.equals(intermediateReturnType);
        if (!commitChangesImmediately && !intermediateReturnType.isInstance(this)) {
            throw new IllegalArgumentException(
                    "Argument \"intermediateReturnType\": This instance must be castable to intermediateReturnType or finalReturnType must be equal to intermediateReturnType.");
        }

        this.baseInstance = baseInstance;
        this.updateInPlace = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @BetaApi
    public R done() throws IOException {
        R result;
        if (updateInPlace && baseInstance != null) {
            result = requester.fetchInto(baseInstance);
        } else {
            result = requester.fetch(returnType);
        }
        return result;
    }

    /**
     * Chooses whether to return a continuing builder or an updated data record
     *
     * If {@code S} is the same as {@code R}, this method will commit changes after the first value change and return a
     * {@code R} from {@link #done()}.
     *
     * If {@code S} is not the same as {@code R}, this method will return an {@code S} and letting the caller batch
     * together multiple changes and call {@link #done()} when they are ready.
     *
     * @return either a continuing builder or an updated data record
     * @throws IOException
     *             if an I/O error occurs
     */
    @Nonnull
    @BetaApi
    protected S continueOrDone() throws IOException {
        // This little bit of roughness in this base class means all inheriting builders get to create Updater and
        // Setter classes from almost identical code. Creator can often be implemented with significant code reuse as
        // well.
        if (commitChangesImmediately) {
            // These casts look strange and risky, but they they're actually guaranteed safe due to the return path
            // being based on the previous comparison of class instances passed to the constructor.
            return (S) done();
        } else {
            return (S) this;
        }
    }

    /**
     * Applies a value to a name for this builder.
     *
     * If {@code S} is the same as {@code R}, this method will commit changes after the first value change and return a
     * {@code R} from {@link #done()}.
     *
     * If {@code S} is not the same as {@code R}, this method will return an {@code S} and letting the caller batch
     * together multiple changes and call {@link #done()} when they are ready.
     *
     * @param name
     *            the name of the field
     * @param value
     *            the value of the field
     * @return either a continuing builder or an updated data record
     * @throws IOException
     *             if an I/O error occurs
     */
    @Nonnull
    @BetaApi
    protected S with(@Nonnull String name, Object value) throws IOException {
        requester.with(name, value);
        return continueOrDone();
    }
}
