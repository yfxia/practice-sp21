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
    /** serialVersionUID is a long that the JVM writes alongside each serialized object. */
    private static final long serialVersionUID = 1L;

    /** Folder that commits located at*/
    static final File OBJECT_FOLDER = join(Repository.GITLET_DIR, "objects");

    static File blobPath(String id) {
        return join(OBJECT_FOLDER, id.substring(0, 2), id.substring(2));
    }

    // Persisted fields (written to disk)
    /** The message of this Commit.*/
    private final String message;

    /** The timestamp of this Commit. Store as long for stability*/
    private final long timestamp;

    /** The commitId of parent commit, null for initial*/
    private final String parentId;

    /** The second parent commitId, null unless merge. */
    private final String secondParentId;

    private String commitId;

    // Runtime-only pointers (NOT written to disk)
    private transient Commit parent;
    // Optional runtime pointer.
    private transient Commit secondParent;

    /** File Name --> blobId (sorted) */
    private TreeMap<String, String> fileIndex;


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

    /**
     * Feed parts directly to Utils.sha1(List<Object></>)
     * @return sha1 hash ID of the commit, containing metadata and files/blob included
     */
    private String computeCommitId() {
        List<Object> parts = new java.util.ArrayList<>();
        parts.add("message:");
        parts.add(message);
        parts.add("\n");
        parts.add("time:");
        parts.add(getTime());
        parts.add("\n");
        if (parentId != null) {
            parts.add("parent:");
            parts.add(parentId);
            parts.add("\n");
        }
        if (secondParentId != null) {
            parts.add("secondParent:");
            parts.add(secondParentId);
            parts.add("\n");
        }
        fileIndex.forEach((fileName, blob) -> {
            parts.add(fileName);
            parts.add("\0");
            parts.add(blob);
            parts.add("\n");
        });
        return sha1(parts);
    }

    public TreeMap<String, String> getFileIndex() {
        return this.fileIndex;
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

        // Check staged additions
        List<String> adds = plainFilenamesIn(Repository.STAGED_ADD_FOLDER);
        List<String> removal = plainFilenamesIn(Repository.STAGED_RM_FOLDER);

        if (adds == null & removal == null) {
            // Failure case 1: If no files have been staged, abort.
            throw error("No changes added to the commit.");
        } else if (adds != null && !adds.isEmpty()) {
            for (String fileName : adds) {
                File file = join(Repository.STAGED_ADD_FOLDER, fileName);
                byte[] bytes = readContents(file);
                String blobId = sha1((Object) bytes);
                Commit.saveFileBlob(blobId, bytes);
                fileIndex.put(fileName, blobId);
                Repository.deleteIfExists(file);
            }
        // Apply staged Removals
        } else if (removal != null && !removal.isEmpty()) {
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

    public String getParentId() {
        return this.parentId;
    }

    /**
     * Save file as raw contents in bytes
     * @param blobId: File object path
     * @param bytes: write raw contents
     */
    public static void saveFileBlob(String blobId, byte[] bytes) {
        File outFile = blobPath(blobId);
        outFile.getParentFile().mkdirs();
        writeContents(outFile, (Object) bytes);
    }

    /**
     * Reading as file bytes
     * @param blobId: file objects path
     * @return File Content String
     */
    public static String readFileBlob(String blobId) {
        File file =  blobPath(blobId);
        return readContentsAsString(file);
    }

    /**
     * Utility function to save the current file version to current working directory.
     * @param fileName: name of the file
     * @param contents: saved contents of the file in String
     */
    public static void saveFileContents(String fileName, String contents) {
        File file = Utils.join(Repository.CWD, fileName);
        writeContents(file, contents);
    }

    /**
     * Reads in and deserializes a commit from a file
     *
     * @param commitId CommitId: name of the file for the commit to load
     * @return Commit from file
     */
    public static Commit fromObject(String commitId) {
        File file = blobPath(commitId);
        return readObject(file, Commit.class);
    }

    /**
     * Serialize the Commit object to objects/ in a file that is the same as its commitId.
     * If already exists, no changes needed. Commit node is immutable.
     * @return String: The commitId being saved to objects/ path.
     */
    public String saveCommit() {
        commitId = getCommitId();
        File outFile = blobPath(commitId);
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
