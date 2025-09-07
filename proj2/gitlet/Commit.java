package gitlet;

// TODO: any imports you need here
import java.io.File;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

// TODO: You'll likely use this in this class


/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Sophia Xia
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** Folder that commits located at*/
    static final File OBJECT_FOLDER = Utils.join(Repository.GITLET_DIR, "objects");

    /** The message of this Commit.*/
    private String message;

    /** The timestamp of this Commit.*/
    private String timestamp;

    /** The branch of this Commit. */
    private String branch;

    /** The unique identifier of this Commit.*/
    private String commitId;

    /** The parent transient reference of the commit. */
    private transient String parent;

    private TreeMap<String, String> fileIndex = new TreeMap<>();


    /***
     * Creates a commit object constructor with the specific parameters.
     * @param message: log message.
     * @param parent: parent reference, transiently created
     */
    public Commit(String message, String branch, String parent) {
        this.message = message;
        this.branch = branch;
        this.parent = parent;
        if (this.parent == null) {
            this.timestamp = new Timestamp(0).toString();
        } else {
            this.timestamp = new Timestamp(System.currentTimeMillis()).toString();
        }
    }

    public String getFileIndex(String fileName) {
        return this.fileIndex.get(fileName);
    }

    public void setFileIndex() {
        List<String> files = Utils.plainFilenamesIn(Repository.STAGED_ADD_FOLDER);
        if (files == null) {
            // Failure case 1: If no files have been staged, abort.
            throw Utils.error("No changes added to the commit.");
        }
        for (String fileName : files) {
            File file = Utils.join(Repository.STAGED_ADD_FOLDER, fileName);
            String fileContent = Utils.readContentsAsString(file);
            String blob = Utils.sha1(fileContent);
            this.fileIndex.put(fileName, blob);
            Commit.saveFileBlob(blob, fileContent);
            // The staging area is cleared after a commit.
            Utils.restrictedDelete(fileName);
        }
    }

    public static void saveFileBlob(String blobId, String contents) {
        File outFile = Utils.join(OBJECT_FOLDER, blobId.substring(0,2), blobId.substring(2));
        outFile.getParentFile().mkdirs();
        Utils.writeContents(outFile, contents);
    }

    public static String readFileBlob(String blobId) {
        File file =  Utils.join(OBJECT_FOLDER, blobId.substring(0,2), blobId.substring(2));
        return Utils.readContentsAsString(file);
    }

    /**
     * Reads in and deserializes a commit from a file
     *
     * @param commitId CommitId: name of the file for the commit to load
     * @return Commit from file
     */
    public static Commit fromObject(String commitId) {
        File file = Utils.join(OBJECT_FOLDER, commitId.substring(0,2), commitId.substring(2));
        return Utils.readObject(file, Commit.class);
    }

    /**
     * Serialize the Commit object to objects/ in a file that is the same as its commitId.
     * If already exists, no changes needed.
     */
    public String saveCommit(){
        commitId = this.getCommitId();
        File outFile = Utils.join(OBJECT_FOLDER, commitId.substring(0,2), commitId.substring(2));
        if (outFile.exists()){
            throw Utils.error("A Gitlet version-control system already exists in the current directory.");
        }
        outFile.getParentFile().mkdirs();
        Utils.writeObject(outFile, this);
        return commitId;
    }

    /**
     * Public method that retrieves the commit message private attribute.
     * @return message in this commit
     */
    public String getMessage() {
        return this.message;
    }

    public String getBranch() {
        return this.branch;
    }

    /**
     * Public method that retrieves the commit timestamp private attribute.
     * @return timestamp created in this commit
     */
    public String getTimestamp() {
        return this.timestamp;
    }


    public String getParent() {
        return this.parent;
    }

    private String getCommitId() {
        this.commitId = Utils.sha1((Object) Utils.serialize(this));
        return this.commitId;
    }

}