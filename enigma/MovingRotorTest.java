package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Santosh Tatipamula
 */
public class MovingRotorTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Rotor rotor;
    private String alpha = UPPER_STRING;

    /** Check that rotor has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkRotor(String testId,
                            String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, rotor.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d (%c)", ci, c),
                         ei, rotor.convertForward(ci));
            assertEquals(msg(testId, "wrong inverse of %d (%c)", ei, e),
                         ci, rotor.convertBackward(ei));
        }
    }

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private void setRotor(String name, HashMap<String, String> x,
                          String notches) {
        rotor = new MovingRotor(name, new Permutation(x.get(name), UPPER),
                                notches);
    }

    /* ***** TESTS ***** */

    @Test
    public void checkRotorAtA() {
        setRotor("I", NAVALA, "");
        checkRotor("Rotor I (A)", UPPER_STRING, NAVALA_MAP.get("I"));
    }

    @Test
    public void checkRotorAdvance() {
        setRotor("I", NAVALA, "");
        rotor.advance();
        checkRotor("Rotor I advanced", UPPER_STRING, NAVALB_MAP.get("I"));
    }

    @Test
    public void checkRotorSet() {
        setRotor("I", NAVALA, "");
        rotor.set(25);
        checkRotor("Rotor I set", UPPER_STRING, NAVALZ_MAP.get("I"));
    }
    private Machine machine;
    private ArrayList<Rotor> rotors = ALL_ROTORS;

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private void setMachine(Alphabet alph, int numrotors,
                            int pawls, Collection<Rotor> allrotors) {
        machine = new Machine(alph, numrotors, pawls, allrotors);
    }

    @Test
    public void testAtNotch() {
        setMachine(UPPER, 5, 3, rotors);
        machine.insertRotors(new String[]{"B", "BETA", "I", "II", "III"});
        machine.setRotors("AQEV");
        assertTrue(machine.rotors().get(3).atNotch());
    }

    @Test
    public void testConvert() {
        setMachine(UPPER, 5, 3, rotors);
        machine.insertRotors(new String[]{"B", "BETA", "I", "II", "III"});
        machine.setRotors("AAAA");
        String input = "HELLO WORLD";
        String output = machine.convert(input);
        assertEquals("ILBDAAMTAZ", output);


        setMachine(UPPER, 5, 3, rotors);
        machine.insertRotors(new String[]{"B", "BETA", "I", "II", "III"});
        machine.setRotors("AAAA");
        String input1 = "ILBDA AMTAZ";
        String output1 = machine.convert(input1);
        assertEquals("HELLOWORLD", output1);

        setMachine(UPPER, 5, 3, rotors);
        machine.insertRotors(new String[]{"B", "BETA", "I", "II", "III"});
        machine.setRotors("AAAA");
        machine.setPlugboard(new Permutation("(AQ) (EP)", UPPER));
        assertEquals("IHBDQQMTQZ", machine.convert("HELLO WORLD"));

        setMachine(UPPER, 5, 3, rotors);
        machine.insertRotors(new String[]{"B", "BETA", "I", "II", "III"});
        machine.setRotors("AAAA");
        machine.setPlugboard(new Permutation("(AQ) (EP)", UPPER));
        assertEquals("HELLOWORLD", machine.convert("IHBDQQMTQZ"));

        setMachine(UPPER, 5, 3, rotors);
        machine.insertRotors(new String[]{"B", "BETA", "I", "II", "III"});
        machine.setRotors("AAEV");
        machine.setPlugboard(new Permutation("(TD) (KC) (JZ", UPPER));
        String x = "ALL THESE MERGE CONFLICTS";

    }

}

