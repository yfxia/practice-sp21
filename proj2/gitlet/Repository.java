package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Sophia Xia
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File STAGED_ADD_FOLDER = join(GITLET_DIR, "staged_add");

    public static final File STAGED_RM_FOLDER = join(GITLET_DIR, "staged_rm");

    private static final File INDEX_FOLDER = join(GITLET_DIR, "index");

    private static final File HEAD = join(GITLET_DIR, "HEAD");

    private static final File REFS = join(GITLET_DIR, "refs", "heads");

    private static final String MASTER = "master";


    /**
     * Set up Gitlet Persistence
     * .gitlet/ -- top level folder for all persistent data
     *     - staged_add -- staging area for files to be added
     *     - staged_rm -- staging area for files to be removed
     *     - objects -- file-system hashtable
     *     - index -- maps associated with the commits
     */
    public static void setupPersistence() {
        STAGED_ADD_FOLDER.mkdirs();
        STAGED_RM_FOLDER.mkdirs();
        INDEX_FOLDER.mkdirs();
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        REFS.mkdirs();
    }

    /**
     * Creates a new Gitlet version-control system that starts with one empty commit.
     * Initialize two pointers: One branch - master; HEAD - branch currently checked out to.
     */
    public Repository() {
        // Initialize a brand-new commit
        Commit initCommitInstance = new Commit("initial commit", MASTER,null);
        // Includes all metadata and references when hashing a commit
        String commitId = initCommitInstance.saveCommit();
        setHeadReference(MASTER);
        setBranchReference(MASTER, commitId);
    }

    /**
     * Adds a copy of the file as it currently exits to the staging area.
     * Staging an already-staged file overwrite the previous entry.
     *  In the worst case, should run in linear time relative to the size of the file
     *  being added and lgN, for N the number of files in the commit.
     * @param fileName: The name of the file to be added for commit
     */
    public static void stageCommit(String fileName) {
        // Check if the file exists in the Current Working Directory
        File file = Utils.join(CWD, fileName);
        if (!file.exists()) throw Utils.error("File does not exist.");

        // create a blob: saved contents of the file.
        String fileContent = Utils.readContentsAsString(file);
        String blob = getFileBlob(fileContent);

        // Check if the file is staged for addition already
        File stagedFile = Utils.join(STAGED_ADD_FOLDER, fileName);
        if (stagedFile.exists()) {
            String stagedFileContent = Utils.readContentsAsString(stagedFile);
            String stagedBlob = getFileBlob(stagedFileContent);
            // If CWD version of the file is identical to the one in current commit, remove it.
            if (blob.equals(stagedBlob)) {
                // leverage git rm
//                Commit.removeACommit(fileName);
                System.out.println("Identical File already exists.");
            }
            // Otherwise, stage this file for addition and overwrites previous entry if any.
        } else {
            Utils.writeContents(stagedFile, fileContent);
        }
    }

    /**
     * Create a new commit
     * @param message: message contains in this commit
     */
    public static void createCommit(String message) {
        if (message == null){
            throw Utils.error("Please enter a commit message.");
        }
        String headRef = getHeadReference();
        String currentBranch = headRef.substring(headRef.lastIndexOf("/") + 1);
        String parentCommitId = getBranchReference(currentBranch);
        Commit myCommitInstance = new Commit(message, currentBranch, parentCommitId);
        myCommitInstance.setFileIndex();
        String commitId = myCommitInstance.saveCommit();
        setBranchReference(currentBranch, commitId);
    }

    /**
     * Checking out to an existing snapshot of the Repository.
     * @param args: User-input list of String arguments
     */
    public static void checkOutCommit(String[] args) {
        String firstArg = args[1];
        // Usage 3: checkout [branch name], take all files at the head of the given branch.
        if (args.length == 2){
            File file = Utils.join(REFS, firstArg);
            if (!file.exists()) throw Utils.error("No such branch exists.");
            String headRef = getHeadReference();
            if (headRef.equals(file.getName())) {
                message("No need to checkout the current branch.");
            }
        // Usage 1: checkout -- [file name], takes the version of the file and puts it in CWD.
        } else if (firstArg.equals("--")) {
            String fileName = args[2];
            String headRef = getHeadReference();
            String currentBranch = headRef.substring(headRef.lastIndexOf("/") + 1);
            String parentCommitId = getBranchReference(currentBranch);
            Commit parentCommitInstance = Commit.fromObject(parentCommitId);

            File file = Utils.join(Commit.OBJECT_FOLDER, parentCommitId.substring(0, 2), parentCommitId.substring(2));
            if (!file.exists()) throw Utils.error("File does not exist in that commit.");
            String blob = parentCommitInstance.getFileIndex(fileName);
            saveFileToCWD(fileName, Commit.readFileBlob(blob));

        // Usage 2: checkout [commit id] -- [file name], takes the commit version and puts it in CWD.
        } else if (args[2].equals("--")) {
            String commitId = args[1];
            String fileName = args[3];
            File file = Utils.join(Commit.OBJECT_FOLDER, commitId.substring(0, 2), commitId.substring(2));
            if (!file.exists()) throw Utils.error("No commit with that id exists.");
        }
    }

    public static void saveFileToCWD(String fileName, String contents) {
        File file = Utils.join(CWD, fileName);
        writeContents(file, contents);
    }

    public static void removeCommit(String fileName) {
        File file = Utils.join(Repository.CWD, fileName);
    }

    private static String getFileBlob(Object vals) {
        return Utils.sha1(vals);
    }

    public static <T extends Serializable> T fromFile(String fileName,  Class<T> expectedClass) {
        File file = join(INDEX_FOLDER, fileName);
        return readObject(file, expectedClass);
    }

    public static void setBranchReference(String branch, String commitId) {
        File branchFile = Utils.join(REFS, branch);
        try {
            branchFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeContents(branchFile, commitId);
//        DumpObj.main(branchFile.getAbsolutePath());
    }

    public static String getBranchReference(String branch) {
        File branchFile = Utils.join(REFS, branch);
        return readContentsAsString(branchFile);
    }

    public static void setHeadReference(String branch){
        File headFile = Utils.join(HEAD);
        String headFileContent = "refs/heads/" + branch;
        writeContents(headFile, headFileContent);
//        DumpObj.main(headFile.getAbsolutePath());
    }

    public static String getHeadReference() {
        File headFile = Utils.join(HEAD);
        return readContentsAsString(headFile);
    }

}