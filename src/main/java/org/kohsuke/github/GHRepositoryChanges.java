package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Changes made to a repository.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHRepositoryChanges {

    /**
     * Repository name that was changed.
     */
    public static class FromName {

        private String from;

        /**
         * Create default FromName instance
         */
        public FromName() {
        }

        /**
         * Get previous name of the repository before rename.
         *
         * @return String
         */
        public String getFrom() {
            return from;
        }
    }

    /**
     * Owner from whom this repository was transferred.
     */
    public static class FromOwner {

        private GHOrganization organization;

        private GHUser user;
        /**
         * Create default FromOwner instance
         */
        public FromOwner() {
        }

        /**
         * Get organization from which this repository was transferred.
         *
         * @return GHOrganization
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHOrganization getOrganization() {
            return organization;
        }

        /**
         * Get user from which this repository was transferred.
         *
         * @return user
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHUser getUser() {
            return user;
        }
    }
    /**
     * Repository object from which the name was changed.
     */
    public static class FromRepository {

        private FromName name;

        /**
         * Create default FromRepository instance
         */
        public FromRepository() {
        }

        /**
         * Get top level object for the previous name of the repository.
         *
         * @return FromName
         */
        public FromName getName() {
            return name;
        }
    }

    /**
     * Outer object of owner from whom this repository was transferred.
     */
    public static class Owner {

        private FromOwner from;

        /**
         * Create default Owner instance
         */
        public Owner() {
        }

        /**
         * Get in owner object.
         *
         * @return FromOwner
         */
        public FromOwner getFrom() {
            return from;
        }
    }

    private Owner owner;

    private FromRepository repository;

    /**
     * Create default GHRepositoryChanges instance
     */
    public GHRepositoryChanges() {
    }

    /**
     * Get outer owner object.
     *
     * @return Owner
     */
    public Owner getOwner() {
        return owner;
    }

    /**
     * Get repository.
     *
     * @return FromRepository
     */
    public FromRepository getRepository() {
        return repository;
    }
}
