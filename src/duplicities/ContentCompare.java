package duplicities;

import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class ContentCompare {

    ArrayBlockingQueue<Pair<File, File>> tasks;
    Boolean processing;
    Boolean[] flags;

    public void compareFiles(Map<String, List<File>> fileHashes, int threadCount) throws InterruptedException {

        processing = true;

        tasks = new ArrayBlockingQueue<>(20);

        flags = new Boolean[threadCount];
        for (int i = 0; i < threadCount; i++) {
            flags[i] = true;
        }

        List<Thread> threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(new BinaryCompare(i)));
            threads.get(i).start();
        }

        generateTasks(fileHashes);

        while (tasks.size() != 0) {
            Thread.sleep(10);
        }

        while (Duplicities.checkProcessing(flags)) {
            Thread.sleep(10);
        }

        processing = false;
    }

    private void generateTasks(Map<String, List<File>> fileHashes) throws InterruptedException {
        for (String hash : fileHashes.keySet()) {
            List<File> files = fileHashes.get(hash);
            if (files.size() > 1) generateAllPairs(files);
        }
    }

    private void generateAllPairs(List<File> files) throws InterruptedException {
        for (int i = 0; i < files.size() - 1; i++) {
            for (int j = i + 1; j < files.size(); j++) {
                tasks.put(new Pair<>(files.get(i), files.get(j)));
            }
        }
    }

    class BinaryCompare implements Runnable {

        int index;

        BinaryCompare(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            while (processing) {
                try {
                    if (tasks.size() != 0) {
                        flags[index] = true;
                        Pair<File, File> task = tasks.take();
                        if (isSameFile(task.getKey(), task.getValue())) {
                            System.out.println("File " + task.getKey().getName() + "is same as " + task.getValue().getName());
                        }
                        flags[index] = false;
                    } else {
                        Thread.sleep(10);
                        flags[index] = false;
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean isSameFile(File af, File bf) throws IOException {
            Duplicities.DEBUG("Comparing %s and %s.", af, bf);

            InputStream a = new FileInputStream(af);
            InputStream b = new FileInputStream(bf);

            try {
                while (true) {
                    int aByte = a.read();
                    int bByte = b.read();
                    if ((aByte == -1) && (bByte == -1)) {
                        return true;
                    }
                    if (aByte != bByte) {
                        return false;
                    }
                }
            } finally {
                a.close();
                b.close();
            }
        }
    }
}

