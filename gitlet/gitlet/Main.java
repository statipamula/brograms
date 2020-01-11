package gitlet;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Santosh Tatipamula
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (!repoexists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else if (repoexists() && args[0].equals("init")) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        } else if (repoexists() && !allcommands.contains(args[0])) {
            System.out.println("No command with that name exists.");
        } else {
            String[] arguments = Arrays.copyOfRange(args, 1, args.length);
            if (Arrays.asList(commands).contains(args[0])) {
                if (repoexists()) {
                    carti = getgitlet();
                    runcommand(args[0], arguments);
                    File gitlet = new File(".gitlet/gitlets");
                    Utils.writeObject(gitlet, carti);
                } else {
                    if (args[0].equals("init")) {
                        carti = new Gitlet();
                        File gitlet = new File(".gitlet/gitlets");
                        Utils.writeObject(gitlet, carti);
                    }
                }
            }
        }
    }

    /** Runs commands. ARG,  ARGUMENTS*/
    public static void runcommand(String arg, String[] arguments) {
        if (arg.equals("add")) {
            carti.add(arguments[0]);
        } else if (arg.equals("commit")) {
            carti.commit(arguments[0]);
        } else if (arg.equals("log")) {
            carti.log();
        } else if (arg.equals("checkout")) {
            if (arguments.length > 1) {
                carti.checkout(arguments);
            } else {
                carti.checkout(arguments[0]);
            }
        } else if (arg.equals("status")) {
            carti.status();
        } else if (arg.equals("rm")) {
            carti.rm(arguments[0]);
        } else if (arg.equals("find")) {
            carti.find(arguments[0]);
        } else if (arg.equals("global-log")) {
            carti.globallog();
        } else if (arg.equals("branch")) {
            carti.branch(arguments[0]);
        } else if (arg.equals("rm-branch")) {
            carti.rmbranch(arguments[0]);
        } else if (arg.equals("reset")) {
            carti.reset(arguments[0]);
        } else if (arg.equals("merge")) {
            carti.merge(arguments[0]);
        }
    }

    /** If repo exists.
     * @return boolean*/
    private static boolean repoexists() {
        File gitlet = new File(".gitlet");
        return gitlet.exists();
    }

    /** Loads gitlet.
     * @return gitlet*/
    private static Gitlet getgitlet() {
        File gitlet = new File(".gitlet/gitlets");
        return Utils.readObject(gitlet, Gitlet.class);
    }

    /** The Gitlet.*/
    private static Gitlet carti;

    /** The valid commands for Gitlet.*/
    private static String[] commands = new String[]{"init", "add",
        "commit", "rm", "log",
        "global-log", "find", "status", "checkout",
        "branch", "rm-branch", "reset", "merge"};

    /** The valid commands for Gitlet as a list.*/
    private static List<String> allcommands = Arrays.asList(commands);



}
