package com.example.informationretrieval;

import java.io.*;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexFiles {
    public static void main(String docsPath, String indexPath) {
        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        try {
            System.out.println("Indexing to directory: " + indexPath);
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new EnglishAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);

            writer.close();

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    /** Recursively walks through the document directory and indexes every HTML or TXT document */
    public static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileLowerCaseName = file.toString().toLowerCase();
                    if (fileLowerCaseName.endsWith(".txt")) {
                        try {
                            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                        } catch (IOException ignore) {
                            // don't index files that can't be read.
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /** Indexes a single document */
    public static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
            Document doc = new Document();

            Field pathField = new StringField("path", file.toString(), Field.Store.YES);
            doc.add(pathField);
            doc.add(new TextField("contents", new String(Files.readAllBytes(file)), Field.Store.YES));

            writer.addDocument(doc);

    }
}
