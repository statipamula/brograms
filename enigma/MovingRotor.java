package enigma;

import static enigma.EnigmaException.*;
import java.util.Arrays;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Santosh Tatipamula
 */
class MovingRotor extends Rotor {

    /**
     * A rotor named NAME whose permutation in its default setting is
     * PERM, and whose notches are at the positions indicated in NOTCHES.
     * The Rotor is initally in its 0 setting (first character of its
     * alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        String[] tempnotches = notches.split("");
        _notches = Arrays.copyOfRange(tempnotches, 0, tempnotches.length);
    }

    /**
     * Advance rotor setting by 1 using Rotor's advance method.
     */
    void advance() {
        this.set(permutation().wrap(this.setting() + 1));
    }

    @Override
    boolean atNotch() {
        boolean notch = false;
        for (String s: _notches) {
            if (this.setting() == this.alphabet().toInt(s.charAt(0))) {
                notch = true;
            }
        }
        return notch;
    }

    /** Positions of notches. */
    private String[] _notches;

    @Override
    boolean rotates() {
        return true;
    }
}


