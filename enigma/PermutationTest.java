package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;


import static org.junit.Assert.*;

import static enigma.TestUtils.*;


/** The suite of all JUnit tests for the Permutation class.
 *  @author Santosh Tatipamula
 */
public class PermutationTest {

    /**
     * Testing time limit.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /**
     * Check that perm has an alphabet whose size is that of
     * FROMALPHA and TOALPHA and that maps each character of
     * FROMALPHA to the corresponding character of FROMALPHA, and
     * vice-versa. TESTID is used in error messages.
     */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void checkCycles() {
        perm = new Permutation("(ABCD) "
                + "(EFG) (HIJK)", new Alphabet(UPPER_STRING));
        assertArrayEquals(new String[]{"ABCD", "EFG", "HIJK"}, perm.cycles());
        Permutation perm1 = new Permutation("()", new Alphabet(UPPER_STRING));
        assertArrayEquals((new String[]{""}), perm1.cycles());
    }

    @Test
    public void checkAddCycle() {
        perm = new Permutation("(ABCD) (EFG) "
                + "(HIJK)", new Alphabet(UPPER_STRING));
        perm.addCycle("LMNOP");
        assertArrayEquals(new String[]{"ABCD", "EFG", "HIJK", "LMNOP"},
                perm.cycles());

        Permutation perm1 = new Permutation("()", new Alphabet(UPPER_STRING));
        perm1.addCycle("LMNOP");
        assertArrayEquals(new String[]{"LMNOP"}, perm1.cycles());

        Permutation perm2 = new Permutation("(ABCD) "
                + "(EFG) (HIJK)", new Alphabet(UPPER_STRING));
        perm2.addCycle("");
        assertArrayEquals(new String[]{"ABCD", "EFG", "HIJK"}, perm2.cycles());

    }

    @Test
    public void testInvertChar() {
        Permutation p = new Permutation("(PNH) (ABDFIKLZYXW) (JC)",
                new Alphabet());
        assertEquals(p.invert('B'), 'A');
        assertEquals(p.invert('G'), 'G');
        assertEquals(p.invert('A'), 'W');
        assertEquals(p.invert('J'), 'C');
    }


    @Test
    public void testPermuteChar() {
        Permutation p = new Permutation("(PNH) (ABDFIKLZYXW) "
                + "(JC)", new Alphabet());
        assertEquals(p.permute('P'), 'N');
        assertEquals(p.permute('H'), 'P');
        assertEquals(p.permute('J'), 'C');
        assertEquals(p.permute('F'), 'I');
    }


    @Test
    public void testDerangement() {
        Permutation p = new Permutation("(PNH) (ABDFIKLZYXW) (JC)",
                new Alphabet());
        assertFalse(p.derangement());
        Permutation p1 = new Permutation("(ABCD) (EFGHIJK) "
                + "(LMNOP) (QRSTUV) (WXYZ)", new Alphabet());
        assertTrue(p1.derangement());
        Permutation p2 = new Permutation("()", new Alphabet());
        assertFalse(p2.derangement());


    }
}

