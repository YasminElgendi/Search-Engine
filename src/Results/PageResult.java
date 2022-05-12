package Results;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class PageResult
{
    private double TFIDF;
    private double PageRank;
    private Document SearchDocument;
    private double FinalScore;
    private ArrayList<Document> ingoingLinksToPage;
    private int countOfIngoingLinksToPage;

    public PageResult(double tfidf)
    {
        this.TFIDF = tfidf;
    }

    //Returns URL of result page
    public String getURL()
    {
        return this.SearchDocument.getString("URL");
    }

    public void setPageRank(double pagerank)
    {
        this.PageRank = pagerank;
    }

    public double getPageRank()
    {
        return this.PageRank;
    }

    public void setFinalScore(double finalscore)
    {
        this.FinalScore = finalscore;
    }

    public void setSearchDocument(Document document)
    {
        this.SearchDocument = document;
    }




}
