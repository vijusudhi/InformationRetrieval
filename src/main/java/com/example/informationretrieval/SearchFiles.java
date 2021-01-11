package com.example.informationretrieval;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Simple command-line based search. */
public class SearchFiles {
    public static void main(String sQuery, String docsPath) throws Exception {
        String index = "index";
        String field = "contents";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new EnglishAnalyzer();

        QueryParser parser = new QueryParser(field, analyzer);

        Query query = parser.parse(sQuery);
        System.out.println("Searching for: " + query.toString(field));

        searcher.setSimilarity( new BM25Similarity() );

        TopDocs results = searcher.search(query, 10);
        ScoreDoc[] hits = results.scoreDocs;

        Pattern pattern = Pattern.compile("Topic[0-9]{1,}");

        int rank = 1;
        for(ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            String path = doc.get("path");


            Matcher matches = pattern.matcher(path);

            String topicId = "";
            if (matches.find()) {
                topicId = matches.group(0);
            }

            String topicPath = docsPath + "/" + topicId + "/";
            Path docDir = Paths.get(topicPath);

            String imagePath = "";
            File folder = new File(topicPath);
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if ((fileName.endsWith(".jpg")) || (fileName.endsWith(".gif")) ||
                            (fileName.endsWith(".png")) || (fileName.endsWith(".jpeg"))) {
                        imagePath = fileName;
                    }
                }
            }

            String imageFile = topicPath + "/" + imagePath;
            Path imageFilePath = Paths.get(imageFile);

            System.out.println(rank + "\t" + topicId + "\t\t" +path + "\t\t" + imageFilePath + "\t\t" + hit.score);
            rank += 1;
        }

        reader.close();
    }

}



