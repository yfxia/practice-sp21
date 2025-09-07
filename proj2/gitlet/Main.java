package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Sophia Xia
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            throw Utils.error("Must have at least one argument");
        }

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
//                Commit.removeCommit(rmFileName);
            case "status":
                validateNumArgs("status", args, 1, 1);
                break;
            default:
                Utils.message(String.format("Unknown command: %s", firstArg));
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
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}