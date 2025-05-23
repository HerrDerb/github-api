/*
 * The MIT License
 *
 * Copyright (c) 2011, Eric Maupin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Action to perform on {@link GHPullRequestReview}.
 */
public enum GHPullRequestReviewEvent {

    /** The approve. */
    APPROVE,
    /** The comment. */
    COMMENT,
    /** The pending. */
    PENDING,
    /** The request changes. */
    REQUEST_CHANGES;

    /**
     * Action.
     *
     * @return the string
     */
    String action() {
        return this == PENDING ? null : name();
    }

    /**
     * When a {@link GHPullRequestReview} is submitted with this event, it should transition to this state.
     *
     * @return the GH pull request review state
     */
    GHPullRequestReviewState toState() {
        switch (this) {
            case PENDING :
                return GHPullRequestReviewState.PENDING;
            case APPROVE :
                return GHPullRequestReviewState.APPROVED;
            case REQUEST_CHANGES :
                return GHPullRequestReviewState.CHANGES_REQUESTED;
            case COMMENT :
                return GHPullRequestReviewState.COMMENTED;
        }
        throw new IllegalStateException();
    }
}
