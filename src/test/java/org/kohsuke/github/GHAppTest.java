package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * Tests for the GitHub App API methods.
 *
 * @author Paulo Miguel Almeida
 */
public class GHAppTest extends AbstractGHAppInstallationTest {

    /**
     * Create default GHAppTest instance
     */
    public GHAppTest() {
    }

    /**
     * Creates the token.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void createToken() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");

        Map<String, GHPermissionType> permissions = new HashMap<String, GHPermissionType>();
        permissions.put("checks", GHPermissionType.WRITE);
        permissions.put("pull_requests", GHPermissionType.WRITE);
        permissions.put("contents", GHPermissionType.READ);
        permissions.put("metadata", GHPermissionType.READ);

        // Create token specifying both permissions and repository ids
        GHAppInstallationToken installationToken = installation.createToken(permissions)
                .repositoryIds(Collections.singletonList((long) 111111111))
                .create();

        assertThat(installationToken.getToken(), is("bogus"));
        assertThat(installation.getPermissions(), is(permissions));
        assertThat(installationToken.getRepositorySelection(), is(GHRepositorySelection.SELECTED));
        assertThat(installationToken.getExpiresAt(), is(GitHubClient.parseInstant("2019-08-10T05:54:58Z")));

        GHRepository repository = installationToken.getRepositories().get(0);
        assertThat(installationToken.getRepositories().size(), is(1));
        assertThat(repository.getId(), is((long) 111111111));
        assertThat(repository.getName(), is("bogus"));

        // Create token with no payload
        GHAppInstallationToken installationToken2 = installation.createToken().create();

        assertThat(installationToken2.getToken(), is("bogus"));
        assertThat(installationToken2.getPermissions().size(), is(4));
        assertThat(installationToken2.getRepositorySelection(), is(GHRepositorySelection.ALL));
        assertThat(installationToken2.getExpiresAt(), is(GitHubClient.parseInstant("2019-12-19T12:27:59Z")));

        assertThat(installationToken2.getRepositories(), nullValue());;
    }

    /**
     * Creates the token with repositories.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void createTokenWithRepositories() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");

        // Create token specifying repositories (not repository_ids!)
        GHAppInstallationToken installationToken = installation.createToken()
                .repositories(Collections.singletonList("bogus"))
                .create();

        assertThat(installationToken.getToken(), is("bogus"));
        assertThat(installationToken.getPermissions().entrySet(), hasSize(4));
        assertThat(installationToken.getRepositorySelection(), is(GHRepositorySelection.SELECTED));
        assertThat(installationToken.getExpiresAt(), is(GitHubClient.parseInstant("2022-07-27T21:38:33Z")));

        GHRepository repository = installationToken.getRepositories().get(0);
        assertThat(installationToken.getRepositories().size(), is(1));
        assertThat(repository.getId(), is((long) 11111111));
        assertThat(repository.getName(), is("bogus"));
    }

    /**
     * Delete installation.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void deleteInstallation() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");
        try {
            installation.deleteInstallation();
        } catch (IOException e) {
            fail("deleteInstallation wasn't suppose to fail in this test");
        }
    }

    /**
     * Gets the git hub app.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getGitHubApp() throws IOException {
        GHApp app = gitHub.getApp();
        assertThat(app.getId(), is((long) 82994));
        assertThat(app.getOwner().getId(), is((long) 7544739));
        assertThat(app.getOwner().getLogin(), is("hub4j-test-org"));
        assertThat(app.getOwner().getType(), is("Organization"));
        assertThat(app.getName(), is("GHApi Test app 1"));
        assertThat(app.getSlug(), is("ghapi-test-app-1"));
        assertThat(app.getDescription(), is(""));
        assertThat(app.getExternalUrl(), is("http://localhost"));
        assertThat(app.getHtmlUrl().toString(), is("https://github.com/apps/ghapi-test-app-1"));
        assertThat(app.getCreatedAt(), is(GitHubClient.parseInstant("2020-09-30T13:40:56Z")));
        assertThat(app.getUpdatedAt(), is(GitHubClient.parseInstant("2020-09-30T13:40:56Z")));
        assertThat(app.getPermissions().size(), is(2));
        assertThat(app.getEvents().size(), is(0));
        assertThat(app.getInstallationsCount(), is((long) 1));
    }

    /**
     * Gets the installation by id.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getInstallationById() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationById(1111111);
        testAppInstallation(installation);
    }

    /**
     * Gets the installation by organization.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getInstallationByOrganization() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByOrganization("bogus");
        testAppInstallation(installation);
    }

    /**
     * Gets the installation by repository.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getInstallationByRepository() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByRepository("bogus", "bogus");
        testAppInstallation(installation);
    }

    /**
     * Gets the installation by user.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getInstallationByUser() throws IOException {
        GHApp app = gitHub.getApp();
        GHAppInstallation installation = app.getInstallationByUser("bogus");
        testAppInstallation(installation);
    }

    /**
     * List installation requests.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listInstallationRequests() throws IOException {
        GHApp app = gitHub.getApp();
        List<GHAppInstallationRequest> installations = app.listInstallationRequests().toList();
        assertThat(installations.size(), is(1));

        GHAppInstallationRequest appInstallation = installations.get(0);
        assertThat(appInstallation.getId(), is((long) 1037204));
        assertThat(appInstallation.getAccount().getId(), is((long) 195438329));
        assertThat(appInstallation.getAccount().getLogin(), is("approval-test"));
        assertThat(appInstallation.getAccount().getType(), is("Organization"));
        assertThat(appInstallation.getRequester().getId(), is((long) 195437694));
        assertThat(appInstallation.getRequester().getLogin(), is("kaladinstormblessed2"));
        assertThat(appInstallation.getRequester().getType(), is("User"));
        assertThat(appInstallation.getCreatedAt(), is(GitHubClient.parseInstant("2025-01-17T15:50:51Z")));
        assertThat(appInstallation.getNodeId(), is("MDMwOkludGVncmF0aW9uSW5zdGFsbGF0aW9uUmVxdWVzdDEwMzcyMDQ="));
    }

    /**
     * List installations.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listInstallations() throws IOException {
        GHApp app = gitHub.getApp();
        List<GHAppInstallation> installations = app.listInstallations().toList();
        assertThat(installations.size(), is(1));

        GHAppInstallation appInstallation = installations.get(0);
        testAppInstallation(appInstallation);
    }

    /**
     * List installations that have been updated since a given date.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParseException
     *             Signals that a ParseException has occurred.
     *
     */
    @Test
    public void listInstallationsSince() throws IOException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date localDate = simpleDateFormat.parse("2023-11-01");
        Instant localInstant = LocalDate.parse("2023-11-01", DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        GHApp app = gitHub.getApp();
        List<GHAppInstallation> installations = app.listInstallations(localDate).toList();
        assertThat(installations.size(), is(1));

        GHAppInstallation appInstallation = installations.get(0);
        testAppInstallation(appInstallation);
    }

    private void testAppInstallation(GHAppInstallation appInstallation) throws IOException {
        Map<String, GHPermissionType> appPermissions = appInstallation.getPermissions();
        GHUser appAccount = appInstallation.getAccount();

        assertThat(appInstallation.getId(), is((long) 11111111));
        assertThat(appAccount.getId(), is((long) 111111111));
        assertThat(appAccount.login, is("bogus"));
        assertThat(appAccount.getType(), is("Organization"));
        assertThat(appInstallation.getRepositorySelection(), is(GHRepositorySelection.SELECTED));
        assertThat(appInstallation.getAccessTokenUrl(), endsWith("/app/installations/11111111/access_tokens"));
        assertThat(appInstallation.getRepositoriesUrl(), endsWith("/installation/repositories"));
        assertThat(appInstallation.getAppId(), is((long) 11111));
        assertThat(appInstallation.getTargetId(), is((long) 111111111));
        assertThat(appInstallation.getTargetType(), is(GHTargetType.ORGANIZATION));

        Map<String, GHPermissionType> permissionsMap = new HashMap<String, GHPermissionType>();
        permissionsMap.put("checks", GHPermissionType.WRITE);
        permissionsMap.put("pull_requests", GHPermissionType.WRITE);
        permissionsMap.put("contents", GHPermissionType.READ);
        permissionsMap.put("metadata", GHPermissionType.READ);
        assertThat(appPermissions, is(permissionsMap));

        List<GHEvent> events = Arrays.asList(GHEvent.PULL_REQUEST, GHEvent.PUSH);
        assertThat(appInstallation.getEvents(), containsInAnyOrder(events.toArray(new GHEvent[0])));
        assertThat(appInstallation.getCreatedAt(), is(GitHubClient.parseInstant("2019-07-04T01:19:36.000Z")));
        assertThat(appInstallation.getUpdatedAt(), is(GitHubClient.parseInstant("2019-07-30T22:48:09.000Z")));
        assertThat(appInstallation.getSingleFileName(), nullValue());
    }

    /**
     * Gets the git hub builder.
     *
     * @return the git hub builder
     */
    protected GitHubBuilder getGitHubBuilder() {
        return super.getGitHubBuilder()
                // ensure that only JWT will be used against the tests below
                .withOAuthToken(null, null)
                // Note that we used to provide a bogus token here and to rely on (apparently) manually crafted/edited
                // Wiremock recordings, so most of the tests cannot actually be executed against GitHub without
                // relying on the Wiremock recordings.
                // Some tests have been updated, though (getGitHubApp in particular).
                .withAuthorizationProvider(jwtProvider1);
    }

}
