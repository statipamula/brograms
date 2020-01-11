package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;


import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Santosh Tatipamula
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** @param x Return a Scanner reading from the file named x. */
    private Scanner getInput(String x) {
        try {
            return new Scanner(new File(x));
        } catch (IOException excp) {
            throw error("could not open %s", x);
        }
    }

    /** @param y Return a PrintStream writing to the file named y. */
    private PrintStream getOutput(String y) {
        try {
            return new PrintStream(new File(y));
        } catch (IOException excp) {
            throw error("could not open %s", y);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String nextSetting = _input.nextLine();
        if (!nextSetting.contains("*")) {
            throw new EnigmaException("Incorrect setting format!");
        }
        if (nextSetting.split("\\w+").length < enigma.numRotors() + 1) {
            throw new EnigmaException("Not enough arguments in setting.");
        }
        setUp(enigma, nextSetting.substring(1));
        if (!enigma.rotors().get(0).reflecting()) {
            throw new EnigmaException("First rotor is not a reflector");
        }
        for (Rotor r: enigma.rotors()) {
            if (Collections.frequency(enigma.rotors(), r) > 1) {
                throw new EnigmaException("Duplicate rotors in setting.");
            }
        }
        if (enigma.nummovingrotors() > enigma.numPawls()) {
            throw new EnigmaException("Wrong # of arguments.");
        }
        while (_input.hasNextLine()) {
            String nextLine = _input.nextLine();
            if (nextLine.trim().equals(""))  {
                _output.println();
            } else if (nextLine.contains("*")) {
                enigma.clearrotors();
                setUp(enigma, nextLine.substring(1));
                if (!enigma.rotors().get(0).reflecting()) {
                    throw new EnigmaException("First rotor is not a reflector");
                }
                if (enigma.nummovingrotors() > enigma.numPawls()) {
                    throw new EnigmaException("Wrong # of arguments.");
                }
                if (_input.hasNextLine()) {
                    String in = _input.nextLine();
                    String[] out = in.split("\\W+");
                    String result = "";
                    for (String s : out) {
                        result = result.concat(enigma.convert(s));
                    }
                    printMessageLine(result);
                }
            } else {
                String[] out = nextLine.split("\\W+");
                String result = "";
                for (String s : out) {
                    result = result.concat(enigma.convert(s));
                }
                printMessageLine(result);
            }
        }
    }



    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String next = _config.next();
            _alphabet = new Alphabet(next);
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Wrong # of Rotors");
            }
            int numberRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Wrong # of Pawls");
            }
            int activeRotors = _config.nextInt();
            holder = _config.next();
            while (_config.hasNext()) {
                name = holder;
                notches = _config.next();
                _rotors.add(readRotor());
            }
            return new Machine(_alphabet, numberRotors, activeRotors, _rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            permutation = "";
            holder = _config.next();
            while (holder.contains("(") && _config.hasNext()) {
                permutation = permutation.concat(holder + " ");
                holder = _config.next();
            }
            if (!_config.hasNext()) {
                permutation = permutation.concat(holder);
            }
            if (notches.charAt(0) == 'M') {
                return new MovingRotor(name, new Permutation(permutation,
                        _alphabet), notches.substring(1));
            } else if (notches.charAt(0) == 'N') {
                return new FixedRotor(name, new
                        Permutation(permutation, _alphabet));
            } else {
                return new Reflector(name, new
                        Permutation(permutation, _alphabet));
            }

        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Mod helper takes in parameters A and B and returns integer X. */
    int modhelper(int a, int b) {
        int x = a % b;
        if (x < 0) {
            x += b;
        }
        return x;
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] x = settings.split(" ");
        List<String> y = new ArrayList<String>(Arrays.asList(x));
        y.removeAll(Arrays.asList("", null));
        String[] z = y.toArray(new String[0]);
        String[] insertable = new String[M.numRotors()];
        System.arraycopy(z, 0, insertable, 0, M.numRotors());
        M.insertRotors(insertable);
        M.setRotors(z[M.numRotors()]);
        for (int i = 1; i <  M.rotors().size() - 1; i++) {
            M.rotors().get(i).setturnablefalse();
        }
        if (z.length > M.numRotors() + 1
                && !z[M.numRotors() + 1].matches("[a-zA-Z]{4}")) {
            String[] perms = Arrays.copyOfRange(z, M.numRotors() + 1, z.length);
            String perm = Arrays.toString(perms);
            perm = perm.substring(1, perm.length() - 1).replace(",", "");
            M.setPlugboard(new Permutation(perm, _alphabet));
        }
        if (z.length > M.numRotors() + 1
                && z[M.numRotors() + 1].matches("[a-zA-Z]{4}")) {
            String[] perms = Arrays.copyOfRange(z, M.numRotors() + 2, z.length);
            String perm = Arrays.toString(perms);
            perm = perm.substring(1, perm.length() - 1).replace(",", "");
            M.setPlugboard(new Permutation(perm, _alphabet));

            String ringsettings = z[M.numRotors() + 1];
            for (int i = 1; i < M.rotors().size(); i++) {
                int lastset = M.rotors().get(i).setting();
                int ringset = _alphabet.toInt(ringsettings.charAt(i - 1));
                int newset = modhelper(lastset - ringset, _alphabet.size());
                M.rotors().get(i).set(newset);
            }

        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            int length = msg.length() - i;
            if (length <= 5) {
                _output.println(msg.substring(i));
            } else {
                _output.print(msg.substring(i, i + 5) + " ");
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** File for encoded/decoded messages. */
    private ArrayList<Rotor> _rotors = new ArrayList<>();

    /** String containing name of rotor. */
    private String name;

    /** String containing notches. */
    private String notches;
    /** Holder string. */
    private String holder;
    /** Permutation string. */
    private String permutation;
}
