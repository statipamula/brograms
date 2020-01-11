package gitlet;

import java.io.File;
import java.io.Serializable;

/** The "blob" class of Gitlet, the tiny stupid version-control system.
 * Basically just the file.
 *  @author Santosh Tatipamula
 */

public class Blob implements Serializable {

    /** Constructor which takes in string NAME. */
    public Blob(String name) {
        _f = new File(name);
        _name = name;
        _cont = Utils.readContents(_f);
        _content = Utils.readContentsAsString(_f);
        _hashCode = hash();
    }

    /** Hashcode function that returns a string. */
    public String hash() {
        return Utils.sha1(_name, _content, _cont);
    }

    /** Accessor for STRING name.
     * @return string*/
    public String getname() {
        return _name;
    }

    /** Accessor for content.
     * @return String*/
    public String getcontent() {
        return _content;
    }

    /** Accessor for hashcode.
     * @return String */
    public String gethashcode() {
        return _hashCode;
    }

    /** Accessor for files.
     * @return File */
    public File getfile() {
        return _f;
    }

    /** String variable for name. */
    private String _name;

    /** String of content. */
    private String _content;

    /** String of hashcode. */
    private String _hashCode;

    /** File off blob. */
    private File _f;

    /** Content byte array. */
    private byte[] _cont;
}
