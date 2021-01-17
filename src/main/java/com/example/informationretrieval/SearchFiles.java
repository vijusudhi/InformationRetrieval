package com.example.informationretrieval;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Simple command-line based search. */
public class SearchFiles {
    public static void main(String sQuery, String datasetPath) throws Exception {
        String indexPath = datasetPath + "/index";
        String field = "contents";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new EnglishAnalyzer();

        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(sQuery);

        searcher.setSimilarity( new BM25Similarity() );
        TopDocs results = searcher.search(query, 10);
        ScoreDoc[] hits = results.scoreDocs;

        Formatter formatter = new SimpleHTMLFormatter();
        QueryScorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 100);
        highlighter.setTextFragmenter(fragmenter);

        Pattern pattern = Pattern.compile("Topic[0-9]{1,}");

        int rank = 1;
        int hitCount = 0;

        for(ScoreDoc hit : hits) {
            hitCount += 1;
            int docid = hit.doc;
            Document doc = searcher.doc(hit.doc);
            String path = doc.get("path");
            File txtFile = new File(path);
            String txtFileName = txtFile.getName();
            String txtResult1 = txtFileName.replaceAll("[+.^:,$']","");
            String txtResult2 = txtResult1.replaceAll("_"," ");
            String txtResult = txtResult2.replace("txt","");

            Matcher matches = pattern.matcher(path);
            String topicId = "";
            if (matches.find()) {
                topicId = matches.group(0);
            }
            String topicPath = datasetPath + "/" + topicId + "/";

            String imagePath = "";
            File folder = new File(topicPath);
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if ( (fileName.endsWith(".jpg")) || (fileName.endsWith(".gif")) ||
                            (fileName.endsWith(".png")) || (fileName.endsWith(".jpeg")) ||
                            (fileName.endsWith(".JPG")) || (fileName.endsWith(".GIF")) ||
                            (fileName.endsWith(".PNG")) || (fileName.endsWith(".JPEG")) ) {
                        imagePath = fileName;
                    }
                }
            }

            String imageFile = topicPath + "/" + imagePath;
            Path imageFilePath = Paths.get(imageFile);

            System.out.println(rank + "\t" + "\t\t" + txtResult + "\t\t" + imageFilePath + "\t\t" + hit.score);
            rank += 1;

            String text = doc.get("contents");

            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

            TextFragment[] frags = highlighter.getBestTextFragments(stream, text, true,10);
            for (TextFragment frag : frags)
            {
                System.out.println(frag.toString());
            }

            System.out.println("=====================================================================");
        }

        if (hitCount == 0) {
            System.out.println("No results found for your search query!");
        }

        reader.close();
    }

}



