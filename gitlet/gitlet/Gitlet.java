package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


/** The "repo" class of Gitlet, the tiny stupid version-control system.
 *  @author Santosh Tatipamula
 */

public class Gitlet implements Serializable {

    /**
     * The "init" method of Gitlet, the tiny stupid version-control system.
     * Creates a new Gitlet version-control system in the current directory.
     */
    public Gitlet() {
        Commit initial = new Commit("initial commit",
                new HashMap<>(), true, null);
        String hashid = initial.hash();
        File gitlet = new File(".gitlet");
        gitlet.mkdir();
        File staging = new File(".gitlet/staging");
        staging.mkdir();
        File commits = new File(".gitlet/commits");
        commits.mkdir();
        File initialFile = new File(".gitlet/commits/" + hashid);
        Utils.writeObject(initialFile, initial);
        _branches = new HashMap<>();
        _untracked = new ArrayList<String>();
        _branches.put("master", initial.hash());
        _head = "master";
        _stage = new HashMap<>();
    }

    /**
     * The "add" method of Gitlet, the tiny stupid version-control system S.
     * Stages file.
     */
    public void add(String s) {
        File f = new File(s);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob toadd = new Blob(s);
        String contenthash = Utils.sha1(toadd.getcontent());
        Commit c = idtocommit(_branches.get(_head));
        File newFile = new File(".gitlet/staging/" + contenthash);
        HashMap<String, String> currfiles = c.getmapping();
        if (!currfiles.containsKey(s)
                || !currfiles.get(s).equals(contenthash)) {
            String content = Utils.readContentsAsString(toadd.getfile());
            Utils.writeContents(newFile, content);
            _stage.put(s, contenthash);
        } else if (newFile.exists()) {
            _stage.remove(s);
        }
        if (_untracked.contains(s)) {
            _untracked.remove(s);
        }
    }

    /**
     * The "commit" method of Gitlet, the tiny stupid version-control system S.
     * Creates new commit.
     */
    public void commit(String s) {
        if (s.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        } else if (_untracked.size() == 0 && _stage.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit current = idtocommit(_branches.get(_head));
        String par = current.hash();
        HashMap<String, String> curr = current.getmapping();
        for (String fileName : _stage.keySet()) {
            curr.put(fileName, _stage.get(fileName));
        }
        if (_untracked.size() > 0) {
            for (String file : _untracked) {
                curr.remove(file);
            }
        }
        String[] parent = {par};
        Commit newCommit = new Commit(s, curr, false, parent);
        File newFile = new File(".gitlet/commits/" + newCommit.hash());
        Utils.writeObject(newFile, newCommit);
        _untracked.clear();
        _stage.clear();
        _branches.put(_head, newCommit.hash());
    }

    /**
     * The merge "commit" method of Gitlet,
     * the tiny stupid version-control system.
     * S MERGED
     */
    public void mergecommit(String s, String merged) {
        if (s.equals("")) {
            System.out.println("Please enter a commit message.");
        } else {
            Commit current = idtocommit(_branches.get(_head));
            String par = current.hash();
            HashMap<String, String> curr = current.getmapping();
            for (String fileName : _stage.keySet()) {
                curr.put(fileName, _stage.get(fileName));
            }
            if (_untracked.size() > 0) {
                for (String file : _untracked) {
                    curr.remove(file);
                }
            }
            String[] parent = {_branches.get(_head), _branches.get(merged)};
            Commit newCommit = new Commit(s, curr, false, parent);
            File newFile = new File(".gitlet/commits/" + newCommit.hash());
            Utils.writeObject(newFile, newCommit);
            _untracked.clear();
            _stage.clear();
            _branches.put(_head, newCommit.hash());
        }
    }

    /**
     * The "log" method of Gitlet, the tiny stupid version-control system.
     * Prints the log of commit's branch.
     */
    public void log() {
        Commit curr = idtocommit(_branches.get(_head));
        while (curr != null && curr.getparents() != null) {
            logcommit(curr);
            String parent = curr.getparents()[0];
            curr = idtocommit(parent);
        }
        logcommit(curr);
    }

    /**
     * Helper for log COM.
     */
    public void logcommit(Commit com) {
        System.out.println("===");
        System.out.println("commit " + com.hash());
        System.out.println("Date: " + com.gettimestamp());
        System.out.println(com.getmessage());
        System.out.println();
    }

    /**
     * The "checkout" method of Gitlet, the tiny stupid version-control system.
     * For first two cases ARGUMENTS.
     */
    public void checkout(String[] arguments) {
        Commit curr = null;
        String name = null;
        if (arguments[0].equals("--") && arguments.length == 2) {
            name = arguments[1];
            curr = idtocommit(_branches.get(_head));
        } else if (arguments[1].equals("--") && arguments.length == 3) {
            boolean b = false;
            String id = shortidtolong(arguments[0]);
            File commits = new File(".gitlet/commits/");
            for (File f : commits.listFiles()) {
                if (shortidtolong(arguments[0]).equals(f.getName())) {
                    b = true;
                }
            }
            if (b) {
                curr = idtocommit(shortidtolong(arguments[0]));
                name = arguments[2];
            } else {
                System.out.println("No commit with that id exists.");
                return;
            }
        } else {
            System.out.println("Incorrect operands");
            return;
        }
        assert name != null;
        File f = new File(name);
        if (!f.exists()) {
            System.out.println("File does not exist in that commit.");
        } else {
            assert curr != null;
            HashMap<String, String> currfiles = curr.getmapping();
            if (currfiles.containsKey(name)) {
                File old = new File(".gitlet/staging/" + currfiles.get(name));
                File newfile = new File(name);
                Utils.writeContents(newfile,
                        Utils.readContentsAsString(old));
            }
        }
    }

    /**
     * The "checkout" method of Gitlet, the tiny stupid version-control system.
     * For branch case STR.
     */
    public void checkout(String str) {
        if (_head.equals(str)) {
            System.out.println("No need to checkout the current branch");
        } else if (_untracked.size() > 0) {
            System.out.println("There is an untracked "
                    + "file in the way; delete it or add it first.");
        } else if (!_branches.containsKey(str)) {
            System.out.println("No such branch exists.");
        } else if (_branches.containsKey(str)) {
            boolean n = untracked();
            if (n) {
                return;
            }
            Commit checkedout = idtocommit(_branches.get(str));
            assert checkedout != null;
            HashMap<String, String> checkedoutfiles = checkedout.getmapping();
            String wd = System.getProperty("user.dir");
            File wdfiles = new File(wd);
            for (File f : wdfiles.listFiles()) {
                String name = f.getName();
                if (!name.equals(".gitlet")) {
                    if (!checkedoutfiles.containsKey(f.getName())) {
                        Utils.restrictedDelete(f);
                    }
                }
            }
            if (checkedoutfiles != null) {
                for (Object s : checkedoutfiles.keySet()) {
                    File oldfile = new File(".gitlet/staging/"
                            + checkedoutfiles.get(s.toString()));
                    Utils.writeContents(new File(s.toString()),
                            Utils.readContentsAsString(oldfile));
                }
            }
            _head = str;
            _stage.clear();
        }
    }

    /**Remove method STR. */
    public void rm(String str) {
        Commit c = idtocommit(_branches.get(_head));
        HashMap<String, String> current = c.getmapping();
        if (!_stage.containsKey(str) && !current.containsKey(str)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (_stage.containsKey(str)) {
            _stage.remove(str);
        } else if (current.containsKey(str)) {
            _untracked.add(str);
            File del = new File(str);
            Utils.restrictedDelete(del);
        } else {
            throw new GitletException("No reason to remove the file.");
        }
    }
    /** Prints status. */
    public void status() {
        System.out.println("=== Branches ===");
        Object[] branchnames = _branches.keySet().toArray();
        Arrays.sort(branchnames);
        for (Object s : branchnames) {
            if (s.equals(_head)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        Object[] stagedfiles = _stage.keySet().toArray();
        Arrays.sort(stagedfiles);
        for (Object s : stagedfiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        if (_untracked != null && !_untracked.isEmpty()) {
            Object[] removed = _untracked.toArray();
            Arrays.sort(removed);
            for (Object s : removed) {
                System.out.println(s.toString());
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        modificationprint();
        System.out.println();
        System.out.println("=== Untracked Files ===");
        untrackedprint();
        System.out.println();
    }

    /** helper for status.*/
    public void untrackedprint() {
        Commit c = idtocommit(_branches.get(_head));
        HashMap<String, String> files = c.getmapping();
        Set keys = files.keySet();
        String wd = System.getProperty("user.dir");
        File wdfiles = new File(wd);
        for (File f : wdfiles.listFiles()) {
            String name = f.getName();
            if (!name.equals(".gitlet")) {
                if (!keys.contains(f.getName())
                    && !_stage.containsKey(f.getName())) {
                    System.out.println(f.getName());
                }
            }
        }
    }

    /** helper for status2.*/
    public void modificationprint() {
        Commit c = idtocommit(_branches.get(_head));
        HashMap<String, String> files = c.getmapping();
        Set keys = files.keySet();
        String wd = System.getProperty("user.dir");
        File wdfiles = new File(wd);
        for (File f : wdfiles.listFiles()) {
            if (!f.getName().equals(".gitlet")) {
                if (keys.contains(f.getName())) {
                    File old = new File(".gitlet/staging/"
                            + files.get(f.getName()));
                    String currcont = Utils.readContentsAsString(f);
                    String oldcont = Utils.readContentsAsString(old);
                    String sha = Utils.sha1(currcont);
                    if (!oldcont.equals(currcont)) {
                        if (_cf != null
                                && _cf.contains(
                                 Utils.sha1(Utils.readContentsAsString(f)))) {
                            break;
                        } else {
                            System.out.println(f.getName() + " (modified)");
                        }
                    }
                }
            }
        }
        ArrayList<String> names = new ArrayList<>();
        for (File f: wdfiles.listFiles()) {
            names.add(f.getName());
        }
        for (Object s: files.keySet()) {
            if (!names.contains(s) && !_untracked.contains(s)) {
                System.out.println(s + " (deleted)");
            }

        }
    }

    /**
     * Creates a new branch STR.
     */
    public void branch(String str) {
        if (_branches.containsKey(str)) {
            System.out.println("A branch with that name already exists.");
        } else {
            _branches.put(str, _branches.get(_head));
        }
    }

    /**
     * Removes a branch STR.
     */
    public void rmbranch(String str) {
        if (!_branches.containsKey(str)) {
            System.out.println("A branch with that name does not exist.");
        } else if (str.equals(_head)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            _branches.remove(str);
        }
    }


    /**
     * The find method. Prints out the ids of all commits
     * that have the given commit message, one per line STR.
     **/
    public void find(String str) {
        File commits = new File(".gitlet/commits");
        File[] allcommits = commits.listFiles();
        boolean exists = false;
        assert allcommits != null;
        for (File f : allcommits) {
            if (idtocommit(f.getName()).getmessage().equals(str)) {
                System.out.println(f.getName());
                exists = true;
            }
        }
        if (!exists) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * The global log method. Prints out all the commits.
     **/
    public void globallog() {
        File commits = new File(".gitlet/commits");
        File[] allcommits = commits.listFiles();
        for (File f : allcommits) {
            Commit c = idtocommit(f.getName());
            System.out.println("===");
            System.out.println("commit " + c.hash());
            System.out.println("Date: " + c.gettimestamp());
            System.out.println(c.getmessage());
            System.out.println();
        }
    }

    /**
     * The reset method STR.
     **/
    public void reset(String str) {
        String id = null;
        if (str.length() < MAX_SIZE) {
            id = shortidtolong(str);
        } else {
            id = str;
        }
        File commits = new File(".gitlet/commits");
        boolean n = false;
        for (File f : commits.listFiles()) {
            if (id.equals(f.getName())) {
                n = true;
            }
        }
        if (!n) {
            System.out.println("No commit with that id exists.");
            return;
        }
        boolean m = untracked();
        if (m) {
            return;
        }
        Commit reset = idtocommit(id);
        assert reset != null;
        HashMap<String, String> resetfiles = reset.getmapping();
        String wd = System.getProperty("user.dir");
        File wdfiles = new File(wd);
        for (File f : wdfiles.listFiles()) {
            String name = f.getName();
            if (!name.equals(".gitlet")) {
                if (!resetfiles.containsKey(f.getName())) {
                    Utils.restrictedDelete(f);
                }
            }
        }
        if (resetfiles != null) {
            for (Object s : resetfiles.keySet()) {
                File oldfile = new File(".gitlet/staging/"
                        + resetfiles.get(s.toString()));
                File newfile = new File(s.toString());
                Utils.writeContents(newfile,
                        Utils.readContentsAsString(oldfile));
            }
        }
        _branches.put(_head, reset.hash());
        _stage.clear();
    }

    /**
     * The merge method STR.
     */
    public void merge(String str) {
        boolean modified = false;
        boolean conflict = false;
        if (_stage.size() > 0 || _untracked.size() > 0) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (!_branches.containsKey(str)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (str.equals(_head)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        boolean n = untracked();
        if (n) {
            return;
        }
        String split = splitpoint(str);
        Commit splitpoint = idtocommit(split);
        if (idtocommit(_branches.get(str)).getparents()[0]
                .equals(_branches.get(_head))) {
            System.out.println("Current branch fast-forwarded.");
            String wd = System.getProperty("user.dir");
            File wdfiles = new File(wd);
            Commit current = idtocommit(_branches.get(str));
            Set s = current.getmapping().keySet();
            for (File f: wdfiles.listFiles()) {
                if (!s.contains(f.getName())) {
                    File del = new File(f.getName());
                    Utils.restrictedDelete(del);
                }
            }
            _branches.put(_head, _branches.get(str));
            return;
        }
        if (split.equals(_branches.get(str))) {
            System.out.println("Given branch is "
                    + "an ancestor of the current branch.");
            return;
        }
        HashMap<String, String> splitfiles = splitpoint.getmapping();
        Commit head = idtocommit(_branches.get(_head));
        HashMap<String, String> headfiles = head.getmapping();
        Commit merged = idtocommit(_branches.get(str));
        HashMap<String, String> mergedfiles = merged.getmapping();
        boolean a = mergehelp1(splitfiles, headfiles, mergedfiles);
        boolean b = mergehelp2(splitfiles, headfiles, mergedfiles);
        boolean c = mergehelp3(splitfiles, headfiles, mergedfiles);
        conflict = mergehelp4(splitfiles, headfiles, mergedfiles);
        if (a || b || c) {
            mergecommit("Merged " + str + " into " + _head + ".", str);
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Merge helper SPLITFILES HEADFILES MERGEDFILES.
     * @param splitfiles
     * @param headfiles
     * @param mergedfiles
     * @return boolean
     */
    public boolean mergehelp1(HashMap<String, String> splitfiles,
                              HashMap<String, String> headfiles,
                              HashMap<String, String> mergedfiles) {
        boolean modified = false;
        for (String s : splitfiles.keySet()) {
            if (mergedfiles.containsKey(s) && headfiles.containsKey(s)) {
                String file = splitfiles.get(s);
                if (!mergedfiles.get(s).equals(file)
                        && headfiles.get(s).equals(file)) {
                    File oldfile = new File(".gitlet/staging/"
                            + mergedfiles.get(s));
                    Utils.writeContents(new File(s),
                            Utils.readContentsAsString(oldfile));
                    _stage.put(s,
                            Utils.sha1(Utils.readContentsAsString(oldfile)));
                    modified = true;
                }
            }
        }
        return modified;
    }

    /** Merge helper2 SPLITFILES HEADFILES MERGEDFILES.
     * @param splitfiles
     * @param headfiles
     * @param mergedfiles
     * @return boolean
     */
    public boolean mergehelp2(HashMap<String, String> splitfiles,
                              HashMap<String, String> headfiles,
                              HashMap<String, String> mergedfiles) {
        boolean modified = false;
        for (String s : mergedfiles.keySet()) {
            if (!splitfiles.containsKey(s) && !headfiles.containsKey(s)) {
                File old = new File(".gitlet/staging/" + mergedfiles.get(s));
                Utils.writeContents(new File(s),
                        Utils.readContentsAsString(old));
                _stage.put(s,
                        Utils.sha1(Utils.readContentsAsString(old)));
                modified = true;
            }
        }
        return modified;
    }

    /** Merge helper3 SPLITFILES HEADFILES MERGEDFILES.
     * @param splitfiles
     * @param headfiles
     * @param mergedfiles
     * @return boolean
     */
    public boolean mergehelp3(HashMap<String, String> splitfiles,
                              HashMap<String, String> headfiles,
                              HashMap<String, String> mergedfiles) {
        boolean modified = false;
        for (String s : splitfiles.keySet()) {
            if (headfiles.containsKey(s)) {
                if (headfiles.get(s).equals(splitfiles.get(s))) {
                    if (!mergedfiles.containsKey(s)) {
                        _untracked.add(s);
                        File del = new File(s);
                        Utils.restrictedDelete(del);
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }

    /** Merge helper4 SPLITFILES HEADFILES MERGEDFILES.
     * @param splitfiles
     * @param headfiles
     * @param mergedfiles
     * @return boolean
     */
    public boolean mergehelp4(HashMap<String, String> splitfiles,
                              HashMap<String, String> headfiles,
                              HashMap<String, String> mergedfiles) {
        boolean conflict = false;
        for (String f : headfiles.keySet()) {
            if (mergeconflict(f, splitfiles, headfiles, mergedfiles)) {
                File towrite = new File(f);
                File mergefile = new File(".gitlet/staging/"
                        + mergedfiles.get(f));
                if (mergefile.exists()) {
                    Utils.writeContents(towrite, "<<<<<<< HEAD\n",
                            Utils.readContentsAsString(
                                    new File(".gitlet/staging/"
                                            + headfiles.get(f))), "=======\n",
                            Utils.readContentsAsString(
                                    new File(".gitlet/staging/"
                                            + mergedfiles.get(f))),
                            ">>>>>>>\n");
                    conflict = true;
                } else {
                    Utils.writeContents(towrite, "<<<<<<< HEAD\n",
                            Utils.readContentsAsString(new File(
                                    ".gitlet/staging/"
                                            + headfiles.get(f))), "=======\n",
                            ">>>>>>>\n");
                    conflict = true;

                }
                _stage.put(f, Utils.sha1(Utils.readContentsAsString(towrite)));
                _cf.add(Utils.sha1(Utils.readContentsAsString(towrite)));
                File old = new File(".gitlet/staging/"
                        + Utils.sha1(Utils.readContentsAsString(towrite)));
                Utils.writeContents(old, Utils.readContentsAsString(towrite));
            }
        }
        return conflict;
    }

    /** finds mergeconflict. FILE SPLIT HEAD MERGE.
     * @return boolean */
    public boolean mergeconflict(String file, HashMap<String,
            String> split, HashMap<String, String> head,
                                 HashMap<String, String> merge) {
        if ((!split.containsKey(file) && head.containsKey(file)
                && merge.containsKey(file)
                && !merge.get(file).equals(head.get(file)))) {
            return true;
        }
        if (split.containsKey(file) && head.containsKey(file)
                && !head.get(file).equals(split.get(file))
                && !merge.containsKey(file)) {
            return true;
        }
        if ((split.containsKey(file) && head.containsKey(file)
                && merge.containsKey(file)
                && !merge.get(file).equals(head.get(file)))
                && !merge.get(file).equals(split.get(file))
                && !head.get(file).equals(split.get(file))) {
            return true;
        }
        return false;
    }


    /** finds splitpoints BRANCH.
     * @return string */
    public String splitpoint(String branch) {
        String par1 = null;
        String par2 = null;
        par1 = _branches.get(_head);
        par2 = _branches.get(branch);
        while (!par1.equals(par2)) {
            if (idtocommit(par1).getparents() != null) {
                par1 = idtocommit(par1).getparents()[0];
            }
            if (idtocommit(par2).getparents() != null) {
                par2 = idtocommit(par2).getparents()[0];
            } else {
                break;
            }
        }
        return par1;
    }


    /** Checks if there are untracked files in the working directory.
     * @return boolean */
    public boolean untracked() {
        String wd = System.getProperty("user.dir");
        File wdfiles = new File(wd);
        Commit current = idtocommit(_branches.get(_head));
        Set s = current.getmapping().keySet();
        for (File f: wdfiles.listFiles()) {
            if (!f.getName().equals(".gitlet")) {
                if (!_stage.containsKey(f.getName())
                        && !s.contains(f.getName())) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it or add it first.");
                    return true;
                }
            }
        }
        return false;
    }


    /** Converts hashID to commit ID.
     * @return commit*/
    public Commit idtocommit(String id) {
        File f = new File(".gitlet/commits/" + id);
        if (f.exists()) {
            return Utils.readObject(f, Commit.class);
        }
        return null;
    }

    /** Returns full version of shortened commit ID STR.
     * @return string*/
    public String shortidtolong(String str) {
        if (str.length() == MAX_SIZE) {
            return str;
        } else {
            File commits = new File(".gitlet/commits");
            for (File f : commits.listFiles()) {
                if (str.equals(f.getName().substring(0, str.length()))) {
                    return f.getName();
                }
            }
        }
        return null;
    }

    /** Accessor for branches.
     * @return hashmap*/
    public HashMap<String, String> getbranches() {
        return _branches;
    }

    /** Head string. */
    private String _head;

    /** HashMap of strings referring to commits that begin a branch. */
    private HashMap<String, String> _branches;

    /** Staged "blobs", mapping of blob names to blob hashID . */
    private HashMap<String, String> _stage;

    /** ArrayList of the names of "untracked" files. */
    private ArrayList<String> _untracked;

    /** ArrayList of conflict file SHAs. */
    private static ArrayList<String> _cf = new ArrayList<>();

    /** Max size of sha. */
    public static final int MAX_SIZE = 40;

}

