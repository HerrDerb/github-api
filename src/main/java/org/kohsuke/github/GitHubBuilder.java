package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.authorization.AuthorizationProvider;
import org.kohsuke.github.authorization.ImmutableAuthorizationProvider;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Configures connection details and produces {@link GitHub}.
 *
 * @since 1.59
 */
public class GitHubBuilder implements Cloneable {

    /** The home directory. */
    // for testing
    static File HOME_DIRECTORY = null;

    /**
     * Creates {@link GitHubBuilder} by picking up coordinates from environment variables.
     *
     * <p>
     * The following environment variables are recognized:
     *
     * <ul>
     * <li>GITHUB_LOGIN: username like 'kohsuke'
     * <li>GITHUB_OAUTH: OAuth token to login
     * <li>GITHUB_ENDPOINT: URL of the API endpoint
     * <li>GITHUB_JWT: JWT token to login
     * </ul>
     *
     * <p>
     * See class javadoc for the relationship between these coordinates.
     *
     * @return the GitHubBuilder
     */
    public static GitHubBuilder fromEnvironment() {
        Properties props = new Properties();
        for (Entry<String, String> e : System.getenv().entrySet()) {
            String name = e.getKey().toLowerCase(Locale.ENGLISH);
            if (name.startsWith("github_"))
                name = name.substring(7);
            props.put(name, e.getValue());
        }
        return fromProperties(props);
    }

    /**
     * From properties GitHubBuilder.
     *
     * @param props
     *            the props
     * @return the GitHubBuilder
     */
    public static GitHubBuilder fromProperties(Properties props) {
        GitHubBuilder self = new GitHubBuilder();
        String oauth = props.getProperty("oauth");
        String jwt = props.getProperty("jwt");
        String login = props.getProperty("login");

        if (oauth != null) {
            self.withOAuthToken(oauth, login);
        }
        if (jwt != null) {
            self.withJwtToken(jwt);
        }
        self.withEndpoint(props.getProperty("endpoint", GitHubClient.GITHUB_URL));
        return self;
    }

    /**
     * From property file GitHubBuilder.
     *
     * @return the GitHubBuilder
     * @throws IOException
     *             the io exception
     */
    public static GitHubBuilder fromPropertyFile() throws IOException {
        File homeDir = HOME_DIRECTORY != null ? HOME_DIRECTORY : new File(System.getProperty("user.home"));
        File propertyFile = new File(homeDir, ".github");
        return fromPropertyFile(propertyFile.getPath());
    }
    /**
     * From property file GitHubBuilder.
     *
     * @param propertyFileName
     *            the property file name
     * @return the GitHubBuilder
     * @throws IOException
     *             the io exception
     */
    public static GitHubBuilder fromPropertyFile(String propertyFileName) throws IOException {
        Properties props = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(propertyFileName);
            props.load(in);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return fromProperties(props);
    }
    private static void loadIfSet(String envName, Properties p, String propName) {
        String v = System.getenv(envName);
        if (v != null)
            p.put(propName, v);
    }

    /**
     * First check if the credentials are configured in the environment. We use environment first because users are not
     * likely to give required (full) permissions to their default key.
     *
     * If no user is specified it means there is no configuration present, so try using the ~/.github properties file.
     **
     * If there is still no user it means there are no credentials defined and throw an IOException.
     *
     * @return the configured Builder from credentials defined on the system or in the environment. Otherwise returns
     *         null.
     *
     * @throws IOException
     *             If there are no credentials defined in the ~/.github properties file or the process environment.
     */
    static GitHubBuilder fromCredentials() throws IOException {
        Exception cause = null;
        GitHubBuilder builder = null;

        builder = fromEnvironment();

        if (builder.authorizationProvider != AuthorizationProvider.ANONYMOUS)
            return builder;

        try {
            builder = fromPropertyFile();

            if (builder.authorizationProvider != AuthorizationProvider.ANONYMOUS)
                return builder;
        } catch (FileNotFoundException e) {
            // fall through
            cause = e;
        }
        throw (IOException) new IOException("Failed to resolve credentials from ~/.github or the environment.")
                .initCause(cause);
    }

    private GitHubAbuseLimitHandler abuseLimitHandler = GitHubAbuseLimitHandler.WAIT;

    private GitHubConnector connector;

    private GitHubRateLimitChecker rateLimitChecker = new GitHubRateLimitChecker();

    private GitHubRateLimitHandler rateLimitHandler = GitHubRateLimitHandler.WAIT;

    /** The authorization provider. */
    /* private */ AuthorizationProvider authorizationProvider = AuthorizationProvider.ANONYMOUS;

    // default scoped so unit tests can read them.
    /** The endpoint. */
    /* private */ String endpoint = GitHubClient.GITHUB_URL;

    /**
     * Instantiates a new Git hub builder.
     */
    public GitHubBuilder() {
    }

    /**
     * Builds a {@link GitHub} instance.
     *
     * @return the github
     * @throws IOException
     *             the io exception
     */
    public GitHub build() throws IOException {
        return new GitHub(endpoint,
                connector,
                rateLimitHandler,
                abuseLimitHandler,
                rateLimitChecker,
                authorizationProvider);
    }

    /**
     * Clone.
     *
     * @return the GitHubBuilder
     */
    @Override
    public GitHubBuilder clone() {
        try {
            return (GitHubBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone should be supported", e);
        }
    }

    /**
     * Adds a {@link GitHubAbuseLimitHandler} to this {@link GitHubBuilder}.
     * <p>
     * When a client sends too many requests in a short time span, GitHub may return an error and set a header telling
     * the client to not make any more request for some period of time. If this happens,
     * {@link GitHubAbuseLimitHandler#onError(GitHubConnectorResponse)} will be called.
     * </p>
     *
     * @param handler
     *            the handler
     * @return the GitHubBuilder
     */
    public GitHubBuilder withAbuseLimitHandler(GitHubAbuseLimitHandler handler) {
        this.abuseLimitHandler = handler;
        return this;
    }

    /**
     * Configures {@link GitHubBuilder} with Installation Token generated by the GitHub Application.
     *
     * @param appInstallationToken
     *            A string containing the GitHub App installation token
     * @return the configured Builder from given GitHub App installation token.
     * @see GHAppInstallation#createToken() GHAppInstallation#createToken()
     */
    public GitHubBuilder withAppInstallationToken(String appInstallationToken) {
        return withAuthorizationProvider(ImmutableAuthorizationProvider.fromAppInstallationToken(appInstallationToken));
    }

    /**
     * Configures a {@link AuthorizationProvider} for this builder
     *
     * There can be only one authorization provider per client instance.
     *
     * @param authorizationProvider
     *            the authorization provider
     * @return the GitHubBuilder
     *
     */
    public GitHubBuilder withAuthorizationProvider(final AuthorizationProvider authorizationProvider) {
        this.authorizationProvider = authorizationProvider;
        return this;
    }

    /**
     * With connector GitHubBuilder.
     *
     * @param connector
     *            the connector
     * @return the GitHubBuilder
     */
    public GitHubBuilder withConnector(GitHubConnector connector) {
        this.connector = connector;
        return this;
    }

    /**
     * With endpoint GitHubBuilder.
     *
     * @param endpoint
     *            The URL of GitHub (or GitHub enterprise) API endpoint, such as "https://api.github.com" or
     *            "https://ghe.acme.com/api/v3". Note that GitHub Enterprise has <code>/api/v3</code> in the URL. For
     *            historical reasons, this parameter still accepts the bare domain name, but that's considered
     *            deprecated.
     * @return the GitHubBuilder
     */
    public GitHubBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * With jwt token GitHubBuilder.
     *
     * @param jwtToken
     *            the jwt token
     * @return the GitHubBuilder
     */
    public GitHubBuilder withJwtToken(String jwtToken) {
        return withAuthorizationProvider(ImmutableAuthorizationProvider.fromJwtToken(jwtToken));
    }

    /**
     * With o auth token GitHubBuilder.
     *
     * @param oauthToken
     *            the oauth token
     * @return the GitHubBuilder
     */
    public GitHubBuilder withOAuthToken(String oauthToken) {
        return withAuthorizationProvider(ImmutableAuthorizationProvider.fromOauthToken(oauthToken));
    }

    /**
     * With o auth token GitHubBuilder.
     *
     * @param oauthToken
     *            the oauth token
     * @param user
     *            the user
     * @return the GitHubBuilder
     */
    public GitHubBuilder withOAuthToken(String oauthToken, String user) {
        return withAuthorizationProvider(ImmutableAuthorizationProvider.fromOauthToken(oauthToken, user));
    }

    /**
     * Adds a {@link RateLimitChecker} for the Core API for this {@link GitHubBuilder}.
     *
     * @param coreRateLimitChecker
     *            the {@link RateLimitChecker} for core GitHub API requests
     * @return the GitHubBuilder
     * @see #withRateLimitChecker(RateLimitChecker, RateLimitTarget)
     */
    public GitHubBuilder withRateLimitChecker(@Nonnull RateLimitChecker coreRateLimitChecker) {
        return withRateLimitChecker(coreRateLimitChecker, RateLimitTarget.CORE);
    }

    /**
     * Adds a {@link RateLimitChecker} to this {@link GitHubBuilder}.
     * <p>
     * GitHub allots a certain number of requests to each user or application per period of time (usually per hour). The
     * number of requests remaining is returned in the response header and can also be requested using
     * {@link GitHub#getRateLimit()}. This requests per interval is referred to as the "rate limit".
     * </p>
     * <p>
     * GitHub prefers that clients stop before exceeding their rate limit rather than stopping after they exceed it. The
     * {@link RateLimitChecker} is called before each request to check the rate limit and wait if the checker criteria
     * are met.
     * </p>
     * <p>
     * Checking your rate limit using {@link GitHub#getRateLimit()} does not effect your rate limit, but each
     * {@link GitHub} instance will attempt to cache and reuse the last seen rate limit rather than making a new
     * request.
     * </p>
     *
     * @param rateLimitChecker
     *            the {@link RateLimitChecker} for requests
     * @param rateLimitTarget
     *            the {@link RateLimitTarget} specifying which rate limit record to check
     * @return the GitHubBuilder
     */
    public GitHubBuilder withRateLimitChecker(@Nonnull RateLimitChecker rateLimitChecker,
            @Nonnull RateLimitTarget rateLimitTarget) {
        this.rateLimitChecker = this.rateLimitChecker.with(rateLimitChecker, rateLimitTarget);
        return this;
    }

    /**
     * Adds a {@link GitHubRateLimitHandler} to this {@link GitHubBuilder}.
     * <p>
     * GitHub allots a certain number of requests to each user or application per period of time (usually per hour). The
     * number of requests remaining is returned in the response header and can also be requested using
     * {@link GitHub#getRateLimit()}. This requests per interval is referred to as the "rate limit".
     * </p>
     * <p>
     * When the remaining number of requests reaches zero, the next request will return an error. If this happens,
     * {@link GitHubRateLimitHandler#onError(GitHubConnectorResponse)} will be called.
     * </p>
     * <p>
     * NOTE: GitHub treats clients that exceed their rate limit very harshly. If possible, clients should avoid
     * exceeding their rate limit. Consider adding a {@link RateLimitChecker} to automatically check the rate limit for
     * each request and wait if needed.
     * </p>
     *
     * @param handler
     *            the handler
     * @return the GitHubBuilder
     * @see #withRateLimitChecker(RateLimitChecker)
     */
    public GitHubBuilder withRateLimitHandler(GitHubRateLimitHandler handler) {
        this.rateLimitHandler = handler;
        return this;
    }
}
