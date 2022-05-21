package com.ram.Results;

import com.ram.Database.MongoDatabase;
import org.bson.Document;
import java.util.*;

public class PageResult
{
    private double TFIDF;
    private double PageRank;
    private Document SearchDocument;
    private double FinalScore;
    private int PositionScore;
    private List<String> outgoingLinksFromPage;

    private int count = 0;

    private static final MongoDatabase Database = new MongoDatabase("SearchEngineDatabase");

    public PageResult()
    {
        outgoingLinksFromPage = new LinkedList<>();
    }

    public void incrementCount()
    {
        this.count++;
    }

    public int getCount()
    {
        return this.count;
    }

    //Returns URL of result page
    public String getURL()
    {
        return this.SearchDocument.getString("URL");
    }

    public String getTitle()
    {
        return this.SearchDocument.getString("Title");
    }

    public String getContent()
    {
        return this.SearchDocument.getString("Content");
    }

    public void setPageRank(double pagerank)
    {
        this.PageRank = pagerank;
    }

    public double getPageRank()
    {
        return this.PageRank;
    }

    public void setPositionScore(int Position)
    {
        this.PositionScore = Position;
    }

    public int getPositionScore()
    {
        return this.PositionScore;
    }

    public void setFinalScore(double finalscore)
    {
        this.FinalScore = finalscore;
    }

    public double getFinalScore()
    {
        return this.FinalScore;
    }

    public void setSearchDocument(Document document)
    {
        this.SearchDocument = document;
        this.setOutgoingLinksFromPage();
    }

    public List<String> getOutgoingLinks()
    {
        if(outgoingLinksFromPage == null)
        {
            this.outgoingLinksFromPage = Database.getPageLinks((String) this.SearchDocument.get("URL"));
        }
        return this.outgoingLinksFromPage;
    }

    public void setTFIDF(double tfidf)
    {
        this.TFIDF = tfidf;
    }

    public double getTFIDF()
    {
       return TFIDF;
    }

    public void setOutgoingLinksFromPage()
    {
        List<String> links;
        links = Database.getPageLinks(this.SearchDocument.getString("URL"));
        if(links == null)
            return;
        for (int i = 0; i < links.size(); i++)
        {
            if(links.get(i) != null)
            {
                this.outgoingLinksFromPage.add(links.get(i));
            }

        }
    }



}
