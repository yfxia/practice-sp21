package gitlet;

// TODO: any imports you need here
import java.io.File;

import java.io.IOException;
import java.sql.Timestamp;
// TODO: You'll likely use this in this class


/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Sophia Xia
 */
public class Commit {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;

    /**
     * The timestamp of this Commit.
     */
    private Timestamp timestamp;

    /**
     * The branch of this Commit.
     */
    private String branch;

    /**
     * The unique identifier of this Commit.
     */
    private String commitId;

    /*
    * The head pointer keeps track of the linked list we currently are.
    */
    private String head;

    public static final String DEFAULT_BRANCH = "master";

    static final File COMMIT_FOLDER = Utils.join(Repository.GITLET_DIR, "commits");

    /**
     * Creates a new Gitlet version-control system in current directory
     * that starts with one empty commit.
     * Initialize two pointers: One branch - master; HEAD - branch currently checked out to.
     * @return a Commit object
     * @throws IOException
     */
    public static Commit makeAnInitCommit() throws IOException {
        Commit commit = new Commit();
        // Includes all metadata and references when hashing a commit? Timestamp??
        commit.commitId = Utils.sha1("");
        File initCommitFile = Utils.join(COMMIT_FOLDER, commit.commitId);
        if (initCommitFile.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        } else {
            initCommitFile.createNewFile();
            commit.timestamp = new Timestamp(0);
            commit.branch = DEFAULT_BRANCH;
            commit.message = "initial commit";
        }
        return commit;
    }

    /**
     * Adds a copy of the file as it currently exits to the staging area.
     * Staging an already-staged file overwrite the previous entry.
     * @param fileName: The name of the file to be added for commit
     */
    public static void addACommit(String fileName) throws IOException {
        // Check if the file exists in the Current Working Directory
        File file = Utils.join(Repository.CWD, fileName);
        if (!file.exists()) throw Utils.error("File does not exist.");
        // create a blob: saved contents of the file.
        String fileContent = Utils.readContentsAsString(file);
        // Check if the file is staged for addition already
        File stagedFile = Utils.join(Repository.GITLET_DIR, "staged_add", fileName);
        if (stagedFile.exists()) {
            String stagedFileContent = Utils.readContentsAsString(stagedFile);
            // If CWD version of the file is identical to the one in current commit, remove it.
            if (fileContent.equals(stagedFileContent)) {
                // leverage git rm
                Commit.removeACommit(fileName);
            }
        // Otherwise, stage this file for addition and overwrites previous entry if any.
        } else {
            Utils.writeContents(stagedFile, fileContent);
        }
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * @param fileName
     */
    public static void removeACommit(String fileName) {
        // How to track a file on the head commit?
    }

    /**
     * Displays what branches currently exist, and marks the current branch with *.
     * Also displays what files have been staged for addition or removal.
     */
    public static void checkCommitStatus() {
        // branch has a pointer
    }
}
