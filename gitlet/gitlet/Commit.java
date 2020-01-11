package gitlet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

/** The "commit" class of Gitlet, the tiny stupid version-control system.
 *  @author Santosh Tatipamula
 */

public class Commit implements Serializable {

    /** A commit is initialized with a message (string),
     * a file mapping (hashmap)
     * an array of parent(s) (string array), and
     * a boolean that indicates if the commit
     * is the first commit in the gitlet repository.
     * MESSAGE MAPPING FIRST PARENTS*/

    public Commit(String message, HashMap<String,
            String> mapping, boolean first, String[] parents) {
        _message = message;
        _mapping = mapping;
        _first = first;
        _parents = parents;
        if (first) {
            _timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            Date d = new Date();
            SimpleDateFormat s =
                    new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
            _timestamp = s.format(d) + " -0800";
        }
    }

    /** Accessor for message.
     * @return string*/
    public String getmessage() {
        return _message;
    }

    /** Accessor for file mapping.
     * @return hashmap*/
    public HashMap<String, String> getmapping() {
        return _mapping;
    }

    /** Accessor for parent(s) array.
     * @return array*/
    public String[] getparents() {
        return _parents;
    }

    /** Accessor for timestamp.
     * @return string*/
    public String gettimestamp() {
        return _timestamp;
    }

    /** Hashcode.
     * @return string*/
    public String hash() {
        String parents = Arrays.toString(_parents);
        String mapping;
        if (_mapping == null) {
            mapping = "";
        } else {
            mapping = _mapping.toString();
        }
        return Utils.sha1(_message, parents, _timestamp, mapping);
    }

    /** A commit's message.*/
    private String _message;

    /** A commit's file mapping, names of blobs to sha IDs of blobs.*/
    private HashMap<String, String> _mapping;

    /** Whether a commit is the first commit in a repo.*/
    private boolean _first;

    /** The array containing the commit's parent(s).*/
    private String[] _parents;

    /** The timestamp of a commit.*/
    private String _timestamp;

}

