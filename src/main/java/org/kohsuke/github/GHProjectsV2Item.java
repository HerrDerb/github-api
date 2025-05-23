package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import org.kohsuke.github.internal.EnumUtils;

import java.net.URL;
import java.time.Instant;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * A Projects V2 item in the organization.
 * <p>
 * Projects V2 are not attached to a repository but to an organization, even if it is possible to create shortcuts at
 * the repository level.
 * <p>
 * This event exposes the GraphQL object (more or less - the ids are handled differently for instance) directly. The new
 * Projects V2 API is only available through GraphQL so for now you cannot execute any actions on this object.
 *
 * @author Guillaume Smet
 * @see <a href=
 *      "https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-api-to-manage-projects">The
 *      GraphQL API for Projects V2</a>
 */
public class GHProjectsV2Item extends GHObject {

    /**
     * The Enum ContentType.
     */
    public enum ContentType {

        /** The draftissue. */
        DRAFTISSUE,
        /** The issue. */
        ISSUE,
        /** The pullrequest. */
        PULLREQUEST,
        /** The unknown. */
        UNKNOWN;
    }

    private String archivedAt;
    private String contentNodeId;
    private String contentType;

    private GHUser creator;
    private String projectNodeId;

    /**
     * Create default GHProjectsV2Item instance
     */
    public GHProjectsV2Item() {
    }

    /**
     * Gets the archived at.
     *
     * @return the archived at
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getArchivedAt() {
        return GitHubClient.parseInstant(archivedAt);
    }

    /**
     * Gets the content node id.
     *
     * @return the content node id
     */
    public String getContentNodeId() {
        return contentNodeId;
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public ContentType getContentType() {
        return EnumUtils.getEnumOrDefault(ContentType.class, contentType, ContentType.UNKNOWN);
    }

    /**
     * Gets the creator.
     *
     * @return the creator
     */
    public GHUser getCreator() {
        return root().intern(creator);
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        throw new IllegalStateException(getClass().getName() + " does not offer a HTML URL.");
    }

    /**
     * Gets the project node id.
     *
     * @return the project node id
     */
    public String getProjectNodeId() {
        return projectNodeId;
    }
}
