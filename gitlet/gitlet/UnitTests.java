package gitlet;


import org.junit.Test;

import java.io.File;

public class UnitTests {

    @Test
    public void commitTest1() {
        Gitlet testGit = new Gitlet();
        testGit.add("wug.txt");
        testGit.commit("commit 1st wug");

        File wugg = new File("wug.txt");
        File wugg2 = new File("notwug.txt");

        testGit.checkout(new String[]{"--", "wug.txt"});
    }

    @Test
    public void commitTest2() {
        Gitlet testGit = new Gitlet();
        testGit.add("wug.txt");
        testGit.commit("commit 2nd wug");

        File wugg = new File("wug.txt");
        File wugg2 = new File("notwug.txt");

        testGit.checkout(new String[]{"--", "wug.txt"});
        File gitletDir = new File(".gitlet");
        gitletDir.delete();
    }

    @Test
    public void statusTest() {
        Gitlet testGit = new Gitlet();
        testGit.status();
        File gitletDir = new File(".gitlet");
        gitletDir.delete();

    }

    @Test
    public void logtest() {
        Gitlet testGit = new Gitlet();
        testGit.add("wug.txt");
        testGit.commit("commit 2nd wug");
        testGit.log();
        File gitletDir = new File(".gitlet");
        gitletDir.delete();
    }

    @Test
    public void globallog() {
        Gitlet testGit = new Gitlet();
        testGit.globallog();
    }

    @Test
    public void branch() {
        Gitlet testGit = new Gitlet();
        testGit.branch("other");
    }

    @Test
    public void globallog2() {
        Gitlet testGit = new Gitlet();
        testGit.add("wug.txt");
        testGit.commit("commit 2nd wug");
        testGit.globallog();
    }

    @Test
    public void log2() {
        Gitlet testGit = new Gitlet();
        testGit.add("wug.txt");
        testGit.commit("commit 2nd wug");
        testGit.log();
    }

    @Test
    public void log3() {
        Gitlet testGit = new Gitlet();
        testGit.add("wug.txt");
        testGit.log();
    }

    @Test
    public void init() {
        Gitlet testGit = new Gitlet();
    }


    @Test
    public void checkout() {
        Gitlet testGit = new Gitlet();
        testGit.checkout("master");
    }

    @Test
    public void checkout1() {
        Gitlet testGit = new Gitlet();
        testGit.branch("other");
        testGit.checkout("master");
    }

}
