package org.kohsuke.github;

import org.kohsuke.github.GHRepository.Visibility;

import java.io.IOException;
import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * The Class GHRepositoryBuilder.
 *
 * @param <S>
 *            the generic type
 */
abstract class GHRepositoryBuilder<S> extends AbstractBuilder<GHRepository, S> {

    /**
     * Instantiates a new GH repository builder.
     *
     * @param intermediateReturnType
     *            the intermediate return type
     * @param root
     *            the root
     * @param baseInstance
     *            the base instance
     */
    protected GHRepositoryBuilder(Class<S> intermediateReturnType, GitHub root, GHRepository baseInstance) {
        super(GHRepository.class, intermediateReturnType, root, baseInstance);
    }

    /**
     * Allow or disallow private forks
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S allowForking(boolean enabled) throws IOException {
        return with("allow_forking", enabled);
    }

    /**
     * Allow or disallow merging pull requests with a merge commit.
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S allowMergeCommit(boolean enabled) throws IOException {
        return with("allow_merge_commit", enabled);
    }

    /**
     * Allow or disallow rebase-merging pull requests.
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S allowRebaseMerge(boolean enabled) throws IOException {
        return with("allow_rebase_merge", enabled);
    }

    /**
     * Allow or disallow squash-merging pull requests.
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S allowSquashMerge(boolean enabled) throws IOException {
        return with("allow_squash_merge", enabled);
    }

    /**
     * Default repository branch.
     *
     * @param branch
     *            branch name
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S defaultBranch(String branch) throws IOException {
        return with("default_branch", branch);
    }

    /**
     * After pull requests are merged, you can have head branches deleted automatically.
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S deleteBranchOnMerge(boolean enabled) throws IOException {
        return with("delete_branch_on_merge", enabled);
    }

    /**
     * Description for repository.
     *
     * @param description
     *            description of repository
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S description(String description) throws IOException {
        return with("description", description);
    }

    /**
     * Done.
     *
     * @return the GH repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public GHRepository done() throws IOException {
        return super.done();
    }

    /**
     * Enables downloads.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S downloads(boolean enabled) throws IOException {
        return with("has_downloads", enabled);
    }

    /**
     * Homepage for repository.
     *
     * @param homepage
     *            homepage of repository
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S homepage(String homepage) throws IOException {
        return with("homepage", homepage);
    }

    /**
     * Homepage for repository.
     *
     * @param homepage
     *            homepage of repository
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S homepage(URL homepage) throws IOException {
        return homepage(homepage.toExternalForm());
    }

    /**
     * Specifies whether the repository is a template.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S isTemplate(boolean enabled) throws IOException {
        return with("is_template", enabled);
    }

    /**
     * Enables issue tracker.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S issues(boolean enabled) throws IOException {
        return with("has_issues", enabled);
    }

    /**
     * Sets the repository to private.
     *
     * @param enabled
     *            private if true
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S private_(boolean enabled) throws IOException {
        return with("private", enabled);
    }

    /**
     * Enables projects.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S projects(boolean enabled) throws IOException {
        return with("has_projects", enabled);
    }

    /**
     * Sets the repository visibility.
     *
     * @param visibility
     *            visibility of repository
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S visibility(final Visibility visibility) throws IOException {
        return with("visibility", visibility.toString());
    }

    /**
     * Enables wiki.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S wiki(boolean enabled) throws IOException {
        return with("has_wiki", enabled);
    }

    /**
     * Archive.
     *
     * @return the s
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    S archive() throws IOException {
        return with("archived", true);
    }

    /**
     * Name.
     *
     * @param name
     *            the name
     * @return the s
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    S name(String name) throws IOException {
        return with("name", name);
    }
}
