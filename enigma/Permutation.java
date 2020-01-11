package enigma;

import java.util.Arrays;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Santosh Tatipamula
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */

    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String holder = cycles.trim();
        holder = holder.replace("(", "");
        holder = holder.replace(")", "");
        _cycles = holder.split(" ");
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    void addCycle(String cycle) {
        if (Arrays.toString(_cycles).equals("[]")) {
            _cycles[0] = cycle;
        } else if (!cycle.trim().equals("")) {
            String[] updatedCycles = new String[_cycles.length + 1];
            System.arraycopy(_cycles, 0, updatedCycles, 0, _cycles.length);
            updatedCycles[_cycles.length] = cycle;
            _cycles = updatedCycles;
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char charPermute = _alphabet.toChar(wrap(p));
        char newChar;
        if (Arrays.toString(_cycles).equals("[]")) {
            return _alphabet.toInt(charPermute);
        } else {
            for (String cycle : _cycles) {
                for (int j = 0; j < cycle.length(); j++) {
                    if (cycle.charAt(j) == charPermute) {
                        newChar = cycle.charAt((j + 1) % cycle.length());
                        return _alphabet.toInt(newChar);
                    }
                }
            }
            return p;
        }
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char charPermute = _alphabet.toChar(wrap(c));
        char newChar;
        if (Arrays.toString(_cycles).equals("[]")) {
            return _alphabet.toInt(charPermute);
        } else {
            for (String cycle : _cycles) {
                for (int j = 0; j < cycle.length(); j++) {
                    if (cycle.charAt(j) == charPermute) {
                        int x = cycle.length();
                        if (j == 0) {
                            int y = cycle.length() - 1;
                            newChar = cycle.charAt(y);
                            return _alphabet.toInt(newChar);
                        } else {
                            int y = (j - 1) % x;
                            newChar = cycle.charAt(y);
                            return _alphabet.toInt(newChar);
                        }
                    }
                }
            }
            return c;
        }
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return _alphabet.toChar(permute(_alphabet.toInt(p)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return _alphabet.toChar(invert(_alphabet.toInt(c)));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return the cycles used to initialize this Permutation. */
    String[] cycles() {
        return _cycles;
    }


    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int cyclesSize = 0;
        for (String cycle : _cycles) {
            cyclesSize += cycle.length();
        }
        return cyclesSize == alphabet().size();
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    /** cycles of this permutation. */
    private String [] _cycles;

}
