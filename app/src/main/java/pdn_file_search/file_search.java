package pdn_file_search;



import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.file.attribute.BasicFileAttributes;

public class file_search {

    private static final ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger activeThreads = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, IOException {
        String[] rootFolders = {"/home/all_c/Изображения/", "/home/all_c/Загрузки/"};
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (String folder : rootFolders) {
            executor.submit(() -> scanFolder(Paths.get(folder)));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        writeResultsToFile("results.txt");
    }

    private static void scanFolder(Path folder) {
        activeThreads.incrementAndGet();
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.toString().toLowerCase();
                    if (fileName.endsWith(".xlsx") || fileName.endsWith(".docx")) {
                        results.add(file.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            activeThreads.decrementAndGet();
        }
    }

    private static void writeResultsToFile(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String result : results) {
                writer.write(result);
                writer.newLine();
            }
        }
    }
}