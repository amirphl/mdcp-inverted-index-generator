package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipFile;

import java.util.zip.ZipEntry;

public class Merger {

    public static final String WORD_SPLITTER = ":";
    public static final String OUTPUT_PATH = "final-index.txt";
    public static final String FIELDS = "fields";
    public static final String NORMS_BODY = "norms.body";
    public static final String STORED_TITLE = "stored.title";

    public static HashMap<String, StringBuilder> mergeTwoIndexes(HashMap<String, StringBuilder> firstIndex,
            HashMap<String, StringBuilder> secondIndex) {
        String word;
        StringBuilder currDocumentFreq;
        StringBuilder oldDocumentFreq;
        StringBuilder newDocumentFreq;
        for (Map.Entry<String, StringBuilder> wordDocuments : secondIndex.entrySet()) {
            word = wordDocuments.getKey();
            currDocumentFreq = wordDocuments.getValue();
            oldDocumentFreq = firstIndex.get(word);
            newDocumentFreq = (oldDocumentFreq == null) ? currDocumentFreq
                    : oldDocumentFreq.append("|").append(currDocumentFreq);
            firstIndex.put(word, newDocumentFreq);
        }
        return firstIndex;
    }

    public static HashMap<String, StringBuilder> merge(List<HashMap<String, StringBuilder>> indexes) {
        Optional<HashMap<String, StringBuilder>> finalIndex = indexes.stream()
                .reduce((firstIndex, secondIndex) -> mergeTwoIndexes(firstIndex, secondIndex));
        return finalIndex.get();
    }

    public static HashMap<String, StringBuilder> loadIndex(String indexZipPath) throws IOException {
        ZipFile zipFile = new ZipFile(indexZipPath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ArrayList<InputStream> partailIndexInputStreams = new ArrayList<>();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(FIELDS) || entry.getName().equals(NORMS_BODY)
                    || entry.getName().equals(STORED_TITLE)) {
                System.out.println("skipped file: " + entry.getName());
                continue;
            }
            InputStream stream = zipFile.getInputStream(entry);
            partailIndexInputStreams.add(stream);
        }

        HashMap<String, StringBuilder> index = new HashMap<>();

        for (InputStream stream : partailIndexInputStreams) {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            String word;
            String documentsFreq;
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(WORD_SPLITTER);
                word = arr[0];
                documentsFreq = arr[1];
                index.put(word, new StringBuilder(documentsFreq));
            }
        }

        zipFile.close();
        return index;
    }

    public static void main(String args[]) throws IOException {
        long s = System.currentTimeMillis();
        if (args.length == 0) {
            System.out.println("no args found...");
            return;
        }
        ArrayList<HashMap<String, StringBuilder>> indexes = new ArrayList<>();

        // File f = new File("outputs-folder");
        // File[] files = f.listFiles();
        // Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        for (String indexZipPath : args) {
            HashMap<String, StringBuilder> index = loadIndex(indexZipPath);
            indexes.add(index);
        }

        // for (File a : files) {
        // System.out.println(a.getAbsolutePath());
        // HashMap<String, StringBuilder> index = loadIndex(a.getAbsolutePath());
        // indexes.add(index);
        // }

        HashMap<String, StringBuilder> finalIndex = merge(indexes);
        writeIndex(finalIndex);
        long e = System.currentTimeMillis();
        System.out.println("took " + (e - s) + " seconds");
    }

    public static void writeIndex(HashMap<String, StringBuilder> index) throws IOException {
        FileWriter fw = new FileWriter(new File(OUTPUT_PATH));
        String word;
        StringBuilder documentFreq;
        for (Map.Entry<String, StringBuilder> wordDocuments : index.entrySet()) {
            word = wordDocuments.getKey();
            documentFreq = wordDocuments.getValue();
            fw.write(word);
            fw.write(":");
            fw.write(documentFreq.toString());
            fw.write("\n");
        }
    }
}
