package com.example.informationretrieval;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.analysis.core.StopFilter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InformationRetrieval {
    public static void main(String[] args) throws IOException{
        String indexPath = "index";
        String docsPath = "dataset";
        String currPath = System.getProperty("user.dir");

        docsPath = currPath + "/" + docsPath;

        boolean runIndexing = false;

        if (runIndexing) {
            IndexFiles.main(docsPath, indexPath);
        }

        String sQuery = "film";

        try {
            SearchFiles.main(sQuery, docsPath);
        } catch (Exception e) {
            System.out.println("Invalid search input received. Enter search query only when 'Enter query: ' is prompted");
            e.printStackTrace();
        }
    }
}
