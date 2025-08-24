package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Sophia Xia
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw Utils.error("Must have at least one argument");
        }

        Repository.setupPersistence();
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                Commit.makeAnInitCommit();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                String addFileName = args[1];
                Commit.addACommit(addFileName);
                break;
            case "rm":
                validateNumArgs("rm", args, 2);
                String rmFileName = args[1];
                Commit.removeACommit(rmFileName);
            case "status":
                validateNumArgs("status", args, 1);
                break;
            default:
                throw Utils.error(String.format("Unknown command: %s", firstArg));
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
