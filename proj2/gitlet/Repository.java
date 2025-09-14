package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
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
                new Commit("initial commit", null, null, null, new TreeMap<>());
        // Includes all metadata and references when hashing a commit
        String commitId = initCommitInstance.saveCommit();

        setHeadReference(MASTER);
        setBranchReference(MASTER, commitId);
        initCommitInstance.buildFileIndex();
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
        if (!file.exists()) {
            throw Utils.error("File does not exist.");
        }
        // create a blob: saved contents of the file.
        byte[] bytes = readContents(file);
        // Check if the file is staged for addition already
        File stagedFile = Utils.join(STAGED_ADD_FOLDER, fileName);
        File stagedRmFile = Utils.join(STAGED_RM_FOLDER, fileName);

        if (checkIdenticalFileExists(fileName)) {
            deleteIfExists(stagedRmFile);
        } else {
            writeContents(stagedFile, (Object) bytes);
        }
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
        String commitId = getHeadCommitId();
        Commit commit = Commit.fromObject(commitId);

        // Create a new commit instance with metadata and file index map
        Commit newCommitInstance =
                new Commit(message, commitId, commit, null, commit.getFileIndex());

        newCommitInstance.buildFileIndex();
        String newCommitId = newCommitInstance.saveCommit();
        setBranchReference(branch, newCommitId);
    }

    /**
     * Supporting `gitlet rm [file name]
     * Unstage the file if it is currently staged for addition. If tracked in the current commit,
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
        } else if (file.exists()) {
            deleteIfExists(file);
            // If file is tracked in current commit, stage it for removal and remove it from CWD
        } else {
            File stagedRmFile = join(STAGED_RM_FOLDER, fileName);
            createNewFile(stagedRmFile);
            restrictedDelete(fileName);
        }
    }

    /**
     * Supporting `gitlet branch [branch name]` command.
     * Creates a new branch with the given name, and points it at the current head commit.
     * It does NOT immediately switch to the newly created branch, before HEAD should be at "master".
     * @param branchName: user-input the name of branch to be created.
     */
    public static void createNewBranch(String branchName) {
        File file = join(REFS, branchName);
        if (file.exists()) {
            throw error("A branch with that name already exists.");
        }
        String commitId = getHeadCommitId();
        // Set the given branch pointer to the current head commit.
        setBranchReference(branchName, commitId);
    }

    /**
     * Support `gitlet checkout` command.
     * Checking out to an existing snapshot of the Repository.
     * @param args: User-input list of String arguments
     */
    public static void checkOutCommit(String[] args) {
        String firstArg = args[1];
        // Usage 3: checkout [branch name], take all files at the head of the given branch.
        if (args.length == 2) {
            File file = join(REFS, firstArg);
            if (!file.exists()) {
                throw Utils.error("No such branch exists.");
            }
            String head = getBranchHead();
            if (head.equals(file.getName())) {
                message("No need to checkout the current branch.");
            }
            // Set the HEAD pointer to the branch.
            setHeadReference(firstArg);
            restoreCommitStatus(getHeadCommitId());
        // Usage 1: checkout -- [file name], takes the version of the file and puts it in CWD.
        } else if (firstArg.equals("--")) {
            String fileName = args[2];
            String parentCommitId = getHeadCommitId();
            Commit parentCommit = Commit.fromObject(parentCommitId);

            // Failure case: File should exist in the Current Working Directory.
            File file = Commit.blobPath(parentCommitId);
            if (!file.exists()) {
                throw Utils.error("File does not exist in that commit.");
            }
            // Can this be cached?
            String blob = parentCommit.getFileIndex().get(fileName);
            Commit.saveFileContents(fileName, Commit.readFileBlob(blob));

        // Usage 2: checkout [commit id] -- [file name], puts it in CWD.
        } else if (args[2].equals("--")) {
            String commitId = args[1];
            String fileName = args[3];
            File file = Commit.blobPath(commitId);
            if (!file.exists()) {
                throw Utils.error("No commit with that id exists.");
            }
            /* Takes the version of the file and puts it in CWD, with overwriting access. */
            Commit commit = Commit.fromObject(commitId);
            Commit.saveFileContents(fileName,
                    Commit.readFileBlob(commit.getFileIndex().get(fileName)));
        }
    }

    /**
     * Utility function to travel back in time to restore Repository file system exactly
     * at the time the given commitId was created.
     * @param commitId: user-input commitId to be restored to.
     */
    private static void restoreCommitStatus(String commitId) {
        Commit commit = Commit.fromObject(commitId);
        Set<String> trackedFiles = commit.getFileIndex().keySet();
        List<String> currentFiles = plainFilenamesIn(CWD);
        // Remove files not being tracked
        if (currentFiles != null && !currentFiles.isEmpty()) {
            for (String fileName : currentFiles) {
                // Delete this from CWD if not being tracked
                if (!trackedFiles.contains(fileName)) {
                    File file = join(CWD, fileName);
                    deleteIfExists(file);
                }
            }
        }
        // Create files being tracked
        for (String name : trackedFiles) {
            String blobId = commit.getFileIndex().get(name);
            Commit.saveFileContents(name, Commit.readFileBlob(blobId));
        }
    }

    /**
     * Supporting command gitlet log
     * Display information about each commit backwards until the initial commit.
     * Display commitId, time of commit, commit message.
     */
    public static void checkCommitLog() {
        String commitId = getHeadCommitId();
        displayCommitLog(commitId);
    }

    /**
     * Supporting command gitlet global-log
     * Display information about all commits ever made. Order does not matter.
     */
    public static void checkCommitGlobalLog() {
        List<String> branchList = plainFilenamesIn(REFS);
        assert branchList != null;
        for (String branch : branchList) {
            String commitId = getBranchReference(branch);
            displayCommitLog(commitId);
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
     * Set HEAD pointer for this commit, at the given branch.
     * It till put the branch path at .gitlet/refs/heads/branchName file
     * @param branch: name of the branch
     */
    public static void setHeadReference(String branch) {
        File headFile = Utils.join(HEAD);
        writeContents(headFile, "refs/heads/", branch);
    }

    /**
     * Get the current HEAD pointer, there's only 1 HEAD pointer for the Repository.
     * @return String: path of the HEAD reference (indicating the commit at which branch)
     */
    public static String getHeadReference() {
        File headFile = Utils.join(HEAD);
        return readContentsAsString(headFile);
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
     * Get the commitId that lives on top of the branch.
     * @param branch: name of the branch.
     * @return String: commitId.
     */
    public static String getBranchReference(String branch) {
        File branchFile = Utils.join(REFS, branch);
        return readContentsAsString(branchFile);
    }

    /**
     * Utility function to delete an existing file.
     * @param file: File object that contains path information.
     */
    public static void deleteIfExists(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            throw error("Could not delete file " + file.toPath(), e);
        }
    }

    /**
     * Check if identical file exists in staging area to be added.
     * @param fileName: name of the file
     * @return: Boolean True exists / False not exists.
     */
    private static Boolean checkIdenticalFileExists(String fileName) {
        File file = Utils.join(CWD, fileName);
        // create a blob: saved contents of the file.
        byte[] bytes = readContents(file);
        String blobId = sha1((Object) bytes);
        File stagedFile = Utils.join(STAGED_ADD_FOLDER, fileName);

        String commitId = getHeadCommitId();
        Commit commit = Commit.fromObject(commitId);
        // Case 1: Check if the file is tracked by current commit
        if (commit.getFileIndex().containsKey(fileName)) {
            String commitBlobId = commit.getFileIndex().get(fileName);
            return commitBlobId.equals(blobId);
        // Case 2: Check if the file is staged for addition already
        } else if (stagedFile.exists()) {
            byte[] stagedBytes = readContents(stagedFile);
            String stagedBlob = sha1((Object) stagedBytes);
            return blobId.equals(stagedBlob);
        }
        // Case 3: neither above, return false.
        return false;
    }



    /**
     * Utility function that takes in a commitId and display its information.
     * @param commitId: commitId ever exists.
     */
    private static void displayCommitLog(String commitId) {
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
     * Utility function that creates a new file.
     * @param file: File object contains path information.
     */
    private static void createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw error("Could not create file " + file.toPath(), e);
        }
    }

}
