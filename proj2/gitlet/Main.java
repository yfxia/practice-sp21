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
                    Repository.stageCommit(args[1]);
                    break;
                case "add-remote":
                    validateNumArgs("add-remote", args, 3, 3);
                    Repository.addRemoteCommit(args[1], args[2]);
                    break;
                case "branch":
                    validateNumArgs("branch", args, 2, 2);
                    Repository.createNewBranch(args[1]);
                    break;
                case "commit":
                    validateNumArgs("commit", args, 2, 2);
                    Repository.createCommit(args[1], null);
                    break;
                case "checkout":
                    validateNumArgs("checkout", args, 2, 4);
                    Repository.checkOutCommit(args);
                    break;
                case "find":
                    validateNumArgs("find", args, 2, 2);
                    Repository.findAllCommits(args[1]);
                    break;
                case "fetch":
                    validateNumArgs("fetch", args, 3, 3);
//                    Repository.fetchRemoteBranch(args[1], args[2]);
                    break;
                case "pull":
                    validateNumArgs("pull", args, 3, 3);
//                    Repository.pullRemoteBranch(args[1], args[2]);
                    break;
                case "push":
                    validateNumArgs("push", args, 3, 3);
//                    Repository.pushRemoteCommits(args[1], args[2]);
                    break;
                case "rm":
                    validateNumArgs("rm", args, 2, 2);
                    Repository.unstageFiles(args[1]);
                    break;
                case "rm-branch":
                    validateNumArgs("rm-branch", args, 2, 2);
                    Repository.removeBranch(args[1]);
                    break;
                case "rm-remote":
                    validateNumArgs("rm-remote", args, 2, 2);
//                    Repository.removeRemoteBranch(args[1]);
                    break;
                case "log":
                    validateNumArgs("log", args, 1, 1);
                    Repository.checkCommitLog();
                    break;
                case "merge":
                    validateNumArgs("merge", args, 2, 2);
                    Repository.mergeBranch(args[1]);
                    break;
                case "global-log":
                    validateNumArgs("global-log", args, 1, 1);
                    Repository.checkCommitGlobalLog();
                    break;
                case "reset":
                    validateNumArgs("reset", args, 2, 2);
                    Repository.resetCommitHistory(args[1]);
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
