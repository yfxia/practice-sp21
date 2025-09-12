package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  It contains folder structures that persist commit metadata and various commands
 *  supported by gitlet.
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
     *     - objects -- file-system hashtable
     *     - index -- maps associated with the commits
     */
    public static void setupPersistence() {
        STAGED_ADD_FOLDER.mkdirs();
        STAGED_RM_FOLDER.mkdirs();
        INDEX_FOLDER.mkdirs();
        createNewFile(HEAD);
        REFS.mkdirs();
    }

    /**
     * Supporting `gitlet init` command.
     * Creates a new Gitlet version-control system that starts with one empty commit.
     * Initialize two pointers: One branch - master; HEAD - branch currently checked out to.
     */
    public Repository() {
        // Initialize a brand-new commit
        Commit initCommitInstance =
                new Commit("initial commit",null, null, null, new TreeMap<>());
        // Includes all metadata and references when hashing a commit
        String commitId = initCommitInstance.saveCommit();

        setHeadReference(MASTER);
        setBranchReference(MASTER, commitId);
        initCommitInstance.buildFileIndex(null);
    }

    /**
     * Supporting `gitlet add` command.
     * Adds a copy of the file as it currently exits to the staging area.
     * Staging an already-staged file overwrite the previous entry.
     * In the worst case, should run in linear time relative to the size of the file
     * being added and lgN, for N the number of files in the commit.
     * @param fileName: The name of the file to be added for commit
     */
    public static void stageCommit(String fileName) {
        // Check if the file exists in the Current Working Directory
        File file = Utils.join(CWD, fileName);
        if (!file.exists()) throw Utils.error("File does not exist.");

        // create a blob: saved contents of the file.
        byte[] bytes = readContents(file);
        // Check if the file is staged for addition already
        File stagedFile = Utils.join(STAGED_ADD_FOLDER, fileName);
        File stagedRmFile = Utils.join(STAGED_RM_FOLDER, fileName);

        if (checkIdenticalFileExists(fileName)){
            deleteIfExists(stagedRmFile);
        } else {
            writeContents(stagedFile, (Object) bytes);
        }
    }

    private static Boolean checkIdenticalFileExists(String fileName) {
        File file = Utils.join(CWD, fileName);
        // create a blob: saved contents of the file.
        byte[] bytes = readContents(file);
        String blobId = sha1((Object) bytes);

        // Check if the file is staged for addition already
        File stagedFile = Utils.join(STAGED_ADD_FOLDER, fileName);

        // Check if the file is tracked by current commit
        String commitId = getHeadCommitId();
        Commit commit = Commit.fromObject(commitId);
        if (commit.getFileIndex().containsKey(fileName) ) {
            String commitBlobId = commit.getFileIndex().get(fileName);
            return commitBlobId.equals(blobId);
        } else if (stagedFile.exists()) {
            byte[] stagedBytes = readContents(stagedFile);
            String stagedBlob = sha1((Object) stagedBytes);
            return blobId.equals(stagedBlob);
        }
        return false;
    }

    /**
     * Supporting `gitlet commit` command.
     * Create a new commit
     * @param message: message contains in this commit
     */
    public static void createCommit(String message) {
        // Failure case: user input message cannot be null
        if (message == null || message.isEmpty()) {
            throw error("Please enter a commit message.");
        }
        List<String> names = plainFilenamesIn(STAGED_ADD_FOLDER);
        List<String> namesRm = plainFilenamesIn(STAGED_RM_FOLDER);

        if ((names == null || names.isEmpty())  && (namesRm == null || namesRm.isEmpty())) {
            throw error("No changes added to the commit.");
        }
        // Get metadata info from parent commit: commitId, parent commit instance
        String branch = getBranchHead();
        String parentCommitId = getHeadCommitId();
        Commit parentCommit = Commit.fromObject(parentCommitId);

        // Create a new commit instance with metadata and file index map
        Commit newCommitInstance =
                new Commit(message, parentCommitId, parentCommit, null, parentCommit.getFileIndex());

        newCommitInstance.buildFileIndex(parentCommitId);
        String commitId = newCommitInstance.saveCommit();
        setBranchReference(branch, commitId);
    }

    /**
     * Utility function to get the current branch at the HEAD pointer.
     * @return The name of the branch at the HEAD pointer.
     */
    private static String getBranchHead() {
        String headRef = getHeadReference();
        return  headRef.substring(headRef.lastIndexOf("/") + 1);
    }

    /***
     * Utility function to get the commitId sits at the HEAD pointer
     * @return The commitId at the HEAD pointer
     */
    private static String getHeadCommitId() {
        String branch = getBranchHead();
        return getBranchReference(branch);
    }

    /**
     * Supporting `gitlet rm [file name]
     * Unstage the file if it is currently staged for addition. If it is tracked in the current commit,
     * stage it for removal and remove the file from working directory if user has not done so.
     * @param fileName: user input file name to be removed/unstaged.
     */
    public static void removeCommit(String fileName) {
        // Get the head commit to check is file is currently being tracked
        String commitId = getHeadCommitId();
        Commit commit = Commit.fromObject(commitId);
        File file = join(STAGED_ADD_FOLDER, fileName);
        /* Failure case: check if the file is neither staged nor tracked by the head commit*/
        if (!(file.exists()) && commit.getFileIndex().get(fileName) == null) {
            throw error("No reason to remove the file.");
            // Unstage the file check
        } else if(file.exists()){
            deleteIfExists(file);
            // If file is tracked in current commit, stage it for removal and remove it from CWD
        } else {
            File stagedRmFile = join(STAGED_RM_FOLDER, fileName);
            createNewFile(stagedRmFile);
            restrictedDelete(fileName);
        }
    }

    /**
     * Support `gitlet checkout` command.
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
            String parentCommitId = getHeadCommitId();
            Commit parentCommit = Commit.fromObject(parentCommitId);

            // Failure case: File should exist in the Current Working Directory.
            File file = Commit.blobPath(parentCommitId);
            if (!file.exists()) throw Utils.error("File does not exist in that commit.");
            // Can this be cached?
            String blob = parentCommit.getFileIndex().get(fileName);
            Commit.saveFileContents(fileName, Commit.readFileBlob(blob));

        // Usage 2: checkout [commit id] -- [file name], takes the commit version and puts it in CWD.
        } else if (args[2].equals("--")) {
            String commitId = args[1];
            String fileName = args[3];
            File file = Commit.blobPath(commitId);
            if (!file.exists()) throw Utils.error("No commit with that id exists.");
            /* Takes the version of the file and puts it in CWD, with overwriting access. */
            Commit commit = Commit.fromObject(commitId);
            Commit.saveFileContents(fileName, Commit.readFileBlob(commit.getFileIndex().get(fileName)));
        }
    }

    /**
     * Supporting command gitlet log
     * Display information about each commit backwards until the initial commit.
     * Display commitId, time of commit, commit message.
     */
    public static void checkCommitLog() {
        String commitId = getHeadCommitId();
        while (commitId != null) {
            Commit commit = Commit.fromObject(commitId);
            message("===");
            message("commit %s", commitId);
            message("Date: %s", commit.getDateTime());
            message("%s", commit.getMessage());
            message("");
            commitId = commit.getParentId();
        }
    }

    /**
     * Supporting command gitlet status
     * Display what branch currently exist, and mark the current branch with *.
     * Also displays what files have been staged for addition or removal.
     */
    public static void checkCommitStatus() {
        message("=== Branches ===");
        List<String> branchList = plainFilenamesIn(REFS);
        assert branchList != null;
        String currentBranch = getBranchHead();
        for (String branch : branchList) {
            message(branch.equals(currentBranch) ? "*%s" : "%s", branch);
        }
        message("");

        message("=== Staged Files ===");
        List<String> stagedFileList = plainFilenamesIn(STAGED_ADD_FOLDER);
        if (stagedFileList != null && !stagedFileList.isEmpty()) {
            for (String fileName : stagedFileList) {
                message("%s", fileName);
            }
        }
        message("");

        message("=== Removed Files ===");
        List<String> removedFileList = plainFilenamesIn(STAGED_RM_FOLDER);
        if (removedFileList != null && !removedFileList.isEmpty()) {
            for (String fileName : removedFileList) {
                message("%s", fileName);
            }
        }
        message("");

        message("=== Modifications Not Staged For Commit ===");
        message("");
        message("=== Untracked Files ===");
        message("");
    }

    public static <T extends Serializable> T fromFile(String fileName,  Class<T> expectedClass) {
        File file = join(INDEX_FOLDER, fileName);
        return readObject(file, expectedClass);
    }

    /**
     * Set the branch pointer for this commit. Will create/overwrite previous commit identifier.
     * @param branch: name of the branch.
     * @param commitId: commit that lives in this branch.
     */
    public static void setBranchReference(String branch, String commitId) {
        File branchFile = Utils.join(REFS, branch);
        createNewFile(branchFile);
        writeContents(branchFile, commitId);
    }

    /**
     * Get the commitId that lives on top of the branch.
     * @param branch: name of the branch.
     * @return String: commitId.
     */
    public static String getBranchReference(String branch) {
        File branchFile = Utils.join(REFS, branch);
        return readContentsAsString(branchFile);
    }

    /**
     * Set HEAD pointer for this commit, at the given branch.
     * It till put the branch path at .gitlet/refs/heads/branchName file
     * @param branch: name of the branch
     */
    public static void setHeadReference(String branch){
        File headFile = Utils.join(HEAD);
        String headFileContent = "refs/heads/" + branch;
        writeContents(headFile, headFileContent);
    }

    /**
     * Get the current HEAD pointer, there's only 1 HEAD pointer for the Repository.
     * @return String: path of the HEAD reference (indicating the commit at which branch)
     */
    public static String getHeadReference() {
        File headFile = Utils.join(HEAD);
        return readContentsAsString(headFile);
    }

    public static void deleteIfExists(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            throw error("Could not delete file " + file.toPath(), e);
        }
    }

    private static void createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw error("Could not create file " + file.toPath(), e);
        }
    }

}