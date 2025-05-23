package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * A discussion in GitHub Team.
 *
 * @author Charles Moulliard
 * @see <a href="https://developer.github.com/v3/teams/discussions">GitHub Team Discussions</a>
 */
public class GHDiscussion extends GHObject {

    /**
     * A {@link GHLabelBuilder} that creates a new {@link GHLabel}
     *
     * Consumer must call {@link Creator#done()} to create the new instance.
     */
    public static class Creator extends GHDiscussionBuilder<Creator> {

        private Creator(@Nonnull GHTeam team) {
            super(GHDiscussion.Creator.class, team, null);
            requester.method("POST").setRawUrlPath(getRawUrlPath(team, null));
        }

        /**
         * Sets whether this discussion is private to this team.
         *
         * @param value
         *            privacy of this discussion
         * @return either a continuing builder or an updated {@link GHDiscussion}
         * @throws IOException
         *             if there is an I/O Exception
         */
        @Nonnull
        public Creator private_(boolean value) throws IOException {
            return with("private", value);
        }
    }

    /**
     * A {@link GHLabelBuilder} that updates a single property per request
     *
     * {@link GitHubRequestBuilderDone#done()} is called automatically after the property is set.
     */
    public static class Setter extends GHDiscussionBuilder<GHDiscussion> {
        private Setter(@Nonnull GHDiscussion base) {
            super(GHDiscussion.class, base.team, base);
            requester.method("PATCH").setRawUrlPath(base.getUrl().toString());
        }
    }
    /**
     * A {@link GHLabelBuilder} that allows multiple properties to be updated per request.
     *
     * Consumer must call {@link Updater#done()} to commit changes.
     */
    public static class Updater extends GHDiscussionBuilder<Updater> {
        private Updater(@Nonnull GHDiscussion base) {
            super(GHDiscussion.Updater.class, base.team, base);
            requester.method("PATCH").setRawUrlPath(base.getUrl().toString());
        }
    }
    private static String getRawUrlPath(@Nonnull GHTeam team, @CheckForNull Long discussionNumber) {
        return team.getUrl().toString() + "/discussions" + (discussionNumber == null ? "" : "/" + discussionNumber);
    }

    /**
     * Begins the creation of a new instance.
     *
     * Consumer must call {@link GHDiscussion.Creator#done()} to commit changes.
     *
     * @param team
     *            the team in which the discussion will be created.
     * @return a {@link GHLabel.Creator}
     */
    static GHDiscussion.Creator create(GHTeam team) {
        return new GHDiscussion.Creator(team);
    }

    /**
     * Read.
     *
     * @param team
     *            the team
     * @param discussionNumber
     *            the discussion number
     * @return the GH discussion
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    static GHDiscussion read(GHTeam team, long discussionNumber) throws IOException {
        return team.root()
                .createRequest()
                .setRawUrlPath(getRawUrlPath(team, discussionNumber))
                .fetch(GHDiscussion.class)
                .wrapUp(team);
    }

    /**
     * Read all.
     *
     * @param team
     *            the team
     * @return the paged iterable
     */
    static PagedIterable<GHDiscussion> readAll(GHTeam team) {
        return team.root()
                .createRequest()
                .setRawUrlPath(getRawUrlPath(team, null))
                .toIterable(GHDiscussion[].class, item -> item.wrapUp(team));
    }

    private String body, title, htmlUrl;

    @JsonProperty(value = "private")
    private boolean isPrivate;

    private long number;

    private GHTeam team;

    /**
     * Create default GHDiscussion instance
     */
    public GHDiscussion() {
    }

    /**
     * Delete the discussion.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        team.root().createRequest().method("DELETE").setRawUrlPath(getRawUrlPath(team, number)).send();
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GHDiscussion that = (GHDiscussion) o;
        return number == that.number && Objects.equals(getUrl(), that.getUrl()) && Objects.equals(team, that.team)
                && Objects.equals(body, that.body) && Objects.equals(title, that.title);
    }

    /**
     * The description of this discussion.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * The id number of this discussion. GitHub discussions have "number" instead of "id". This is provided for
     * convenience.
     *
     * @return the id number for this discussion
     * @see #getNumber()
     */
    @Override
    public long getId() {
        return getNumber();
    }

    /**
     * The number of this discussion.
     *
     * @return the number
     */
    public long getNumber() {
        return number;
    }

    /**
     * Get the team to which this discussion belongs.
     *
     * @return the team for this discussion
     */
    @Nonnull
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHTeam getTeam() {
        return team;
    }

    /**
     * Get the title of the discussion.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(team, number, body, title);
    }

    /**
     * Whether the discussion is private to the team.
     *
     * @return {@code true} if discussion is private.
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * Begins a single property update.
     *
     * @return a {@link GHDiscussion.Setter}
     */
    public GHDiscussion.Setter set() {
        return new GHDiscussion.Setter(this);
    }

    /**
     * Begins a batch update
     *
     * Consumer must call {@link GHDiscussion.Updater#done()} to commit changes.
     *
     * @return a {@link GHDiscussion.Updater}
     */
    public GHDiscussion.Updater update() {
        return new GHDiscussion.Updater(this);
    }

    /**
     * Wrap up.
     *
     * @param team
     *            the team
     * @return the GH discussion
     */
    GHDiscussion wrapUp(GHTeam team) {
        this.team = team;
        return this;
    }
}
