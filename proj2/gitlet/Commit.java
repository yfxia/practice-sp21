package gitlet;

// TODO: any imports you need here
import java.io.File;
import static gitlet.Utils.*;

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

    public static final String DEFAULT_BRANCH = "master";

    static final File COMMIT_FOLDER = Utils.join(Repository.GITLET_DIR, "commits");

    /**
     * Creates an initial commit.
     * */
    public static Commit makeAnInitCommit() throws IOException {
        Commit commit = new Commit();
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
}
