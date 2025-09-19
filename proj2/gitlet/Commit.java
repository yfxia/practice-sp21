package gitlet;

import java.io.File;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static gitlet.Utils.*;


/** Represents a gitlet commit object.
 *  It encompasses all the properties about a commit object.
 *  @author Sophia Xia
 */
public class Commit implements Serializable {

    /** Folder that commits located at*/
    static final File OBJECT_FOLDER = join(Repository.GITLET_DIR, "objects");

    static File blobsPath(String id) {
        return join(OBJECT_FOLDER, "blobs", id.substring(0, 2), id.substring(2));
    }

    static File commitsPath(String id) {
        return join(OBJECT_FOLDER, "commits", id.substring(0, 2), id.substring(2));
    }

    /** serialVersionUID is a long that the JVM writes alongside each serialized object. */
    private static final long serialVersionUID = 1L;

    // Persisted fields (written to disk)
    /** The message of this Commit.*/
    private final String message;

    /** The timestamp of this Commit. Store as long for stability*/
    private final long timestamp;

    /** The commitId of parent commit, null for initial*/
    private final String parentId;

    /** The second parent commitId, null unless merge. */
    private final String secondParentId;

    /** The commitId associated with the current commit. */
    private String commitId;

    /** File Name --> blobId (sorted) */
    private TreeMap<String, String> fileIndex;

    // Runtime-only pointers (NOT written to disk)
    private transient Commit parent;

    // Optional runtime pointer.
    private transient Commit secondParent;


    /***
     * Creates a commit object constructor with the specific parameters.
     * @param message: log message.
     * @param parentId: parent reference, transiently created
     */
    public Commit(String message,
                  String parentId,
                  Commit parent,
                  String secondParentId,
                  TreeMap<String, String> snapshot) {
        this.message = message;
        this.parentId = parentId;
        this.secondParentId = secondParentId;
        this.timestamp = (parentId == null) ? 0L : System.currentTimeMillis();
        // defensive copy to lock it down (TreeMap keeps deterministic order)
        this.fileIndex = new TreeMap<>(snapshot);
    }

    public TreeMap<String, String> getFileIndex() {
        return this.fileIndex;
    }

    /**
     * Utility function to remove file key from fileIndex Tree Map
     * @param fileKey: file key to be removed, assume exists.
     */
    public void updateFileIndex(String fileKey) {
        if (!this.fileIndex.containsKey(fileKey)) {
            return;
        }
        this.fileIndex.remove(fileKey);
    }

    /**
     * Build fileIndex included for this commit from snapshot +/- staged files
     */
    public void buildFileIndex() {
        // start from parent snapshot
        if (parentId == null) {
            fileIndex.clear();
        } else {
            fileIndex = new TreeMap<>(Commit.fromObject(parentId).getFileIndex());
        }

        // Check staged additions & removals
        List<String> adds = plainFilenamesIn(Repository.STAGED_ADD_FOLDER);
        List<String> removal = plainFilenamesIn(Repository.STAGED_RM_FOLDER);

        // Failure case: If no files have been staged, abort.
        if (adds == null & removal == null) {
            throw error("No changes added to the commit.");
        }
        // Check if there's any files staged for additions
        if (adds != null && !adds.isEmpty()) {
            for (String fileName : adds) {
                File file = join(Repository.STAGED_ADD_FOLDER, fileName);
                byte[] bytes = readContents(file);
                String blobId = sha1((Object) bytes);
                Commit.saveFileBlob(blobId, bytes);
                fileIndex.put(fileName, blobId);
                Repository.deleteIfExists(file);
            }
        }
        // Check if there's any files staged for removal
        if (removal != null && !removal.isEmpty()) {
            removal.forEach(fileIndex::remove);
            for (String fileName : removal) {
                File file = join(Repository.STAGED_RM_FOLDER, fileName);
                Repository.deleteIfExists(file);
            }
        }
    }

    /** Lazy-load parent by ID (cache in transient field)*/
    public Commit getParent() {
        if (this.parent == null && this.parentId != null) {
            parent = Commit.fromObject(parentId);
        }
        return this.parent;
    }

    /** Get parent commit id*/
    public List<String> getParentId() {
        if (parentId == null && secondParentId == null) {
            return Collections.emptyList();
        } else if (secondParentId == null) {
            return Collections.singletonList(parentId);
        } else if (parentId == null) {
            return Collections.singletonList(secondParentId);
        } else {
            return Arrays.asList(parentId, secondParentId);
        }
    }

    /**
     * Save file as raw contents in bytes
     * @param blobId: File object path
     * @param bytes: write raw contents
     */
    public static void saveFileBlob(String blobId, byte[] bytes) {
        File outFile = blobsPath(blobId);
        outFile.getParentFile().mkdirs();
        writeContents(outFile, (Object) bytes);
    }

    /**
     * Reading as file bytes
     * @param blobId: file objects path
     * @return File Content String
     */
    public static String readFileBlob(String blobId) {
        String result;
        try {
            File file = blobsPath(blobId);
            result = readContentsAsString(file);
        } catch (GitletException e) {
            result = "";
        }
        return result;
    }

    /**
     * Utility function to save the current file version to current working directory.
     * @param fileName: name of the file
     * @param contents: saved contents of the file in String
     */
    public static void saveFileContents(String fileName, String contents, File path) {
        File file = Utils.join(path, fileName);
        writeContents(file, contents);
    }

    /**
     * Reads in and deserializes a commit from a file
     *
     * @param commitId CommitId: name of the file for the commit to load
     * @return Commit from file
     */
    public static Commit fromObject(String commitId) {
        File file = commitsPath(commitId);
        try {
            return readObject(file, Commit.class);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Serialize the Commit object to objects/ in a file that is the same as its commitId.
     * If already exists, no changes needed. Commit node is immutable.
     * @return String: The commitId being saved to objects/ path.
     */
    public String saveCommit() {
        commitId = getCommitId();
        File outFile = commitsPath(commitId);
        // Once a commit node has been created, can only add new things, not anything existing.
        if (outFile.exists()) {
            throw error("A Gitlet version-control system already exists in the current directory.");
        }
        outFile.getParentFile().mkdirs();
        writeObject(outFile, this);
        return commitId;
    }

    /**
     * Public method that retrieves the commit message private attribute.
     * @return message in this commit
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Public method that retrieves the commit timestamp private attribute.
     * @return timestamp created in this commit
     */
    public String getDateTime() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(f);
    }

    /**
     * Feed parts directly to Utils.sha1(List<Object></>)
     * @return sha1 hash ID of the commit, containing metadata and files/blob included
     */
    private String computeCommitId() {
        StringBuilder sb = new StringBuilder();
        sb.append("commit ").append('\0');
        sb.append("message:").append(message).append('\n');
        sb.append("time:").append(getTime()).append('\n');
        if (parentId != null) {
            sb.append("parent:").append(parentId).append('\n');
        }
        if (secondParentId != null) {
            sb.append("secondParent:").append(secondParentId).append('\n');
        }
        fileIndex.forEach((name, blob) ->
                sb.append(name).append('\0').append(blob).append('\n')
        );
        return sha1(sb.toString());
    }

    private String getCommitId() {
        if (this.commitId == null) {
            this.commitId = computeCommitId();
        }
        return this.commitId;
    }

    private String getTime() {
        return Long.toString(this.timestamp);
    }

}
