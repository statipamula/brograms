package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Santosh Tatipamula
 */
class Alphabet {

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    private char[] chararray;
    /** A string containing of all characters of the alphabet. */
    private String characters;

    /** @param x new alphabet containing STRING.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String x) {
        this.chararray = new char[x.length()];
        this.characters = x;
        for (int i = 0; i < x.length(); i += 1) {
            chararray[i] = x.charAt(i);
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return chararray.length;
    }
    /** Returns the character of the alphabet. */
    String getCharacters() {
        return characters;
    }

    /** Returns the character of the alphabet. */
    char[] getChararray() {
        return chararray;
    }

    /** Returns true if preprocess(CH) is in this alphabet. */
    boolean contains(char ch) {
        return characters.contains(String.valueOf(ch));
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return chararray[index];
    }

    /** Returns the index of character preprocess(CH), which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        int charIndex = 0;
        for (int i = 0; i < chararray.length; i++) {
            if (chararray[i] == ch) {
                charIndex = i;
            }
        }
        return charIndex;
    }

}
