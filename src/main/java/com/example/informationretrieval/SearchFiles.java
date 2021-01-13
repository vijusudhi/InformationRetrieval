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

        // PhraseQuery query = new PhraseQuery();
        // query.setSlop(0);

        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        String[] sQueryparts = sQuery.split(" ");

        for(String word: sQueryparts) {
            builder.add(new Term(field, word));
        }

        //PhraseQuery query = builder.build();

        // System.out.println("Searching for: " + query.toString(field));

        searcher.setSimilarity( new BM25Similarity() );

        TopDocs results = searcher.search(query, 10);
        ScoreDoc[] hits = results.scoreDocs;

        Pattern pattern = Pattern.compile("Topic[0-9]{1,}");

        //Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
        Formatter formatter = new SimpleHTMLFormatter();

        //It scores text fragments by the number of unique query terms found
        //Basically the matching score in layman terms
        QueryScorer scorer = new QueryScorer(query);

        //used to markup highlighted terms found in the best sections of a text
        Highlighter highlighter = new Highlighter(formatter, scorer);

        //It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);

        //breaks text up into same-size fragments with no concerns over spotting sentence boundaries.
        // Fragmenter fragmenter = new SimpleFragmenter(10);

        //set fragmenter to highlighter
        highlighter.setTextFragmenter(fragmenter);

        int rank = 1;
        for(ScoreDoc hit : hits) {
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
                    if ((fileName.endsWith(".jpg")) || (fileName.endsWith(".gif")) ||
                            (fileName.endsWith(".png")) || (fileName.endsWith(".jpeg"))) {
                        imagePath = fileName;
                    }
                }
            }

            String imageFile = topicPath + "/" + imagePath;
            Path imageFilePath = Paths.get(imageFile);

            System.out.println(rank + "\t" + topicId + "\t\t" + txtResult + "\t\t" + imageFilePath + "\t\t" + hit.score);
            rank += 1;


            //Get stored text from found document
            String text = doc.get("contents");

            //Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

            //Get highlighted text fragments
            String[] frags = highlighter.getBestFragments(stream, text, 10);
            for (String frag : frags)
            {
                System.out.println(frag);
            }
            System.out.println("=====================================================================");


        }

        reader.close();
    }

}



