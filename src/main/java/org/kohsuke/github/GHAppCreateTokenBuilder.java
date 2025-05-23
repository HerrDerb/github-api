package org.kohsuke.github;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Creates a access token for a GitHub App Installation.
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation#createToken() GHAppInstallation#createToken()
 */
public class GHAppCreateTokenBuilder extends GitHubInteractiveObject {

    private final String apiUrlTail;
    /** The builder. */
    protected final Requester builder;

    /**
     * Instantiates a new GH app create token builder.
     *
     * @param root
     *            the root
     * @param apiUrlTail
     *            the api url tail
     */
    @BetaApi
    GHAppCreateTokenBuilder(GitHub root, String apiUrlTail) {
        super(root);
        this.apiUrlTail = apiUrlTail;
        this.builder = root.createRequest();
    }

    /**
     * Creates an app token with all the parameters.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @return a GHAppInstallationToken
     * @throws IOException
     *             on error
     */
    public GHAppInstallationToken create() throws IOException {
        return builder.method("POST").withUrlPath(apiUrlTail).fetch(GHAppInstallationToken.class);
    }

    /**
     * Set the permissions granted to the access token. The permissions object includes the permission names and their
     * access type.
     *
     * @param permissions
     *            Map containing the permission names and types.
     * @return a GHAppCreateTokenBuilder
     */
    @BetaApi
    public GHAppCreateTokenBuilder permissions(Map<String, GHPermissionType> permissions) {
        Map<String, String> retMap = new HashMap<>();
        for (Map.Entry<String, GHPermissionType> entry : permissions.entrySet()) {
            retMap.put(entry.getKey(), GitHubRequest.transformEnum(entry.getValue()));
        }
        builder.with("permissions", retMap);
        return this;
    }

    /**
     * By default the installation token has access to all repositories that the installation can access. To restrict
     * the access to specific repositories, you can provide repository names when creating the token.
     *
     * @param repositories
     *            Array containing the repository names
     * @return a GHAppCreateTokenBuilder
     */
    @BetaApi
    public GHAppCreateTokenBuilder repositories(List<String> repositories) {
        this.builder.with("repositories", repositories);
        return this;
    }

    /**
     * By default the installation token has access to all repositories that the installation can access. To restrict
     * the access to specific repositories, you can provide the repository_ids when creating the token. When you omit
     * repository_ids, the response does not contain neither the repositories nor the permissions key.
     *
     * @param repositoryIds
     *            Array containing the repositories Ids
     * @return a GHAppCreateTokenBuilder
     */
    @BetaApi
    public GHAppCreateTokenBuilder repositoryIds(List<Long> repositoryIds) {
        this.builder.with("repository_ids", repositoryIds);
        return this;
    }

}
