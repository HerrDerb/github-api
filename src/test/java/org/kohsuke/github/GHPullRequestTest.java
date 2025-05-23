package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHPullRequest.AutoMerge;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.kohsuke.github.GHPullRequestReviewComment.Side.LEFT;
import static org.kohsuke.github.GHPullRequestReviewComment.Side.RIGHT;

// TODO: Auto-generated Javadoc
/**
 * The Class GHPullRequestTest.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHPullRequestTest extends AbstractGitHubWireMockTest {

    /**
     * Create default GHPullRequestTest instance
     */
    public GHPullRequestTest() {
    }

    /**
     * Adds the labels.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void addLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest("addLabels", "test/stable", "main", "## test");
        String addedLabel1 = "addLabels_label_name_1";
        String addedLabel2 = "addLabels_label_name_2";
        String addedLabel3 = "addLabels_label_name_3";

        List<GHLabel> resultingLabels = p.addLabels(addedLabel1);
        assertThat(resultingLabels.size(), equalTo(1));
        GHLabel ghLabel = resultingLabels.get(0);
        assertThat(ghLabel.getName(), equalTo(addedLabel1));

        int requestCount = mockGitHub.getRequestCount();
        resultingLabels = p.addLabels(addedLabel2, addedLabel3);
        // multiple labels can be added with one api call
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 1));

        assertThat(resultingLabels.size(), equalTo(3));
        assertThat(resultingLabels,
                containsInAnyOrder(hasProperty("name", equalTo(addedLabel1)),
                        hasProperty("name", equalTo(addedLabel2)),
                        hasProperty("name", equalTo(addedLabel3))));

        // Adding a label which is already present does not throw an error
        resultingLabels = p.addLabels(ghLabel);
        assertThat(resultingLabels.size(), equalTo(3));
    }

    /**
     * Adds the labels concurrency issue.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void addLabelsConcurrencyIssue() throws Exception {
        String addedLabel1 = "addLabelsConcurrencyIssue_label_name_1";
        String addedLabel2 = "addLabelsConcurrencyIssue_label_name_2";

        GHPullRequest p1 = getRepository()
                .createPullRequest("addLabelsConcurrencyIssue", "test/stable", "main", "## test");
        p1.getLabels();

        GHPullRequest p2 = getRepository().getPullRequest(p1.getNumber());
        p2.addLabels(addedLabel2);

        Collection<GHLabel> labels = p1.addLabels(addedLabel1);

        assertThat(labels.size(), equalTo(2));
        assertThat(labels,
                containsInAnyOrder(hasProperty("name", equalTo(addedLabel1)),
                        hasProperty("name", equalTo(addedLabel2))));
    }

    /**
     * Check non existent author.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void checkNonExistentAuthor() throws IOException {
        // PR id is based on https://github.com/sahansera/TestRepo/pull/2
        final GHPullRequest pullRequest = getRepository().getPullRequest(2);

        assertThat(pullRequest.getUser(), is(notNullValue()));
        assertThat(pullRequest.getUser().login, is("ghost"));
    }

    /**
     * Check non existent reviewer.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void checkNonExistentReviewer() throws IOException {
        // PR id is based on https://github.com/sahansera/TestRepo/pull/1
        final GHPullRequest pullRequest = getRepository().getPullRequest(1);
        final Optional<GHPullRequestReview> review = pullRequest.listReviews().toList().stream().findFirst();
        final GHUser reviewer = review.get().getUser();

        assertThat(pullRequest.getRequestedReviewers(), is(empty()));
        assertThat(review, notNullValue());
        assertThat(reviewer, is(nullValue()));
    }

    /**
     * Check pull request reviewer.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void checkPullRequestReviewer() throws IOException {
        // PR id is based on https://github.com/sahansera/TestRepo/pull/6
        final GHPullRequest pullRequest = getRepository().getPullRequest(6);
        final Optional<GHPullRequestReview> review = pullRequest.listReviews().toList().stream().findFirst();
        final GHUser reviewer = review.get().getUser();

        assertThat(review, notNullValue());
        assertThat(reviewer, notNullValue());
    }

    /**
     * Clean up.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    @After
    public void cleanUp() throws Exception {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        for (GHPullRequest pr : getRepository(this.getNonRecordingGitHub()).queryPullRequests()
                .state(GHIssueState.OPEN)
                .list()
                .toList()) {
            pr.close();
        }
    }

    /**
     * Close pull request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void closePullRequest() throws Exception {
        String name = "closePullRequest";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "main", "## test");
        // System.out.println(p.getUrl());
        assertThat(p.getTitle(), equalTo(name));
        assertThat(getRepository().getPullRequest(p.getNumber()).getState(), equalTo(GHIssueState.OPEN));
        p.close();
        assertThat(getRepository().getPullRequest(p.getNumber()).getState(), equalTo(GHIssueState.CLOSED));
    }

    /**
     * Comments objects in pull request review builder.
     */
    @Test
    public void commentsInPullRequestReviewBuilder() {
        GHPullRequestReviewBuilder.DraftReviewComment draftReviewComment = new GHPullRequestReviewBuilder.DraftReviewComment(
                "comment",
                "path/to/file.txt",
                1);
        assertThat(draftReviewComment.getBody(), equalTo("comment"));
        assertThat(draftReviewComment.getPath(), equalTo("path/to/file.txt"));
        assertThat(draftReviewComment.getPosition(), equalTo(1));

        GHPullRequestReviewBuilder.SingleLineDraftReviewComment singleLineDraftReviewComment = new GHPullRequestReviewBuilder.SingleLineDraftReviewComment(
                "comment",
                "path/to/file.txt",
                2);
        assertThat(singleLineDraftReviewComment.getBody(), equalTo("comment"));
        assertThat(singleLineDraftReviewComment.getPath(), equalTo("path/to/file.txt"));
        assertThat(singleLineDraftReviewComment.getLine(), equalTo(2));

        GHPullRequestReviewBuilder.MultilineDraftReviewComment multilineDraftReviewComment = new GHPullRequestReviewBuilder.MultilineDraftReviewComment(
                "comment",
                "path/to/file.txt",
                1,
                2);
        assertThat(multilineDraftReviewComment.getBody(), equalTo("comment"));
        assertThat(multilineDraftReviewComment.getPath(), equalTo("path/to/file.txt"));
        assertThat(multilineDraftReviewComment.getStartLine(), equalTo(1));
        assertThat(multilineDraftReviewComment.getLine(), equalTo(2));
    }

    /**
     * Creates the draft pull request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createDraftPullRequest() throws Exception {
        String name = "createDraftPullRequest";
        GHRepository repo = getRepository();
        GHPullRequest p = repo.createPullRequest(name, "test/stable", "main", "## test", false, true);
        assertThat(p.getTitle(), equalTo(name));
        assertThat(p.canMaintainerModify(), is(false));
        assertThat(p.isDraft(), is(true));

        // There are multiple paths to get PRs and each needs to read draft correctly
        p.draft = false;
        p.refresh();
        assertThat(p.isDraft(), is(true));

        GHPullRequest p2 = repo.getPullRequest(p.getNumber());
        assertThat(p2.getNumber(), is(p.getNumber()));
        assertThat(p2.isDraft(), is(true));

        p = repo.queryPullRequests().state(GHIssueState.OPEN).head("test/stable").list().toList().get(0);
        assertThat(p2.getNumber(), is(p.getNumber()));
        assertThat(p.isDraft(), is(true));
    }

    /**
     * Creates the pull request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createPullRequest() throws Exception {
        String name = "createPullRequest";
        GHRepository repo = getRepository();
        GHPullRequest p = repo.createPullRequest(name, "test/stable", "main", "## test");
        assertThat(p.getTitle(), equalTo(name));
        assertThat(p.canMaintainerModify(), is(false));
        assertThat(p.isDraft(), is(false));

        // Check auto merge status of the pull request
        final AutoMerge autoMerge = p.getAutoMerge();
        assertThat(autoMerge, is(notNullValue()));
        assertThat(autoMerge.getCommitMessage(), equalTo("This is a auto merged squash commit message"));
        assertThat(autoMerge.getCommitTitle(), equalTo("This is a auto merged squash commit"));
        assertThat(autoMerge.getMergeMethod(), equalTo(GHPullRequest.MergeMethod.SQUASH));
        assertThat(autoMerge.getEnabledBy(), is(notNullValue()));
    }

    /**
     *
     * Test enabling auto merge for pull request
     *
     * @throws IOException
     *             the Exception
     */
    @Test
    public void enablePullRequestAutoMerge() throws IOException {
        String authorEmail = "sa20207@naver.com";
        String clientMutationId = "github-api";
        String commitBody = "This is commit body.";
        String commitTitle = "This is commit title.";
        String expectedCommitHeadOid = "4888b44d7204dd05680e90159af839c8b1194b6d";

        GHPullRequest pullRequest = gitHub.getRepository("seate/for-test").getPullRequest(9);

        pullRequest.enablePullRequestAutoMerge(authorEmail,
                clientMutationId,
                commitBody,
                commitTitle,
                expectedCommitHeadOid,
                GHPullRequest.MergeMethod.MERGE);

        AutoMerge autoMerge = pullRequest.getAutoMerge();
        assertThat(autoMerge.getEnabledBy().getEmail(), is(authorEmail));
        assertThat(autoMerge.getCommitMessage(), is(commitBody));
        assertThat(autoMerge.getCommitTitle(), is(commitTitle));
        assertThat(autoMerge.getMergeMethod(), is(GHPullRequest.MergeMethod.MERGE));
    }

    /**
     * Test enabling auto merge for pull request with no verified email throws GraphQL exception
     *
     * @throws IOException
     *             the io exception
     */
    @Test
    public void enablePullRequestAutoMergeFailure() throws IOException {
        String authorEmail = "failureEmail@gmail.com";
        String clientMutationId = "github-api";
        String commitBody = "This is commit body.";
        String commitTitle = "This is commit title.";
        String expectedCommitHeadOid = "4888b44d7204dd05680e90159af839c8b1194b6d";

        GHPullRequest pullRequest = gitHub.getRepository("seate/for-test").getPullRequest(9);

        try {
            pullRequest.enablePullRequestAutoMerge(authorEmail,
                    clientMutationId,
                    commitBody,
                    commitTitle,
                    expectedCommitHeadOid,
                    null);
        } catch (IOException e) {
            assertThat(e.getMessage(), containsString("does not have a verified email"));
        }
    }

    /**
     * Get list of commits from searched PR.
     *
     * This would result in a wrong API URL used, resulting in a GHFileNotFoundException.
     *
     * For more details, please have a look at the bug description in https://github.com/hub4j/github-api/issues/1778.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getListOfCommits() throws Exception {
        String name = "getListOfCommits";
        GHPullRequestSearchBuilder builder = getRepository().searchPullRequests().isClosed();
        Optional<GHPullRequest> firstPR = builder.list().toList().stream().findFirst();

        try {
            GHPullRequestCommitDetail detail = firstPR.get().listCommits().toArray()[0];
            assertThat(detail.getApiUrl().toString(), notNullValue());
            assertThat(detail.getSha(), equalTo("2d29c787b46ce61b98a1c13e05e21ebc21f49dbf"));
            assertThat(detail.getCommentsUrl().toString(),
                    endsWith(
                            "/repos/hub4j-test-org/github-api/commits/2d29c787b46ce61b98a1c13e05e21ebc21f49dbf/comments"));
            assertThat(detail.getUrl().toString(),
                    equalTo("https://github.com/hub4j-test-org/github-api/commit/2d29c787b46ce61b98a1c13e05e21ebc21f49dbf"));

            GHPullRequestCommitDetail.Commit commit = detail.getCommit();
            assertThat(commit, notNullValue());
            assertThat(commit.getAuthor().getEmail(), equalTo("bitwiseman@gmail.com"));
            assertThat(commit.getCommitter().getEmail(), equalTo("bitwiseman@gmail.com"));
            assertThat(commit.getMessage(), equalTo("Update README"));
            assertThat(commit.getUrl().toString(),
                    endsWith("/repos/hub4j-test-org/github-api/git/commits/2d29c787b46ce61b98a1c13e05e21ebc21f49dbf"));
            assertThat(commit.getComment_count(), equalTo(0));

            GHPullRequestCommitDetail.Tree tree = commit.getTree();
            assertThat(tree, notNullValue());
            assertThat(tree.getSha(), equalTo("ce7a1ba95aba901cf08d9f8365410d290d6c23aa"));
            assertThat(tree.getUrl().toString(),
                    endsWith("/repos/hub4j-test-org/github-api/git/trees/ce7a1ba95aba901cf08d9f8365410d290d6c23aa"));

            GHPullRequestCommitDetail.CommitPointer[] parents = detail.getParents();
            assertThat(parents, notNullValue());
            assertThat(parents.length, equalTo(1));
            assertThat(parents[0].getSha(), equalTo("3a09d2de4a9a1322a0ba2c3e2f54a919ca8fe353"));
            assertThat(parents[0].getHtml_url().toString(),
                    equalTo("https://github.com/hub4j-test-org/github-api/commit/3a09d2de4a9a1322a0ba2c3e2f54a919ca8fe353"));
            assertThat(parents[0].getUrl().toString(),
                    endsWith("/repos/hub4j-test-org/github-api/commits/3a09d2de4a9a1322a0ba2c3e2f54a919ca8fe353"));

        } catch (GHFileNotFoundException e) {
            if (e.getMessage().contains("/issues/")) {
                fail("Issued a request against the wrong path");
            }
        }
    }

    /**
     * Gets the user test.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getUserTest() throws IOException {
        GHPullRequest p = getRepository().createPullRequest("getUserTest", "test/stable", "main", "## test");
        GHPullRequest prSingle = getRepository().getPullRequest(p.getNumber());
        assertThat(prSingle.getUser().root(), notNullValue());
        prSingle.getMergeable();
        assertThat(prSingle.getUser().root(), notNullValue());

        List<GHPullRequest> ghPullRequests = getRepository().getPullRequests(GHIssueState.OPEN);
        for (GHPullRequest pr : ghPullRequests) {
            assertThat(pr.getUser().root(), notNullValue());
            pr.getMergeable();
            assertThat(pr.getUser().root(), notNullValue());
        }
    }

    /**
     * Merge commit SHA.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void mergeCommitSHA() throws Exception {
        String name = "mergeCommitSHA";
        GHRepository repo = getRepository();
        GHPullRequest p = repo.createPullRequest(name, "test/mergeable_branch", "main", "## test");
        int baseRequestCount = mockGitHub.getRequestCount();
        assertThat(p.getMergeableNoRefresh(), nullValue());
        assertThat("Used existing value", mockGitHub.getRequestCount() - baseRequestCount, equalTo(0));

        // mergeability computation takes time, this should still be null immediately after creation
        assertThat(p.getMergeable(), nullValue());
        assertThat("Asked for PR information", mockGitHub.getRequestCount() - baseRequestCount, equalTo(1));

        for (int i = 2; i <= 10; i++) {
            if (Boolean.TRUE.equals(p.getMergeable()) && p.getMergeCommitSha() != null) {
                assertThat("Asked for PR information", mockGitHub.getRequestCount() - baseRequestCount, equalTo(i));

                // make sure commit exists
                GHCommit commit = repo.getCommit(p.getMergeCommitSha());
                assertThat(commit, notNullValue());

                assertThat("Asked for PR information", mockGitHub.getRequestCount() - baseRequestCount, equalTo(i + 1));

                return;
            }

            // mergeability computation takes time. give it more chance
            Thread.sleep(1000);
        }
        // hmm?
        fail();
    }

    /**
     * Pull request comment.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pullRequestComment() throws Exception {
        String name = "createPullRequestComment";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "main", "## test");
        assertThat(p.getIssueUrl().toString(), endsWith("/repos/hub4j-test-org/github-api/issues/461"));

        List<GHIssueComment> comments;
        comments = p.listComments().toList();
        assertThat(comments, hasSize(0));
        comments = p.queryComments().list().toList();
        assertThat(comments, hasSize(0));

        GHIssueComment firstComment = p.comment("First comment");
        Instant firstCommentCreatedAt = firstComment.getCreatedAt();
        Instant firstCommentCreatedAtPlus1Second = firstComment.getCreatedAt().plusSeconds(1);

        comments = p.listComments().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("First comment"))));
        comments = p.queryComments().list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("First comment"))));

        // Test "since"
        comments = p.queryComments().since(firstCommentCreatedAt).list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("First comment"))));
        comments = p.queryComments().since(firstCommentCreatedAtPlus1Second).list().toList();
        assertThat(comments, hasSize(0));

        // "since" is only precise up to the second,
        // so if we want to differentiate comments, we need to be completely sure they're created
        // at least 1 second from each other.
        // Waiting 2 seconds to avoid edge cases.
        Thread.sleep(2000);

        GHIssueComment secondComment = p.comment("Second comment");
        Instant secondCommentCreatedAt = secondComment.getCreatedAt();
        Instant secondCommentCreatedAtPlus1Second = secondComment.getCreatedAt().plusSeconds(1);
        assertThat(
                "There's an error in the setup of this test; please fix it."
                        + " The second comment should be created at least one second after the first one.",
                firstCommentCreatedAtPlus1Second.isBefore(secondCommentCreatedAt));

        comments = p.listComments().toList();
        assertThat(comments, hasSize(2));
        assertThat(comments,
                contains(hasProperty("body", equalTo("First comment")),
                        hasProperty("body", equalTo("Second comment"))));
        comments = p.queryComments().list().toList();
        assertThat(comments, hasSize(2));
        assertThat(comments,
                contains(hasProperty("body", equalTo("First comment")),
                        hasProperty("body", equalTo("Second comment"))));

        // Test "since"
        comments = p.queryComments().since(firstCommentCreatedAt).list().toList();
        assertThat(comments, hasSize(2));
        assertThat(comments,
                contains(hasProperty("body", equalTo("First comment")),
                        hasProperty("body", equalTo("Second comment"))));
        comments = p.queryComments().since(firstCommentCreatedAtPlus1Second).list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("Second comment"))));
        comments = p.queryComments().since(secondCommentCreatedAt).list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("Second comment"))));
        comments = p.queryComments().since(secondCommentCreatedAtPlus1Second).list().toList();
        assertThat(comments, hasSize(0));

        // Test "since" with timestamp instead of Date
        comments = p.queryComments().since(secondCommentCreatedAt.toEpochMilli()).list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("Second comment"))));
    }

    /**
     * Pull request review comments.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pullRequestReviewComments() throws Exception {
        String name = "pullRequestReviewComments";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "main", "## test");
        try {
            assertThat(p.listReviewComments().toList(), is(empty()));

            // Call the deprecated method to ensure continued support
            p.createReviewComment("Sample review comment", p.getHead().getSha(), "README.md", 1);
            p.createReviewComment()
                    .commitId(p.getHead().getSha())
                    .body("A single line review comment")
                    .path("README.md")
                    .line(2)
                    .side(RIGHT)
                    .create();
            p.createReviewComment()
                    .commitId(p.getHead().getSha())
                    .body("A multiline review comment")
                    .path("README.md")
                    .lines(2, 3)
                    .sides(RIGHT, RIGHT)
                    .create();
            List<GHPullRequestReviewComment> comments = p.listReviewComments().toList();
            assertThat(comments.size(), equalTo(3));

            GHPullRequestReviewComment comment = comments.get(0);
            assertThat(comment.getBody(), equalTo("Sample review comment"));
            assertThat(comment.getInReplyToId(), equalTo(-1L));
            assertThat(comment.getPath(), equalTo("README.md"));
            assertThat(comment.getPosition(), equalTo(1));
            assertThat(comment.getDiffHunk(), equalTo("@@ -1,3 +1,4 @@\n-Java API for GitHub"));
            assertThat(comment.getCommitId(), equalTo("07374fe73aff1c2024a8d4114b32406c7a8e89b7"));
            assertThat(comment.getOriginalCommitId(), equalTo("07374fe73aff1c2024a8d4114b32406c7a8e89b7"));
            assertThat(comment.getAuthorAssociation(), equalTo(GHCommentAuthorAssociation.MEMBER));
            assertThat(comment.getUser(), notNullValue());
            assertThat(comment.getStartLine(), equalTo(-1));
            assertThat(comment.getOriginalStartLine(), equalTo(-1));
            assertThat(comment.getStartSide(), equalTo(GHPullRequestReviewComment.Side.UNKNOWN));
            assertThat(comment.getLine(), equalTo(1));
            assertThat(comment.getOriginalLine(), equalTo(1));
            assertThat(comment.getSide(), equalTo(LEFT));
            assertThat(comment.getPullRequestUrl(), notNullValue());
            assertThat(comment.getPullRequestUrl().toString(), containsString("hub4j-test-org/github-api/pulls/"));
            assertThat(comment.getBodyHtml(), nullValue());
            assertThat(comment.getBodyText(), nullValue());
            // Assert htmlUrl is not null
            assertThat(comment.getHtmlUrl(), notNullValue());
            assertThat(comment.getHtmlUrl().toString(),
                    containsString("hub4j-test-org/github-api/pull/" + p.getNumber()));

            comment = comments.get(1);
            assertThat(comment.getBody(), equalTo("A single line review comment"));
            assertThat(comment.getLine(), equalTo(2));
            assertThat(comment.getSide(), equalTo(RIGHT));

            comment = comments.get(2);
            assertThat(comment.getBody(), equalTo("A multiline review comment"));
            assertThat(comment.getStartLine(), equalTo(2));
            assertThat(comment.getLine(), equalTo(3));
            assertThat(comment.getStartSide(), equalTo(RIGHT));
            assertThat(comment.getSide(), equalTo(RIGHT));

            comment.createReaction(ReactionContent.EYES);
            GHReaction toBeRemoved = comment.createReaction(ReactionContent.CONFUSED);
            comment.createReaction(ReactionContent.ROCKET);
            comment.createReaction(ReactionContent.HOORAY);
            comment.createReaction(ReactionContent.HEART);
            comment.createReaction(ReactionContent.MINUS_ONE);
            comment.createReaction(ReactionContent.PLUS_ONE);
            comment.createReaction(ReactionContent.LAUGH);
            GHPullRequestReviewCommentReactions commentReactions = p.listReviewComments()
                    .toList()
                    .get(2)
                    .getReactions();
            assertThat(commentReactions.getUrl().toString(), equalTo(comment.getUrl().toString().concat("/reactions")));
            assertThat(commentReactions.getTotalCount(), equalTo(8));
            assertThat(commentReactions.getPlusOne(), equalTo(1));
            assertThat(commentReactions.getMinusOne(), equalTo(1));
            assertThat(commentReactions.getLaugh(), equalTo(1));
            assertThat(commentReactions.getHooray(), equalTo(1));
            assertThat(commentReactions.getConfused(), equalTo(1));
            assertThat(commentReactions.getHeart(), equalTo(1));
            assertThat(commentReactions.getRocket(), equalTo(1));
            assertThat(commentReactions.getEyes(), equalTo(1));

            comment.deleteReaction(toBeRemoved);

            List<GHReaction> reactions = comment.listReactions().toList();
            assertThat(reactions.size(), equalTo(7));

            GHReaction reaction = comment.createReaction(ReactionContent.CONFUSED);
            assertThat(reaction.getContent(), equalTo(ReactionContent.CONFUSED));

            reactions = comment.listReactions().toList();
            assertThat(reactions.size(), equalTo(8));

            comment.deleteReaction(reaction);

            reactions = comment.listReactions().toList();
            assertThat(reactions.size(), equalTo(7));

            GHPullRequestReviewComment reply = comment.reply("This is a reply.");
            assertThat(reply.getInReplyToId(), equalTo(comment.getId()));
            comments = p.listReviewComments().toList();

            assertThat(comments.size(), equalTo(4));

            comment.update("Updated review comment");
            comments = p.listReviewComments().toList();
            comment = comments.get(2);
            assertThat(comment.getBody(), equalTo("Updated review comment"));

            comment.delete();
            comments = p.listReviewComments().toList();
            // Reply is still present after delete of original comment, but no longer has replyToId
            assertThat(comments.size(), equalTo(3));
            assertThat(comments.get(2).getId(), equalTo(reply.getId()));
            assertThat(comments.get(2).getInReplyToId(), equalTo(-1L));
        } finally {
            p.close();
        }
    }

    /**
     * Pull request reviews.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void pullRequestReviews() throws Exception {
        String name = "testPullRequestReviews";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "main", "## test");

        List<GHPullRequestReview> reviews = p.listReviews().toList();
        assertThat(reviews.size(), is(0));

        GHPullRequestReview draftReview = p.createReview()
                .body("Some draft review")
                .comment("Some niggle", "README.md", 1)
                .singleLineComment("A single line comment", "README.md", 2)
                .multiLineComment("A multiline comment", "README.md", 2, 3)
                .create();
        assertThat(draftReview.getState(), is(GHPullRequestReviewState.PENDING));
        assertThat(draftReview.getBody(), is("Some draft review"));
        assertThat(draftReview.getCommitId(), notNullValue());
        reviews = p.listReviews().toList();
        assertThat(reviews.size(), is(1));
        GHPullRequestReview review = reviews.get(0);
        assertThat(review.getState(), is(GHPullRequestReviewState.PENDING));
        assertThat(review.getBody(), is("Some draft review"));
        assertThat(review.getCommitId(), notNullValue());
        draftReview.submit("Some review comment", GHPullRequestReviewEvent.COMMENT);
        List<GHPullRequestReviewComment> comments = review.listReviewComments().toList();
        assertThat(comments.size(), equalTo(3));
        GHPullRequestReviewComment comment = comments.get(0);
        assertThat(comment.getBody(), equalTo("Some niggle"));
        comment = comments.get(1);
        assertThat(comment.getBody(), equalTo("A single line comment"));
        assertThat(comment.getPosition(), equalTo(4));
        comment = comments.get(2);
        assertThat(comment.getBody(), equalTo("A multiline comment"));
        assertThat(comment.getPosition(), equalTo(5));
        draftReview = p.createReview().body("Some new review").comment("Some niggle", "README.md", 1).create();
        draftReview.delete();
    }

    /**
     * Query pull requests qualified head.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void queryPullRequestsQualifiedHead() throws Exception {
        GHRepository repo = getRepository();
        // Create PRs from two different branches to main
        repo.createPullRequest("queryPullRequestsQualifiedHead_stable", "test/stable", "main", null);
        repo.createPullRequest("queryPullRequestsQualifiedHead_rc", "test/rc", "main", null);

        // Query by one of the heads and make sure we only get that branch's PR back.
        List<GHPullRequest> prs = repo.queryPullRequests()
                .state(GHIssueState.OPEN)
                .head("hub4j-test-org:test/stable")
                .base("main")
                .list()
                .toList();
        assertThat(prs, notNullValue());
        assertThat(prs.size(), equalTo(1));
        assertThat(prs.get(0).getHead().getRef(), equalTo("test/stable"));
    }

    /**
     * Query pull requests unqualified head.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void queryPullRequestsUnqualifiedHead() throws Exception {
        GHRepository repo = getRepository();
        // Create PRs from two different branches to main
        repo.createPullRequest("queryPullRequestsUnqualifiedHead_stable", "test/stable", "main", null);
        repo.createPullRequest("queryPullRequestsUnqualifiedHead_rc", "test/rc", "main", null);

        // Query by one of the heads and make sure we only get that branch's PR back.
        List<GHPullRequest> prs = repo.queryPullRequests()
                .state(GHIssueState.OPEN)
                .head("test/stable")
                .base("main")
                .list()
                .toList();
        assertThat(prs, notNullValue());
        assertThat(prs.size(), equalTo(1));
        assertThat(prs.get(0).getHead().getRef(), equalTo("test/stable"));
    }

    /**
     * Create/Delete reaction for pull requests.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void reactions() throws Exception {
        String name = "createPullRequest";
        GHRepository repo = getRepository();
        GHPullRequest p = repo.createPullRequest(name, "test/stable", "main", "## test");

        assertThat(p.listReactions().toList(), hasSize(0));
        GHReaction reaction = p.createReaction(ReactionContent.CONFUSED);
        assertThat(p.listReactions().toList(), hasSize(1));

        p.deleteReaction(reaction);
        assertThat(p.listReactions().toList(), hasSize(0));
    }

    /**
     * Test refreshing a PR coming from the search results.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void refreshFromSearchResults() throws Exception {
        // To re-record, uncomment the Thread.sleep() calls below
        snapshotNotAllowed();

        String prName = "refreshFromSearchResults";
        GHRepository repository = getRepository();

        repository.createPullRequest(prName, "test/stable", "main", "## test");

        // we need to wait a bit for the pull request to be indexed by GitHub
        // Thread.sleep(2000);

        GHPullRequest pullRequestFromSearchResults = repository.searchPullRequests()
                .isOpen()
                .titleLike(prName)
                .list()
                .toList()
                .get(0);

        pullRequestFromSearchResults.getMergeableState();

        // wait a bit for the mergeable state to get populated
        // Thread.sleep(5000);

        assertThat("Pull request is supposed to have been refreshed and have a mergeable state",
                pullRequestFromSearchResults.getMergeableState(),
                equalTo("clean"));

        pullRequestFromSearchResults.close();
    }

    /**
     * Removes the labels.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void removeLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest("removeLabels", "test/stable", "main", "## test");
        String label1 = "removeLabels_label_name_1";
        String label2 = "removeLabels_label_name_2";
        String label3 = "removeLabels_label_name_3";
        p.setLabels(label1, label2, label3);

        Collection<GHLabel> labels = getRepository().getPullRequest(p.getNumber()).getLabels();
        assertThat(labels.size(), equalTo(3));
        GHLabel ghLabel3 = labels.stream().filter(label -> label3.equals(label.getName())).findFirst().get();

        int requestCount = mockGitHub.getRequestCount();
        List<GHLabel> resultingLabels = p.removeLabels(label2, label3);
        // each label deleted is a separate api call
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 2));

        assertThat(resultingLabels.size(), equalTo(1));
        assertThat(resultingLabels.get(0).getName(), equalTo(label1));

        // Removing some labels that are not present does not throw
        // This is consistent with earlier behavior and with addLabels()
        p.removeLabels(ghLabel3);

        // Calling removeLabel() on label that is not present will throw
        try {
            p.removeLabel(label3);
            fail("Expected GHFileNotFoundException");
        } catch (GHFileNotFoundException e) {
            assertThat(e.getMessage(), containsString("Label does not exist"));
        }
    }

    /**
     * Sets the assignee.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void setAssignee() throws Exception {
        GHPullRequest p = getRepository().createPullRequest("setAssignee", "test/stable", "main", "## test");
        GHMyself user = gitHub.getMyself();
        p.assignTo(user);

        assertThat(getRepository().getPullRequest(p.getNumber()).getAssignee(), equalTo(user));
    }

    /**
     * Sets the base branch.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void setBaseBranch() throws Exception {
        String prName = "testSetBaseBranch";
        String originalBaseBranch = "main";
        String newBaseBranch = "gh-pages";

        GHPullRequest pullRequest = getRepository().createPullRequest(prName, "test/stable", "main", "## test");

        assertThat("Pull request base branch is supposed to be " + originalBaseBranch,
                pullRequest.getBase().getRef(),
                equalTo(originalBaseBranch));

        GHPullRequest responsePullRequest = pullRequest.setBaseBranch(newBaseBranch);

        assertThat("Pull request base branch is supposed to be " + newBaseBranch,
                responsePullRequest.getBase().getRef(),
                equalTo(newBaseBranch));
    }

    /**
     * Sets the base branch non existing.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void setBaseBranchNonExisting() throws Exception {
        String prName = "testSetBaseBranchNonExisting";
        String originalBaseBranch = "main";
        String newBaseBranch = "non-existing";

        GHPullRequest pullRequest = getRepository().createPullRequest(prName, "test/stable", "main", "## test");

        assertThat("Pull request base branch is supposed to be " + originalBaseBranch,
                pullRequest.getBase().getRef(),
                equalTo(originalBaseBranch));

        try {
            pullRequest.setBaseBranch(newBaseBranch);
        } catch (HttpException e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.toString(), containsString("Proposed base branch 'non-existing' was not found"));
        }

        pullRequest.close();
    }

    /**
     * Sets the labels.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void setLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest("setLabels", "test/stable", "main", "## test");
        String label = "setLabels_label_name";
        p.setLabels(label);

        Collection<GHLabel> labels = getRepository().getPullRequest(p.getNumber()).getLabels();
        assertThat(labels.size(), equalTo(1));
        GHLabel savedLabel = labels.iterator().next();
        assertThat(savedLabel.getName(), equalTo(label));
        assertThat(savedLabel.getId(), notNullValue());
        assertThat(savedLabel.getNodeId(), notNullValue());
        assertThat(savedLabel.isDefault(), is(false));
    }

    /**
     * Squash merge.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void squashMerge() throws Exception {
        String name = "squashMerge";
        String branchName = "test/" + name;
        GHRef mainRef = getRepository().getRef("heads/main");
        GHRef branchRef = getRepository().createRef("refs/heads/" + branchName, mainRef.getObject().getSha());

        getRepository().createContent().content(name).path(name).message(name).branch(branchName).commit();
        Thread.sleep(1000);
        GHPullRequest p = getRepository().createPullRequest(name, branchName, "main", "## test squash");
        Thread.sleep(1000);
        p.merge("squash merge", null, GHPullRequest.MergeMethod.SQUASH);
    }

    /**
     * Test pull request review requests.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testPullRequestReviewRequests() throws Exception {
        String name = "testPullRequestReviewRequests";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "main", "## test");
        // System.out.println(p.getUrl());
        assertThat(p.getRequestedReviewers(), is(empty()));

        GHUser kohsuke2 = gitHub.getUser("kohsuke2");
        p.requestReviewers(Collections.singletonList(kohsuke2));
        p.refresh();
        assertThat(p.getRequestedReviewers(), is(not(empty())));
    }

    /**
     * Test pull request team review requests.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testPullRequestTeamReviewRequests() throws Exception {
        String name = "testPullRequestTeamReviewRequests";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "main", "## test");
        // System.out.println(p.getUrl());
        assertThat(p.getRequestedReviewers(), is(empty()));

        GHOrganization testOrg = gitHub.getOrganization("hub4j-test-org");
        GHTeam testTeam = testOrg.getTeamBySlug("dummy-team");

        p.requestTeamReviewers(Collections.singletonList(testTeam));

        int baseRequestCount = mockGitHub.getRequestCount();
        p.refresh();
        assertThat("We should not eagerly load organizations for teams",
                mockGitHub.getRequestCount() - baseRequestCount,
                equalTo(1));
        assertThat(p.getRequestedTeams().size(), equalTo(1));
        assertThat("We should not eagerly load organizations for teams",
                mockGitHub.getRequestCount() - baseRequestCount,
                equalTo(1));
        assertThat("Org should be queried for automatically if asked for",
                p.getRequestedTeams().get(0).getOrganization(),
                notNullValue());
        assertThat("Request count should show lazy load occurred",
                mockGitHub.getRequestCount() - baseRequestCount,
                equalTo(2));
    }

    /**
     * Update content squash merge.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void updateContentSquashMerge() throws Exception {
        String name = "updateContentSquashMerge";
        String branchName = "test/" + name;

        GHRef mainRef = getRepository().getRef("heads/main");
        GHRef branchRef = getRepository().createRef("refs/heads/" + branchName, mainRef.getObject().getSha());

        GHContentUpdateResponse response = getRepository().createContent()
                .content(name)
                .path(name)
                .branch(branchName)
                .message(name)
                .commit();

        Thread.sleep(1000);

        getRepository().createContent()
                .content(name + name)
                .path(name)
                .branch(branchName)
                .message(name)
                .sha(response.getContent().getSha())
                .commit();
        GHPullRequest p = getRepository().createPullRequest(name, branchName, "main", "## test squash");
        Thread.sleep(1000);
        p.merge("squash merge", null, GHPullRequest.MergeMethod.SQUASH);
    }

    /**
     * Update outdated branches.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void updateOutdatedBranches() throws Exception {
        String prName = "testUpdateOutdatedBranches";
        String outdatedRefName = "refs/heads/outdated";
        GHRepository repository = gitHub.getOrganization("hub4j-test-org").getRepository("updateOutdatedBranches");

        repository.getRef(outdatedRefName).updateTo("6440189369f9f33b2366556a94dbc26f2cfdd969", true);

        GHPullRequest outdatedPullRequest = repository.createPullRequest(prName, "outdated", "main", "## test");

        do {
            Thread.sleep(5000);
            outdatedPullRequest.refresh();
        } while (outdatedPullRequest.getMergeableState().equalsIgnoreCase("unknown"));

        assertThat("Pull request is supposed to be not up to date",
                outdatedPullRequest.getMergeableState(),
                equalTo("behind"));

        outdatedPullRequest.updateBranch();
        outdatedPullRequest.refresh();

        assertThat("Pull request is supposed to be up to date", outdatedPullRequest.getMergeableState(), not("behind"));

        outdatedPullRequest.close();
    }

    /**
     * Update outdated branches unexpected head.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void updateOutdatedBranchesUnexpectedHead() throws Exception {
        String prName = "testUpdateOutdatedBranches";
        String outdatedRefName = "refs/heads/outdated";
        GHRepository repository = gitHub.getOrganization("hub4j-test-org").getRepository("updateOutdatedBranches");

        GHRef outdatedRef = repository.getRef(outdatedRefName);
        outdatedRef.updateTo("6440189369f9f33b2366556a94dbc26f2cfdd969", true);

        GHPullRequest outdatedPullRequest = repository.createPullRequest(prName, "outdated", "main", "## test");

        do {
            Thread.sleep(5000);
            outdatedPullRequest.refresh();
        } while (outdatedPullRequest.getMergeableState().equalsIgnoreCase("unknown"));

        assertThat("Pull request is supposed to be not up to date",
                outdatedPullRequest.getMergeableState(),
                equalTo("behind"));

        outdatedRef.updateTo("f567328eb81270487864963b7d7446953353f2b5", true);

        try {
            outdatedPullRequest.updateBranch();
        } catch (HttpException e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.toString(), containsString("expected head sha didn’t match current head ref."));
        }

        outdatedPullRequest.close();
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }
}
