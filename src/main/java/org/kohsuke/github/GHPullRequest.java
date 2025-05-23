/*
 * The MIT License
 *
 * Copyright (c) 2010, Kohsuke Kawaguchi
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

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

// TODO: Auto-generated Javadoc
/**
 * A pull request.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getPullRequest(int) GHRepository#getPullRequest(int)
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class GHPullRequest extends GHIssue implements Refreshable {

    /**
     * The status of auto merging a {@linkplain GHPullRequest}.
     *
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    public static class AutoMerge {

        private String commitMessage;

        private String commitTitle;
        private GHUser enabledBy;
        private MergeMethod mergeMethod;
        /**
         * Create default AutoMerge instance
         */
        public AutoMerge() {
        }

        /**
         * the message of the commit, if e.g. {@linkplain MergeMethod#SQUASH} is used for the auto merge.
         *
         * @return the message of the commit
         */
        public String getCommitMessage() {
            return commitMessage;
        }

        /**
         * the title of the commit, if e.g. {@linkplain MergeMethod#SQUASH} is used for the auto merge.
         *
         * @return the title of the commit
         */
        public String getCommitTitle() {
            return commitTitle;
        }

        /**
         * The user who enabled the auto merge of the pull request.
         *
         * @return the {@linkplain GHUser}
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
        public GHUser getEnabledBy() {
            return enabledBy;
        }

        /**
         * The merge method of the auto merge.
         *
         * @return the {@linkplain MergeMethod}
         */
        public MergeMethod getMergeMethod() {
            return mergeMethod;
        }
    }

    /** The enum MergeMethod. */
    public enum MergeMethod {

        /** The merge. */
        MERGE,
        /** The rebase. */
        REBASE,
        /** The squash. */
        SQUASH
    }
    private static final String COMMENTS_ACTION = "/comments";

    private static final String REQUEST_REVIEWERS = "/requested_reviewers";
    private AutoMerge autoMerge;
    private GHCommitPointer base;
    private int changedFiles;

    private int deletions;
    private GHCommitPointer head;
    private String mergeCommitSha;

    private Boolean mergeable;
    private String mergeableState;
    private boolean merged, maintainerCanModify;
    private String mergedAt;
    // details that are only available when obtained from ID
    private GHUser mergedBy;
    private String patchUrl, diffUrl, issueUrl;
    private GHUser[] requestedReviewers;

    // pull request reviewers

    private GHTeam[] requestedTeams;
    private int reviewComments, additions, commits;

    /** The draft. */
    // making these package private to all for testing
    boolean draft;

    /**
     * Create default GHPullRequest instance
     */
    public GHPullRequest() {
    }

    /**
     * Can maintainer modify boolean.
     *
     * @return the boolean
     * @throws IOException
     *             the io exception
     */
    public boolean canMaintainerModify() throws IOException {
        populate();
        return maintainerCanModify;
    }

    /**
     * Create review gh pull request review builder.
     *
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder createReview() {
        return new GHPullRequestReviewBuilder(this);
    }

    /**
     * Create gh pull request review comment builder.
     *
     * @return the gh pull request review comment builder.
     */
    public GHPullRequestReviewCommentBuilder createReviewComment() {
        return new GHPullRequestReviewCommentBuilder(this);
    }

    /**
     * Create review comment gh pull request review comment.
     *
     * @param body
     *            the body
     * @param sha
     *            the sha
     * @param path
     *            the path
     * @param position
     *            the position
     * @return the gh pull request review comment
     * @throws IOException
     *             the io exception
     * @deprecated use {@link #createReviewComment()}
     */
    @Deprecated
    public GHPullRequestReviewComment createReviewComment(String body, String sha, String path, int position)
            throws IOException {
        return createReviewComment().body(body).commitId(sha).path(path).position(position).create();
    }

    /**
     * Request to enable auto merge for a pull request.
     *
     * @param authorEmail
     *            The email address to associate with this merge.
     * @param clientMutationId
     *            A unique identifier for the client performing the mutation.
     * @param commitBody
     *            Commit body to use for the commit when the PR is mergable; if omitted, a default message will be used.
     *            NOTE: when merging with a merge queue any input value for commit message is ignored.
     * @param commitHeadline
     *            Commit headline to use for the commit when the PR is mergable; if omitted, a default message will be
     *            used. NOTE: when merging with a merge queue any input value for commit headline is ignored.
     * @param expectedHeadOid
     *            The expected head OID of the pull request.
     * @param mergeMethod
     *            The merge method to use. If omitted, defaults to `MERGE`. NOTE: when merging with a merge queue any
     *            input value for merge method is ignored.
     * @throws IOException
     *             the io exception
     */
    public void enablePullRequestAutoMerge(String authorEmail,
            String clientMutationId,
            String commitBody,
            String commitHeadline,
            String expectedHeadOid,
            MergeMethod mergeMethod) throws IOException {

        StringBuilder inputBuilder = new StringBuilder();
        addParameter(inputBuilder, "pullRequestId", this.getNodeId());
        addOptionalParameter(inputBuilder, "authorEmail", authorEmail);
        addOptionalParameter(inputBuilder, "clientMutationId", clientMutationId);
        addOptionalParameter(inputBuilder, "commitBody", commitBody);
        addOptionalParameter(inputBuilder, "commitHeadline", commitHeadline);
        addOptionalParameter(inputBuilder, "expectedHeadOid", expectedHeadOid);
        addOptionalParameter(inputBuilder, "mergeMethod", mergeMethod);

        String graphqlBody = "mutation EnableAutoMerge { enablePullRequestAutoMerge(input: {" + inputBuilder + "}) { "
                + "pullRequest { id } } }";

        root().createGraphQLRequest(graphqlBody).sendGraphQL();

        refresh();
    }

    /**
     * Gets additions.
     *
     * @return the additions
     * @throws IOException
     *             the io exception
     */
    public int getAdditions() throws IOException {
        populate();
        return additions;
    }

    /**
     * The status of auto merging a pull request.
     *
     * @return the {@linkplain AutoMerge} or {@code null} if no auto merge is set.
     */
    public AutoMerge getAutoMerge() {
        return autoMerge;
    }

    /**
     * This points to where the change should be pulled into, but I'm not really sure what exactly it means.
     *
     * @return the base
     */
    public GHCommitPointer getBase() {
        return base;
    }

    /**
     * Gets changed files.
     *
     * @return the changed files
     * @throws IOException
     *             the io exception
     */
    public int getChangedFiles() throws IOException {
        populate();
        return changedFiles;
    }

    //
    // details that are only available via get with ID
    //

    /**
     * Gets the closed by.
     *
     * @return the closed by
     */
    @Override
    public GHUser getClosedBy() {
        return null;
    }

    /**
     * Gets the number of commits.
     *
     * @return the number of commits
     * @throws IOException
     *             the io exception
     */
    public int getCommits() throws IOException {
        populate();
        return commits;
    }

    /**
     * Gets deletions.
     *
     * @return the deletions
     * @throws IOException
     *             the io exception
     */
    public int getDeletions() throws IOException {
        populate();
        return deletions;
    }

    /**
     * The diff file, like https://github.com/jenkinsci/jenkins/pull/100.diff
     *
     * @return the diff url
     */
    public URL getDiffUrl() {
        return GitHubClient.parseURL(diffUrl);
    }

    /**
     * The change that should be pulled. The tip of the commits to merge.
     *
     * @return the head
     */
    public GHCommitPointer getHead() {
        return head;
    }

    /**
     * The URL of the patch file. like https://github.com/jenkinsci/jenkins/pull/100.patch
     *
     * @return the issue url
     */
    public URL getIssueUrl() {
        return GitHubClient.parseURL(issueUrl);
    }

    /**
     * See <a href="https://developer.github.com/changes/2013-04-25-deprecating-merge-commit-sha">GitHub blog post</a>
     *
     * @return the merge commit sha
     * @throws IOException
     *             the io exception
     */
    public String getMergeCommitSha() throws IOException {
        populate();
        return mergeCommitSha;
    }

    /**
     * Is this PR mergeable?.
     *
     * @return null if the state has not been determined yet, for example when a PR is newly created. If this method is
     *         called on an instance whose mergeable state is not yet known, API call is made to retrieve the latest
     *         state.
     * @throws IOException
     *             the io exception
     */
    public Boolean getMergeable() throws IOException {
        refresh(mergeable);
        return mergeable;
    }

    /**
     * Gets mergeable state.
     *
     * @return the mergeable state
     * @throws IOException
     *             the io exception
     */
    public String getMergeableState() throws IOException {
        populate();
        return mergeableState;
    }

    /**
     * Gets merged at.
     *
     * @return the merged at
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getMergedAt() {
        return GitHubClient.parseInstant(mergedAt);
    }

    /**
     * Gets merged by.
     *
     * @return the merged by
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getMergedBy() throws IOException {
        populate();
        return mergedBy;
    }

    /**
     * The URL of the patch file. like https://github.com/jenkinsci/jenkins/pull/100.patch
     *
     * @return the patch url
     */
    public URL getPatchUrl() {
        return GitHubClient.parseURL(patchUrl);
    }

    /**
     * Gets the pull request.
     *
     * @return the pull request
     */
    @Override
    public PullRequest getPullRequest() {
        return null;
    }

    /**
     * Gets requested reviewers.
     *
     * @return the requested reviewers
     * @throws IOException
     *             the io exception
     */
    public List<GHUser> getRequestedReviewers() throws IOException {
        refresh(requestedReviewers);
        return Collections.unmodifiableList(Arrays.asList(requestedReviewers));
    }

    /**
     * Gets requested teams.
     *
     * @return the requested teams
     * @throws IOException
     *             the io exception
     */
    public List<GHTeam> getRequestedTeams() throws IOException {
        refresh(requestedTeams);
        return Collections.unmodifiableList(Arrays.asList(requestedTeams));
    }

    /**
     * Gets review comments.
     *
     * @return the review comments
     * @throws IOException
     *             the io exception
     */
    public int getReviewComments() throws IOException {
        populate();
        return reviewComments;
    }

    /**
     * Is draft boolean.
     *
     * @return the boolean
     * @throws IOException
     *             the io exception
     */
    public boolean isDraft() throws IOException {
        populate();
        return draft;
    }

    /**
     * Is merged boolean.
     *
     * @return the boolean
     * @throws IOException
     *             the io exception
     */
    public boolean isMerged() throws IOException {
        populate();
        return merged;
    }

    /**
     * Retrieves all the commits associated to this pull request.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHPullRequestCommitDetail> listCommits() {
        return root().createRequest()
                .withUrlPath(String.format("%s/commits", getApiRoute()))
                .toIterable(GHPullRequestCommitDetail[].class, item -> item.wrapUp(this));
    }

    /**
     * Retrieves all the files associated to this pull request. The paginated response returns 30 files per page by
     * default.
     *
     * @return the paged iterable
     * @see <a href="https://docs.github.com/en/rest/reference/pulls#list-pull-requests-files">List pull requests
     *      files</a>
     */
    public PagedIterable<GHPullRequestFileDetail> listFiles() {
        return root().createRequest()
                .withUrlPath(String.format("%s/files", getApiRoute()))
                .toIterable(GHPullRequestFileDetail[].class, null);
    }

    /**
     * Obtains all the review comments associated with this pull request.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHPullRequestReviewComment> listReviewComments() {
        return root().createRequest()
                .withUrlPath(getApiRoute() + COMMENTS_ACTION)
                .toIterable(GHPullRequestReviewComment[].class, item -> item.wrapUp(this));
    }

    /**
     * Retrieves all the reviews associated to this pull request.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHPullRequestReview> listReviews() {
        return root().createRequest()
                .withUrlPath(String.format("%s/reviews", getApiRoute()))
                .toIterable(GHPullRequestReview[].class, item -> item.wrapUp(this));
    }

    /**
     * Merge this pull request.
     *
     * <p>
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *            Commit message. If null, the default one will be used.
     * @throws IOException
     *             the io exception
     */
    public void merge(String msg) throws IOException {
        merge(msg, null);
    }

    /**
     * Merge this pull request.
     *
     * <p>
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *            Commit message. If null, the default one will be used.
     * @param sha
     *            SHA that pull request head must match to allow merge.
     * @throws IOException
     *             the io exception
     */
    public void merge(String msg, String sha) throws IOException {
        merge(msg, sha, null);
    }

    /**
     * Merge this pull request, using the specified merge method.
     *
     * <p>
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *            Commit message. If null, the default one will be used.
     * @param sha
     *            the sha
     * @param method
     *            SHA that pull request head must match to allow merge.
     * @throws IOException
     *             the io exception
     */
    public void merge(String msg, String sha, MergeMethod method) throws IOException {
        root().createRequest()
                .method("PUT")
                .with("commit_message", msg)
                .with("sha", sha)
                .with("merge_method", method)
                .withUrlPath(getApiRoute() + "/merge")
                .send();
    }

    /**
     * Repopulates this object.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void refresh() throws IOException {
        if (isOffline()) {
            return; // cannot populate, will have to live with what we have
        }

        // we do not want to use getUrl() here as it points to the issues API
        // and not the pull request one
        URL absoluteUrl = GitHubRequest.getApiURL(root().getApiUrl(), getApiRoute());
        root().createRequest().setRawUrlPath(absoluteUrl.toString()).fetchInto(this).wrapUp(owner);
    }

    /**
     * Request reviewers.
     *
     * @param reviewers
     *            the reviewers
     * @throws IOException
     *             the io exception
     */
    public void requestReviewers(List<GHUser> reviewers) throws IOException {
        root().createRequest()
                .method("POST")
                .with("reviewers", getLogins(reviewers))
                .withUrlPath(getApiRoute() + REQUEST_REVIEWERS)
                .send();
    }

    /**
     * Request team reviewers.
     *
     * @param teams
     *            the teams
     * @throws IOException
     *             the io exception
     */
    public void requestTeamReviewers(List<GHTeam> teams) throws IOException {
        List<String> teamReviewers = new ArrayList<String>(teams.size());
        for (GHTeam team : teams) {
            teamReviewers.add(team.getSlug());
        }
        root().createRequest()
                .method("POST")
                .with("team_reviewers", teamReviewers)
                .withUrlPath(getApiRoute() + REQUEST_REVIEWERS)
                .send();
    }

    /**
     * Set the base branch on the pull request.
     *
     * @param newBaseBranch
     *            the name of the new base branch
     * @return the updated pull request
     * @throws IOException
     *             the io exception
     */
    public GHPullRequest setBaseBranch(String newBaseBranch) throws IOException {
        return root().createRequest()
                .method("PATCH")
                .with("base", newBaseBranch)
                .withUrlPath(getApiRoute())
                .fetch(GHPullRequest.class);
    }

    /**
     * Updates the branch. The same as pressing the button in the web GUI.
     *
     * @throws IOException
     *             the io exception
     */
    public void updateBranch() throws IOException {
        root().createRequest()
                .method("PUT")
                .with("expected_head_sha", head.getSha())
                .withUrlPath(getApiRoute() + "/update-branch")
                .send();
    }

    private void addOptionalParameter(StringBuilder inputBuilder, String name, Object value) {
        if (value != null) {
            addParameter(inputBuilder, name, value);
        }
    }

    private void addParameter(StringBuilder inputBuilder, String name, Object value) {
        Objects.requireNonNull(value);
        String formatString = " %s: \"%s\"";
        if (value instanceof Enum) {
            formatString = " %s: %s";
        }

        inputBuilder.append(String.format(formatString, name, value));
    }

    /**
     * Fully populate the data by retrieving missing data.
     *
     * <p>
     * Depending on the original API call where this object is created, it may not contain everything.
     */
    private void populate() throws IOException {
        if (mergeableState != null)
            return; // already populated
        refresh();
    }

    /**
     * Gets the api route.
     *
     * @return the api route
     */
    @Override
    protected String getApiRoute() {
        if (owner == null) {
            // Issues returned from search to do not have an owner. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            // The url sourced above is of the form '/repos/<owner>/<reponame>/issues/', which
            // subsequently issues requests against the `/issues/` handler, causing a 404 when
            // asking for, say, a list of commits associated with a PR. Replace the `/issues/`
            // with `/pulls/` to avoid that.
            return StringUtils.prependIfMissing(url.toString().replace(root().getApiUrl(), ""), "/")
                    .replace("/issues/", "/pulls/");
        }
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/pulls/" + number;
    }

    /**
     * for test purposes only.
     *
     * @return the mergeable no refresh
     */
    Boolean getMergeableNoRefresh() {
        return mergeable;
    }
    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH pull request
     */
    GHPullRequest wrapUp(GHRepository owner) {
        this.wrap(owner);
        return this;
    }
}
