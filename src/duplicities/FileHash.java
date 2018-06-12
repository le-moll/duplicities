package duplicities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.security.MessageDigest;

public class FileHash {


    private Map<String, List<File>> hashes = new HashMap<>();

    private ArrayBlockingQueue<File> queue;
    private Boolean processingHash;
    private Boolean[] flags;


    public Map<String, List<File>> generateFileHashes(String[] args, int threadCount) throws InterruptedException {

        queue = new ArrayBlockingQueue<>(10);
        processingHash = true;

        hashes = new HashMap<>();

        flags = new Boolean[threadCount];
        for (int i = 0; i < threadCount; i++) {
            flags[i] = true;
        }

        List<Thread> threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(new HashCoder(i)));
            threads.get(i).start();
        }

        generateTasks(args);

        while (queue.size() != 0) {
            Thread.sleep(10);
        }

        while (Duplicities.checkProcessing(flags)) {
            Thread.sleep(10);
        }

        processingHash = false;

        return hashes;

    }

    private void generateTasks(String[] args) throws InterruptedException {
        for (String filename : args) {
            findFiles(new File(filename));
        }
    }

    private void findFiles(File file) throws InterruptedException {
        if (file.isFile()) {
            queue.put(file);
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                findFiles(f);
            }
        }
    }



    class HashCoder implements Runnable {

        int index;

        public HashCoder(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            while (processingHash) {
                try {
                    if (queue.size() != 0) {
                        flags[index] = true;
                        File file = queue.take();
                        String fileHash = getFileHash(file);
                        saveHash(file, fileHash);
                        flags[index] = false;
                    } else {
                        Thread.sleep(10);
                        flags[index] = false;
                    }
                } catch (InterruptedException | NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Hash thread" + index + " finished");

        }
    }

    private synchronized void saveHash(File file, String fileHash) {
        if (hashes.containsKey(fileHash)) {
            List<File> files = hashes.get(fileHash);
            files.add(file);
        } else {
            List<File> files = new ArrayList<>();
            files.add(file);
            hashes.put(fileHash, files);
        }
    }

    private static String getFileHash(File f) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        InputStream is = new FileInputStream(f);
        byte[] data = new byte[1024];

        while (true) {
            int actuallyRead = is.read(data);
            if (actuallyRead == -1) {
                break;
            }
            digest.update(data, 0, actuallyRead);
        }

        is.close();

        byte[] digestBytes = digest.digest();

        StringBuffer sb = new StringBuffer();
        for (byte b : digestBytes) {
            sb.append(String.format("%02x", b));
        }

        String hash = sb.toString();

        Duplicities.DEBUG("%s => %s", f.getName(), hash);

        return hash;
    }



}
