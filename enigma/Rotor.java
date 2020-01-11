package enigma;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Santosh Tatipamula
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
        _turnable = false;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return my turnable. */
    boolean turnable() {
        return _turnable;
    }

    /** make turnable true. */
    void setturnabletrue() {
        _turnable = true;
    }

    /** make turnable false. */
    void setturnablefalse() {
        _turnable = false;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        _setting = _permutation.alphabet().toInt(cposn);
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int input = _permutation.wrap(p + _setting);
        int permutedInput = _permutation.permute(input);
        return _permutation.wrap(permutedInput - _setting);
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int input = _permutation.wrap(e + _setting);
        int invertedInput = _permutation.invert(input);
        return _permutation.wrap(invertedInput - _setting);
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;
    /** Whether a rotor is turnable. */
    private boolean _turnable;


    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;
    /** The setting of this rotor. */
    private int _setting;
}
