package duplicities;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Duplicities {

    private static final int threadCount = 4;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Searching for duplicities via " + args);
        new Duplicities(args);
    }

    Duplicities(String[] args) throws InterruptedException {
        // create hash of all files
        duplicities.FileHash fileHash = new FileHash();
        Map<String, List<File>> fileHashes = fileHash.generateFileHashes(args, threadCount);

        System.out.println("Hashes calculated");

        // check for duplicities by binary content
        ContentCompare contentCompare = new ContentCompare();
        contentCompare.compareFiles(fileHashes, threadCount);

        System.out.println("Finished:)");
    }

    protected static void DEBUG(String fmt, Object... args) {
        System.out.printf("[debug]: " + fmt + "\n", args);
    }

    protected static boolean checkProcessing(Boolean[] flags) {
        for (Boolean flag : flags) {
            if (flag) return true;
        }
        return false;
    }
}
