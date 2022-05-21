package com.ram.Results;

import com.ram.Database.MongoDatabase;

import java.util.*;
import java.lang.Double;

public class PageRanker
{
    MongoDatabase Database;
    ArrayList<PageResult> Results;

    public PageRanker(ArrayList<PageResult> PageResults)
    {
        Database = new MongoDatabase("SearchEngineDatabase");
        this.Results = PageResults;
        this.Rank();
    }

    public ArrayList<PageResult> getRankedPages()
    {
        return this.Results;
    }

    public ArrayList<String> getAllDistinctLinksOfPageResults()
    {
        ArrayList<PageResult> DistinctPageResults = new ArrayList<>();
        ArrayList<String> ResultsLinks = new ArrayList<>();
            String link;
            int i = 0;
            for (PageResult Result : Results) {
                link = Result.getURL();
                if (ResultsLinks.contains(link))
                {
                    for (PageResult result : DistinctPageResults)
                    {
                        if (result.getURL().equals(link)) {
                            result.incrementCount();
                            break;
                        }
                    }
                    continue;
                }
                Result.incrementCount();
                DistinctPageResults.add(Result);
                ResultsLinks.add(link);
            }
            this.Results = DistinctPageResults;
            return ResultsLinks;
    }

    public void calculateFinalScore()
    {
        for (PageResult result : Results)
        {
            double FinalScore;
            FinalScore = result.getCount() * (1000 * result.getTFIDF() + result.getPageRank() + result.getPositionScore());
            result.setFinalScore(FinalScore);
        }
    }

    //Calculates page rank using the page rank algorithm
    public void calculatePageRank()
    {
        ArrayList<String> ResultsLinks = getAllDistinctLinksOfPageResults();
        final int N = Results.size();   //Total number pf pages
        //Initialize all page ranks with 1/N, N --> Number of results
        for (int i = 0; i < N ; i++)
        {
            float initialize = (float)1/N;
            Results.get(i).setPageRank(initialize);
        }

        for(int i = 0; i < 5; i++)
        {
            for (int j = 0; j < N ; j++)
            {
                double PageRank = 0;
                ArrayList<String> InLinks = Database.getIngoingLinks(Results.get(j).getURL());
                if(InLinks == null) continue;
                for (int k = 0; k < N; k++)
                {
                    if(j == k) continue;
                    if(InLinks.contains(Results.get(k).getURL()))
                    {
                        List<String> OutLinks = Results.get(k).getOutgoingLinks();
                        if(OutLinks == null) continue;
                        int OutCount = 0;
                        for (String link : ResultsLinks)
                        {
                            if(OutLinks.contains(link))
                            {
                                OutCount++;
                            }
                        }

                        if(OutCount == 0) continue;

                        PageRank += Results.get(k).getPageRank()/OutCount;
                    }
                }
                if(PageRank == 0) continue;
                Results.get(j).setPageRank(PageRank);
            }
        }
    }

    public void Rank()
    {
        this.calculatePageRank();
        this.calculateFinalScore();
        try
        {
            Results.sort(new PageComparator());
        }
        catch (Exception e)
        {
            Comparator<PageResult> comparator = new PageComparator();
            for (PageResult PageResult1: Results) {
                for (PageResult PageResult2: Results) {
                    if(comparator.compare(PageResult1,PageResult2) != -comparator.compare(PageResult2,PageResult1))
                    {
                        System.out.println("How come ya3ny");
                    }
                }
            }
        }

    }

    public static class PageComparator implements Comparator<PageResult>
    {
        @Override
        public int compare(PageResult Page1, PageResult Page2)
        {
            return Double.compare(Page1.getFinalScore(), Page2.getFinalScore());
        }
    }

}
