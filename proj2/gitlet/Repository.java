package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();

    private static final String LINE_SEPARATOR = System.lineSeparator();

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
    public static void createCommit(String message, String secondParentId) {
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
                new Commit(message, commitId, commit, secondParentId, commit.getFileIndex());

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
     * It does NOT immediately switch to the new branch, before should be at "master".
     * @param branchName: user input the name of branch to be created.
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
     * Supporting `gitlet rm-branch [branch name]
     * Deletes the branch with the given name. This only means to delete the pointer
     * associated with the branch; it does not mean to delete all commits that were
     * created under the branch, or anything like that.
     * @param branchName: user input the name of branch to be deleted.
     */
    public static void removeBranch(String branchName) {
        File file = join(REFS, branchName);
        if (!file.exists()) {
            throw error("A branch with that name does not exist.");
        }
        String currentBranch = getBranchHead();
        if (currentBranch.equals(branchName)) {
            throw error("Cannot remove the current branch.");
        }
        // Delete the pointer, i.e. refs/heads/branchName path
        deleteIfExists(file);
    }

    /**
     * Support `gitlet checkout` command.
     * Checking out to an existing snapshot of the Repository.
     * @param args: User-input list of String arguments
     */
    public static void checkOutCommit(String... args) {
        String firstArg = args[1];
        // Usage 3: checkout [branch name], take all files at the head of the branch.
        if (args.length == 2) {
            File file = join(REFS, firstArg);
            if (!file.exists()) {
                throw Utils.error("No such branch exists.");
            }
            // Check if that branch is the current branch.
            String head = getBranchHead();
            if (head.equals(file.getName())) {
                message("No need to checkout the current branch.");
            }
            // Takes all files in the commit at the given branch, and puts them in the CWD.
            String commitId = getBranchReference(firstArg);
            restoreCommitStatus(getHeadCommitId(), commitId);
            // Set the HEAD pointer to the branch.
            setHeadReference(firstArg);
        // Usage 1: checkout -- [file name], takes file version & puts it in CWD.
        } else if (firstArg.equals("--")) {
            String fileName = args[2];
            String commitId = getHeadCommitId();
            Commit commit = Commit.fromObject(commitId);
            // Failure case: File should exist in the previous commit.
            Set<String> trackedFiles = commit.getFileIndex().keySet();
            if (!trackedFiles.contains(fileName)) {
                throw Utils.error("File does not exist in that commit.");
            }
            String blob = commit.getFileIndex().get(fileName);
            Commit.saveFileContents(fileName, Commit.readFileBlob(blob), CWD);
        // Usage 2: checkout [commit id] -- [file name], puts it in CWD.
        } else if (args[2].equals("--")) {
            String fileName = args[3];
            List<String> commitIds = findCommitIdsByPrefix(args[1], false);
            if (commitIds.isEmpty()) {
                throw error("No commit with that id exists.");
            }
            String commitId = commitIds.get(0);
            File file = join(CWD, fileName);
            // Failure case: File should exist in the CWD.
            if (!file.exists()) {
                throw Utils.error("File does not exist in that commit.");
            }
            Commit commit = Commit.fromObject(commitId);
            checkOutFileFromCommit(fileName, commit.getFileIndex().get(fileName));
        } else {
            throw error("Incorrect operands.");
        }
    }

    /**
     * Support command `gitlet reset [commit id]`.
     * Checks out all the files tracked by the given commit. Staging area is cleared.
     * Removes tracked files not present. Also moves the head to that commit node.
     * Essentially `checkout` of an arbitrary commit that also changes current branch head.
     */
    public static void resetCommitHistory(String commitId) {
        Commit commit = Commit.fromObject(commitId);
        // Failure case 1: no commit with the given id exists.
        if (commit == null) {
            throw error("No commit with that id exists.");
        }
        restoreCommitStatus(getHeadCommitId(), commitId);
        // Moves the current branch's head to that commit node
        String branch = getBranchHead();
        setBranchReference(branch, commitId);
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
     * Supporting command `gitlet global-log`.
     * Display information about all commits ever made. Order does not matter.
     */
    public static void checkCommitGlobalLog() {
        List<String> objectIds = findCommitIdsByPrefix("", true);
        for (String objectId : objectIds) {
            displayCommitLog(objectId);
        }
    }

    /**
     * Supporting command `gitlet find [commit message]`.
     * Prints out the ids of all commits that have the given commit message.
     * If there are multiple such commits, it prints the ids out on separate lines.
     * @param commitMessage: user input single operand.
     */
    public static void findAllCommits(String commitMessage) {
        boolean foundCommit = false;
        List<String> objectIds = findCommitIdsByPrefix("", true);
        for (String objectId : objectIds) {
            Commit commit = Commit.fromObject(objectId);
            if (commit != null) {
                String msg = commit.getMessage();
                if (msg.equals(commitMessage)) {
                    message(objectId);
                    foundCommit = true;
                }
            }
        }
        if (!foundCommit) {
            throw error("Found no commit with that message.");
        }
    }

    /**
     * Supporting command `gitlet status`.
     * Display what branch currently exist, and mark the current branch with *.
     * Also displays what files have been staged for addition or removal.
     */
    public static void checkCommitStatus() {
        checkInitRepoStatus();
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
     * Supporting command `gitlet merge [branch name]`.
     * Merge files from the given branch into the current branch.
     * @param branch: user-input branch name to be merged together.
     */
    public static void mergeBranch(String branch) {
        if (!checkStagingAreaEmpty()) {
            throw error("You have uncommitted changes.");
        }
        String givenHead = getBranchReference(branch);
        if (givenHead == null) {
            throw error("A branch with that name does not exist.");
        }
        String currentHead = getHeadCommitId();
        if (givenHead.equals(currentHead)) {
            throw error("Cannot merge a branch with itself.");
        }
        TreeMap<String, String> currFiles = Commit.fromObject(currentHead).getFileIndex();
        TreeMap<String, String> givenFiles = Commit.fromObject(givenHead).getFileIndex();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (cwdFiles != null && !cwdFiles.isEmpty()) {
            List<String> untrackedSet = plainFilenamesIn(CWD).stream().
                    filter(ele -> !currFiles.containsKey(ele)).collect(Collectors.toList());
            if (!untrackedSet.isEmpty()) {
                throw error("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
        String lca = lowestCommonAncestor(currentHead, givenHead);
        if (lca.equals(givenHead)) {
            message("Given branch is an ancestor of the current branch.");
            return;
        } else if (lca.equals(currentHead)) {
            checkOutCommit("checkout", branch);
            message("Current branch fast-forwarded.");
            return;
        }
        TreeMap<String, String> splitPointFiles = Commit.fromObject(lca).getFileIndex();
        Set<String> notASet = Stream.concat(givenFiles.keySet().stream(), currFiles.keySet().stream()).
                filter(k -> !splitPointFiles.containsKey(k)).collect(Collectors.toSet());
        for (String fileName : splitPointFiles.keySet()) {
            String aVersion = splitPointFiles.get(fileName);
            String bVersion = currFiles.get(fileName);
            String cVersion = givenFiles.get(fileName);
            // Case 7: A = C && not B
            if ((bVersion == null && aVersion.equals(cVersion))) {
                deleteIfExists(join(CWD, fileName));  // remain absent
            // Case 6: A = B && not C
            } else if (cVersion == null && aVersion.equals(bVersion)) {
                deleteIfExists(join(CWD, fileName));
                Commit.fromObject(givenHead).updateFileIndex(fileName); // untracked
                Commit.fromObject(currentHead).updateFileIndex(fileName);
            // Case 1: A = B != C
            } else if (aVersion.equals(bVersion) && !aVersion.equals(cVersion)) {
                checkOutFileFromCommit(fileName, cVersion);
                Commit.saveFileContents(fileName, Commit.readFileBlob(cVersion),
                        STAGED_RM_FOLDER);
            // Case 2: A = C != B
            } else if (aVersion.equals(cVersion) && !aVersion.equals(bVersion)) {
                continue; // stay as they are
            // Case 3: A != B = C
            } else if (aVersion != bVersion && aVersion.equals(cVersion)) {
                continue; // left unchanged
            // case 0: only A, not B and not C
            } else if (bVersion == null && cVersion == null) {
                deleteIfExists(join(CWD, fileName));
            // Case 8: A != B != C --- merge conflict
            } else {
                message("Encountered a merge conflict.");
                String mergedContent = "<<<<<<< HEAD"
                        + LINE_SEPARATOR
                        + Commit.readFileBlob(bVersion)
                        + "======="
                        + LINE_SEPARATOR
                        + Commit.readFileBlob(cVersion)
                        + ">>>>>>>";
                Commit.saveFileContents(fileName, mergedContent, STAGED_ADD_FOLDER);
                Commit.saveFileContents(fileName, mergedContent, CWD);
            }
        }
        for (String name : notASet) {
            String b = currFiles.get(name);
            String c = givenFiles.get(name);
            // Case 4: !A && B && !C
            if (b != null && c == null) {
                continue; // remain as they are
            // Case 5: !A && !B && C
            } else if (b == null && c != null) {
                checkOutFileFromCommit(name, c);
                Commit.saveFileContents(name, Commit.readFileBlob(c), STAGED_ADD_FOLDER);
            }
        }
        String message = String.format("Merged %s into %s.", branch, getBranchHead());
        createCommit(message, givenHead);
    }


    /**
     * Utility function to use Breadth-First-Search algorithm to find the latest
     * common ancestor of two commit ids, i.e. the split point of the commits.
     * @param commitIdA: commitId of current branch.
     * @param commitIdB: commitId of the given branch.
     * @return: the latest common ancestor of two commits where split happens.
     */
    private static String lowestCommonAncestor(String commitIdA, String commitIdB) {
        // Returns a single LCA. Null only if graph is disconnected.
        if (commitIdA == null || commitIdB == null) {
            return null;
        } else if (commitIdA.equals(commitIdB)) {
            return commitIdA;
        }
        // 1) Distances from node A upward (BFS)
        Map<String, Integer> distA = distToCommitId(commitIdA);
        if (distA.containsKey(commitIdB)) {
            return commitIdB; // node 2 is ancestor of node 1
        }
        // 2) BFS from B upward, keep best candidate
        Map<String, Integer> distB = new HashMap<>();
        distB.put(commitIdB, 0);
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(commitIdB);
        String lca = null;
        Integer minDist = Integer.MAX_VALUE;
        while (!queue.isEmpty()) {
            String x = queue.removeLast();
            Integer db =  distB.get(x);
            if (db > minDist) {
                break;
            }
            Integer da =  distA.get(x);
            if (da != null) {
                int dist = Math.max(db, da);
                if (dist < minDist) {
                    minDist = dist;
                    lca = x;
                    if (dist == db) { // early return for max-score metric
                        return lca;
                    }
                }
            }
            for (String parent: Commit.fromObject(x).getParentId()) {
                if (distB.putIfAbsent(parent, db + 1) == null) {
                    queue.add(parent);
                }
            }
        }
        return lca;
    }

    /**
     * Utility function to build a distance hashmap: key-commitId, value-distance to node.
     * @param id: anchor commitId from which its distance to others are calculated.
     * @return: A hashmap that shows the distance from the anchor commitId to other nodes.
     */
    private static Map<String, Integer> distToCommitId(String id) {
        Map<String, Integer> distMap = new HashMap<>();
        distMap.put(id, 0);
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(id);
        while (!queue.isEmpty()) {
            String x = queue.removeLast();
            Integer dx = distMap.get(x);
            for (String parent: Commit.fromObject(x).getParentId()) {
                if (distMap.putIfAbsent(parent, dx + 1) == null) {
                    queue.addFirst(parent);  // if this node not seen before, traverse it.
                }
            }
        }
        return distMap;
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
     * It will put the branch path at .gitlet/refs/heads/branchName file
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
        String commitId;
        try {
            File branchFile = Utils.join(REFS, branch);
            commitId = readContentsAsString(branchFile);
        } catch (GitletException e) {
            commitId = null;
        }
        return commitId;
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
     * Utility function to iteratively delete all files in staging area.
     * @param stagingFolder: folder path for staging area: Add or Remove.
     */
    public static void clearStagingArea(File stagingFolder) {
        List<String> files = plainFilenamesIn(stagingFolder);
        if (files != null && !files.isEmpty()) {
            for (String fileName: files) {
                File file = join(stagingFolder, fileName);
                deleteIfExists(file);
            }
        }
    }

    /**
     * Utility function to travel back in time to restore Repository file system exactly
     * at the time the given commitId was created.
     * @param currentCommitId: current commitId.
     * @param checkedCommitId: checked-out commitId.
     */
    private static void restoreCommitStatus(String currentCommitId, String checkedCommitId) {
        Commit currentCommit = Commit.fromObject(currentCommitId);
        Commit checkedCommit = Commit.fromObject(checkedCommitId);
        Set<String> currentTrackedFiles = currentCommit.getFileIndex().keySet();
        Set<String> checkedTrackedFiles = checkedCommit.getFileIndex().keySet();
        List<String> currentFiles = plainFilenamesIn(CWD);
        // Failure case: If a working file is untracked in the current branch and
        // would be overwritten by the checkout, exit.
        if (currentFiles != null && !currentFiles.isEmpty()) {
            for (String fileName : currentFiles) {
                if (!currentTrackedFiles.contains(fileName)
                        && checkedTrackedFiles.contains(fileName)) {
                    throw error("There is an untracked file in the way;"
                            + "delete it, or add and commit it first.");
                }
            }
        }
        // Remove any files that are tracked in the current branch
        // but are not present in the checked-out branch.
        for (String fileName : currentTrackedFiles) {
            if (!checkedTrackedFiles.contains(fileName)) {
                File file = join(CWD, fileName);
                restrictedDelete(file);
            }
        }
        // Create files being tracked
        for (String name : checkedTrackedFiles) {
            String blobId = checkedCommit.getFileIndex().get(name);
            Commit.saveFileContents(name, Commit.readFileBlob(blobId), CWD);
        }
        // Staging Add/Remove area is cleared.
        clearStagingArea(STAGED_ADD_FOLDER);
        clearStagingArea(STAGED_RM_FOLDER);
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
     * Utility function to search user-input (shortened) commitId by its prefix.
     * @param prefix: user-input commitId argument
     * @param wildcard: indicator variable to show if doing search all files (wild card)
     *                or only search for a specific prefix
     * @return: full commitId if exists, otherwise the original commit id input.
     */
    private static List<String> findCommitIdsByPrefix(String prefix, Boolean wildcard) {
        File[] commitList = join(Commit.OBJECT_FOLDER, "commits").listFiles();
        List<String> objectIdList = new ArrayList<>();
        assert commitList != null;
        for (File object : commitList) {
            String folderName = object.getName();
            if (wildcard || folderName.startsWith(prefix.substring(0, 2))) {
                List<String> objectIds = plainFilenamesIn(object);
                if (objectIds != null) {
                    for (String objectId : objectIds) {
                        if (wildcard || (folderName + objectId).startsWith(prefix)) {
                            objectIdList.add(folderName + objectId);
                        }
                    }
                }
            }
        }
        return objectIdList;
    }

    /**
     * Utility function that takes in a commitId and display its information.
     * @param commitId: commitId ever exists.
     */
    private static void displayCommitLog(String commitId) {
        while (commitId != null) {
            Commit commit = Commit.fromObject(commitId);
            if (commit != null) {
                message("===");
                message("commit %s", commitId);
                message("Date: %s", commit.getDateTime());
                message("%s", commit.getMessage());
                message("");
                List<String> parentIds = commit.getParentId();
                commitId = (parentIds.isEmpty()) ? null : parentIds.get(0);
            } else {
                return;
            }
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

    /**
     * Utility function to check if Gitlet working directory is initialized.
     */
    private static void checkInitRepoStatus() {
        if (!GITLET_DIR.exists()) {
            throw error("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * Utility function takes the version of the file and puts it in CWD, with overwriting access.
     * @param fileName: tracked file name inside the commit.
     * @param blobId: the blobId that file version will be checked.
     */
    private static void checkOutFileFromCommit(String fileName, String blobId) {
        Commit.saveFileContents(fileName,
                Commit.readFileBlob(blobId), CWD);
    }

    /**
     * Utility function to check if staging add/rm area is empty
     * @return: Boolean indicator on emptiness
     */
    private static Boolean checkStagingAreaEmpty() {
        List<String> addFiles = plainFilenamesIn(STAGED_ADD_FOLDER);
        List<String> rmFiles = plainFilenamesIn(STAGED_RM_FOLDER);
        if (addFiles != null && !addFiles.isEmpty()) {
            return false;
        } else if (rmFiles != null && !rmFiles.isEmpty()) {
            return false;
        }
        return true;
    }

}
