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
        String usage = "java -jar InformationRetrieval.jar"
                + " [-DATASET_PATH] [-QUERY]\n\n"
                + "[-DATASET_PATH] Path of the dataset\n"
                + "[-QUERY] Search query";

        int n = args.length;
        if (n != 2) {
            System.out.println("Invalid number of arguments provided");
            System.err.println("Usage: " + usage);
        } else {
            String datasetPath = args[0];
            String sQuery = args[1];

            if (datasetPath.length() == 0 || datasetPath.isEmpty() || sQuery.length() == 0 || sQuery.isEmpty()) {
                System.err.println("[-DATASET_PATH] and [-QUERY] should not be empty.");
                System.err.println("Usage: " + usage);
                System.exit(1);
            }

            System.out.println("Path of dataset: " + datasetPath);
            System.out.println("Query: " + sQuery);

            String indexPath = datasetPath + "/index";

            boolean runIndexing = true;

            if (runIndexing) {
                IndexFiles.main(datasetPath, indexPath);
            }

            try {
                SearchFiles.main(sQuery, datasetPath);
            } catch (Exception e) {
                System.out.println("Invalid search input received.");
                e.printStackTrace();
            }
        }
    }
}
