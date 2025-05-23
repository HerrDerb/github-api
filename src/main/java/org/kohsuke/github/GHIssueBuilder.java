package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The type GHIssueBuilder.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHIssueBuilder {
    private List<String> assignees = new ArrayList<String>();
    private final Requester builder;
    private List<String> labels = new ArrayList<String>();
    private final GHRepository repo;

    /**
     * Instantiates a new GH issue builder.
     *
     * @param repo
     *            the repo
     * @param title
     *            the title
     */
    GHIssueBuilder(GHRepository repo, String title) {
        this.repo = repo;
        this.builder = repo.root().createRequest().method("POST");
        builder.with("title", title);
    }

    /**
     * Assignee gh issue builder.
     *
     * @param user
     *            the user
     * @return the gh issue builder
     */
    public GHIssueBuilder assignee(GHUser user) {
        if (user != null)
            assignees.add(user.getLogin());
        return this;
    }

    /**
     * Assignee gh issue builder.
     *
     * @param user
     *            the user
     * @return the gh issue builder
     */
    public GHIssueBuilder assignee(String user) {
        if (user != null)
            assignees.add(user);
        return this;
    }

    /**
     * Sets the main text of an issue, which is arbitrary multi-line text.
     *
     * @param str
     *            the str
     * @return the gh issue builder
     */
    public GHIssueBuilder body(String str) {
        builder.with("body", str);
        return this;
    }

    /**
     * Creates a new issue.
     *
     * @return the gh issue
     * @throws IOException
     *             the io exception
     */
    public GHIssue create() throws IOException {
        return builder.with("labels", labels)
                .with("assignees", assignees)
                .withUrlPath(repo.getApiTailUrl("issues"))
                .fetch(GHIssue.class)
                .wrap(repo);
    }

    /**
     * Label gh issue builder.
     *
     * @param label
     *            the label
     * @return the gh issue builder
     */
    public GHIssueBuilder label(String label) {
        if (label != null)
            labels.add(label);
        return this;
    }

    /**
     * Milestone gh issue builder.
     *
     * @param milestone
     *            the milestone
     * @return the gh issue builder
     */
    public GHIssueBuilder milestone(GHMilestone milestone) {
        if (milestone != null)
            builder.with("milestone", milestone.getNumber());
        return this;
    }
}
