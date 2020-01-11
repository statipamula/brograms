package enigma;

import java.util.ArrayList;
import java.util.Collection;

/** Class that represents a complete enigma machine.
 *  @author Santosh Tatipamula
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        assert numRotors > 1 : "Not enough rotor slots";
        assert (pawls >= 0 && pawls < numRotors) : "Invalid number of pawls";
        _alphabet = alpha;
        _numberRotors = numRotors;
        _pawls = pawls;
        _allRotors = new ArrayList<>(allRotors);
        _rotors = new ArrayList<>();
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numberRotors;
    }

    /** Return the arraylist of rotors in the machine. */
    ArrayList<Rotor> rotors() {
        return _rotors;
    }

    /** Clears rotors from machine. */
    void clearrotors() {
        _rotors.clear();
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (Rotor allRotor : _allRotors) {
                String x = allRotor.name();
                if (rotors[i].equals(x)) {
                    _rotors.add(allRotor);
                }
            }
        }
        if (_rotors.size() != rotors.length) {
            throw new EnigmaException("Misnamed rotors");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw new EnigmaException("Initial settings wrong length");
        }
        for (char s: setting.toCharArray()) {
            if (!_alphabet.contains(s)) {
                throw new EnigmaException("Initial settings not in alphabet");
            }
        }
        for (int i = 1; i < _rotors.size(); i++) {
            _rotors.get(i).set(setting.charAt(i - 1));
        }

    }


        /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. advancing the last rotor, then double stepping when
     *  necessary before running permutation **/

    int convert(int c) {
        _rotors.get(_rotors.size() - 1).setturnabletrue();
        for (int i = _rotors.size() - 2; i > 0; i--) {
            if (_rotors.get(i + 1).atNotch()
                    && _rotors.get(i).rotates()) {
                _rotors.get(i + 1).setturnabletrue();
                _rotors.get(i).setturnabletrue();
            }
        }
        for (Rotor rotor : _rotors) {
            if (rotor.turnable()) {
                rotor.advance();
            }
        }
        for (int i = 0; i < _rotors.size() - 1; i++) {
            _rotors.get(i).setturnablefalse();
        }
        int inputForward;
        if (_plugboard == null) {
            inputForward = c;
        } else {
            inputForward = _plugboard.permute(c);
        }
        int outputForward = 0;
        for (int i = _rotors.size() - 1; i >= 0; i--) {
            outputForward = _rotors.get(i).convertForward(inputForward);
            inputForward = outputForward;
        }
        int inputBackward = outputForward;
        int outputBackward = 0;
        for (int i = 1; i < _rotors.size(); i++) {
            outputBackward = _rotors.get(i).convertBackward(inputBackward);
            inputBackward = outputBackward;
        }
        if (_plugboard == null) {
            return outputBackward;
        } else {
            return _plugboard.invert(outputBackward);
        }
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {

        String message = msg.replaceAll(" ", "");
        char[] messagearray = message.toCharArray();
        char[] result = new char[messagearray.length];
        for (int i = 0; i < messagearray.length; i++) {
            int output = convert(_alphabet.toInt(messagearray[i]));
            result[i] = _alphabet.toChar(output);
        }
        return new String(result);
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** specific number of rotors in a single enigma machine. */
    private int _numberRotors;

    /** returns number of rotating rotors in a machine. */
    int nummovingrotors() {
        int count = 0;
        for (Rotor r: _rotors) {
            if (r.rotates()) {
                count += 1;
            }
        }
        return count;
    }

    /** specific number of rotors in a single enigma machine. */
    private int _pawls;
    /** arrayList containing all rotors. */
    private ArrayList<Rotor> _allRotors;
    /** Permutation containing plugboard. */
    private Permutation _plugboard;
    /** arraylist containing active rotors. */
    private ArrayList<Rotor> _rotors;
}
