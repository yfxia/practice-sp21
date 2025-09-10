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
            Repository.setupPersistence();
            String firstArg = args[0];
            switch(firstArg) {
                case "init":
                    validateNumArgs("init", args, 1, 1);
                    new Repository();
                    break;
                case "add":
                    validateNumArgs("add", args, 2, 2);
                    String addFileName = args[1];
                    Repository.stageCommit(addFileName);
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
                case "rm":
                    validateNumArgs("rm", args, 2, 2);
                    String rmFileName = args[1];
                    Repository.removeCommit(rmFileName);
                case "log":
                    validateNumArgs("status", args, 1, 1);
                    Repository.checkCommitLog();
                    break;
                case "status":
                    validateNumArgs("status", args, 1, 1);
                    Repository.checkCommitStatus();
                    break;
                default:
                    Utils.message(String.format("Unknown command: %s", firstArg));
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