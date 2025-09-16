package gitlet;


import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Sophia Xia
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                message("Please enter a command.");
                System.exit(0);
            }
            String firstArg = args[0];
            switch (firstArg) {
                case "init":
                    validateNumArgs("init", args, 1, 1);
                    Repository.setupPersistence();
                    new Repository();
                    break;
                case "add":
                    validateNumArgs("add", args, 2, 2);
                    String addFileName = args[1];
                    Repository.stageCommit(addFileName);
                    break;
                case "branch":
                    validateNumArgs("branch", args, 2, 2);
                    String branchName = args[1];
                    Repository.createNewBranch(branchName);
                    break;
                case "commit":
                    validateNumArgs("commit", args, 2, 2);
                    String commitFileName = args[1];
                    Repository.createCommit(commitFileName);
                    break;
                case "checkout":
                    validateNumArgs("checkout", args, 2, 4);
                    Repository.checkOutCommit(args);
                    break;
                case "find":
                    validateNumArgs("find", args, 2, 2);
                    String commitMessage = args[1];
                    Repository.findAllCommits(commitMessage);
                    break;
                case "rm":
                    validateNumArgs("rm", args, 2, 2);
                    String rmFileName = args[1];
                    Repository.removeCommit(rmFileName);
                    break;
                case "rm-branch":
                    validateNumArgs("rm-branch", args, 2, 2);
                    String rmBranchName = args[1];
                    Repository.removeBranch(rmBranchName);
                    break;
                case "log":
                    validateNumArgs("log", args, 1, 1);
                    Repository.checkCommitLog();
                    break;
                case "merge":
                    validateNumArgs("merge", args, 2, 2);
                    String mergeBranchName = args[1];
                    Repository.mergeBranch(mergeBranchName);
                    break;
                case "global-log":
                    validateNumArgs("global-log", args, 1, 1);
                    Repository.checkCommitGlobalLog();
                    break;
                case "reset":
                    validateNumArgs("reset", args, 2, 2);
                    String commitId = args[1];
                    Repository.resetCommitHistory(commitId);
                    break;
                case "status":
                    validateNumArgs("status", args, 1, 1);
                    Repository.checkCommitStatus();
                    break;
                default:
                    message("No command with that name exists.");
            }
        } catch (GitletException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param lowerBound/UpperBound Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int lowerBound, int upperBound) {
        if (args.length < lowerBound || args.length > upperBound) {
            throw error(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
