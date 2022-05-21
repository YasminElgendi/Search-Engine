package com.ram.Results;

import com.ram.Database.MongoDatabase;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;
import ca.rmen.porterstemmer.PorterStemmer;

/*
 * This class handles the query entered by the user and retrieves related documents from the database
 *
 *
 */

//http://localhost:8080/SearchWordRequest?SearchWord=ofcourse+we+want

public class QueryProcessor
{
    MongoDatabase Database;

    public QueryProcessor(String SearchQuery)
    {
        Database = new MongoDatabase("SearchEngineDatabase");
        ArrayList<PageResult> SearchPageResults = Get(SearchQuery);
        DisplayResults(SearchPageResults);

    }

    public ArrayList<PageResult> retrievePagesFromQuery(ArrayList<String> words) {
        ArrayList<PageResult> results = new ArrayList<>();
        for (String word : words)
        {
            double IDF = Database.getWordIDF(word);
            List<Document> DocumentsPerWord = Database.getDocumentsOfWord(word);
            for (Document document : DocumentsPerWord)
            {
                String URL = document.getString("URL");
                FindIterable<Document> PageResultDocument = Database.getSearchDocument(URL);
                double TF = (double) document.get("TF");
                int Position = (int) document.get("Position");
                double TFIDF = calculateTFIDF(IDF, TF);

                //Creating new page result and setting parameters
                PageResult pageresult = new PageResult();
                pageresult.setTFIDF(TFIDF);
                pageresult.setPositionScore(Position);
                //always iterates once
                for (Document resultDoc : PageResultDocument)
                {
                    pageresult.setSearchDocument(resultDoc);
                }
                results.add(pageresult);
            }
        }
        return results;
    }

    public double calculateTFIDF(double IDF, double TF) {
        return (IDF * TF);
    }


    public ArrayList<PageResult> Get(String query)
    {
        System.out.println(query);
        ArrayList<String> QueryWords = new ArrayList<>(Arrays.asList(query.split(" ")));

        PorterStemmer porterStemmer = new PorterStemmer();

        QueryWords.replaceAll(porterStemmer::stemWord);

        ArrayList<PageResult> PageResults;
        PageResults = retrievePagesFromQuery(QueryWords);

        PageRanker Ranker = new PageRanker(PageResults);
        PageResults = Ranker.getRankedPages();

        return PageResults;
    }

    public void DisplayResults(ArrayList<PageResult> Results)
    {
        try
        {
            FileWriter ResultsFile = new FileWriter("SearchResults.txt");
            PrintWriter WriteToFile = new PrintWriter(ResultsFile);
            for (int i = (Results.size()-1); i >= 0; i--)
            {
                WriteToFile.write(Results.get(i).getTitle() + "\n");
                WriteToFile.write(Results.get(i).getURL() + "\n\n");
            }
            WriteToFile.flush();
            WriteToFile.close();
        }
        catch (IOException ioe)
        {
            System.out.println("Error writing in file");
        }

    }
}