package org.kohsuke.github;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Builder pattern for updating a Gist.
 *
 * @author Martin van Zijl
 */
public class GHGistUpdater {
    private final GHGist base;
    private final Requester builder;

    /** The files. */
    LinkedHashMap<String, Map<String, String>> files;

    /**
     * Instantiates a new GH gist updater.
     *
     * @param base
     *            the base
     */
    GHGistUpdater(GHGist base) {
        this.base = base;
        this.builder = base.root().createRequest();

        files = new LinkedHashMap<>();
    }

    /**
     * Add file gh gist updater.
     *
     * @param fileName
     *            the file name
     * @param content
     *            the content
     * @return the gh gist updater
     */
    public GHGistUpdater addFile(@Nonnull String fileName, @Nonnull String content) {
        updateFile(fileName, content);
        return this;
    }

    /**
     * Delete file.
     *
     * @param fileName
     *            the file name
     * @return the GH gist updater
     */
    public GHGistUpdater deleteFile(@Nonnull String fileName) {
        files.put(fileName, null);
        return this;
    }

    /**
     * Description gh gist updater.
     *
     * @param desc
     *            the desc
     * @return the gh gist updater
     */
    public GHGistUpdater description(String desc) {
        builder.with("description", desc);
        return this;
    }

    /**
     * Rename file gh gist updater.
     *
     * @param fileName
     *            the file name
     * @param newFileName
     *            the new file name
     * @return the gh gist updater
     */
    public GHGistUpdater renameFile(@Nonnull String fileName, @Nonnull String newFileName) {
        Map<String, String> file = files.computeIfAbsent(fileName, d -> new HashMap<>());
        file.put("filename", newFileName);
        return this;
    }

    /**
     * Updates the Gist based on the parameters specified thus far.
     *
     * @return the gh gist
     * @throws IOException
     *             the io exception
     */
    public GHGist update() throws IOException {
        builder.with("files", files);
        return builder.method("PATCH").withUrlPath(base.getApiTailUrl("")).fetch(GHGist.class);
    }

    /**
     * Update file gh gist updater.
     *
     * @param fileName
     *            the file name
     * @param content
     *            the content
     * @return the gh gist updater
     */
    public GHGistUpdater updateFile(@Nonnull String fileName, @Nonnull String content) {
        Map<String, String> file = files.computeIfAbsent(fileName, d -> new HashMap<>());
        file.put("content", content);
        return this;
    }

    /**
     * Update file name and content.
     *
     * @param fileName
     *            the file name
     * @param newFileName
     *            the new file name
     * @param content
     *            the content
     * @return the gh gist updater
     */
    public GHGistUpdater updateFile(@Nonnull String fileName, @Nonnull String newFileName, @Nonnull String content) {
        Map<String, String> file = files.computeIfAbsent(fileName, d -> new HashMap<>());
        file.put("content", content);
        file.put("filename", newFileName);
        files.put(fileName, file);
        return this;
    }
}
