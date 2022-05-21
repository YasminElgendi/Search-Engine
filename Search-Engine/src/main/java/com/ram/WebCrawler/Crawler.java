package com.ram.WebCrawler;

import com.ram.Database.MongoDatabase;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Crawler implements Runnable
{
    private static final int LIMIT = 5000; //Maximum number of crawled pages
    private static HashSet<String> completedURLs = new HashSet<>(); //All links that have been crawled and put in the database
    private static Queue<String> URLstobeVisited = new LinkedList<>(); //All links that have been put in the database to be crawled next
    private final MongoDatabase Database;
    static int id;

    public Crawler()
    {
        Database = new MongoDatabase("SearchEngineDatabase"); //Connect to the database
        id = Database.getCrawledCount(); //Unique field for crawled documents
        Database.checkCrawlerState(); //Check crawler state at the beginning
        URLstobeVisited = Database.getToBeCrawledList(); //Get all links to be crawled next
        completedURLs = Database.getCrawledLinksFromDatabase(); //Get all crawled links to not duplicate links
    }

    //Inserts crawled page into database
    public void addTOCrawled(String title, String URL, String content, ArrayList<String> pageLinks)
    {
        //To ensure that not more than a single thread accesses the database at a time
        synchronized (this.Database)
        {
            try
            {
                synchronized (this)
                {
                    id++;
                }
                Database.insertCrawledWebsites(id ,title, URL, content, pageLinks);
                completedURLs.add(URL); //Add to list to check for condition --> avoid duplication
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    //Add link to to be crawled in the database
    public void addTOToBeCrawled(String URL)
    {
        synchronized (this.Database)
        {
            try
            {
                Database.insertToBeCrawledWebsites(URL);
                URLstobeVisited.add(URL); //Add to list to check for condition --> avoid duplication
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    //Check robots.txt file for each link and returns disallowed links
    public static ArrayList<String> getDisallows(String url) throws IOException
    {
        ArrayList<String> disallow = new ArrayList<>();
        URLConnection connect = new URL(url + "/robots.txt").openConnection();
        String line;
        InputStreamReader inputStream = new InputStreamReader(connect.getInputStream());
        BufferedReader reader = new BufferedReader(inputStream);
        try
        {
            while((line = reader.readLine()) != null)
            {
                if (line.contains("User-agent:"))
                {
                    if(line.contains("*")) // All user agents; our search engine included
                    {
                        //all crawlers should not enter
                        while((line = reader.readLine()).contains("Disallow:"))
                        {
                            disallow.add(url +'/'+line.substring(10));
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Something went wrong");
        }
        return disallow;
    }

    /* Main crawling function
     * Crawls all links
     * Updates database
     * Validates links and content
     * */
    public void crawl()
    {
        while (completedURLs.size() < LIMIT) //Checks limit hasn't been reached yet
        {
            System.out.println(completedURLs.size());
            System.out.println(Database.getCrawledCount());

            Document CrawlDocument = Database.retrieveAndDeleteNextToBeCrawled();
            if(CrawlDocument != null)
            {
                String nextURL = CrawlDocument.getString("URL");
                synchronized (this.URLstobeVisited)
                {
                    URLstobeVisited.remove(nextURL);
                }
                org.jsoup.nodes.Document htmlDoc;
                try
                {
                    htmlDoc = Jsoup.connect(nextURL).get();
                }
                catch (IOException ioe)
                {
                    continue;
                }

                Element LangTag = htmlDoc.select("html").first();
                String Language = LangTag.attr("lang");

                if(!Language.equals("en")) continue; //Checks that the page is in english

                String content = htmlDoc.text(); //Gets content of the page

                //Checks if two pages have the same content
                boolean CheckContent = Database.checkIfSameContent(content);
                if(CheckContent) continue;

                System.out.println( "\n" + Thread.currentThread().getName() + "--> Crawling Site: " + Language + "******"+ nextURL + "******");

                //Gets disallowed links from robots.txt
                ArrayList<String> robots = new ArrayList<>();
                try
                {
                    robots = getDisallows(nextURL);
                }
                catch (IOException e)
                {
                    System.out.println("Error reading robots.txt");
                }

                //Crawls html and extracts all links
                Elements links = htmlDoc.select("a[href]");

                ArrayList<String> pageLinks = new ArrayList<>();
                for (Element link : links)
                {
                    if (link.attr("abs:href").contains("http")) //Checks if link starts with http
                    {
                        String visitURL = link.attr("abs:href");
                        if (visitURL.equals(""))  continue; //if empty link skip it

                        // Remove #s from the URL
                        if (visitURL.contains("#"))
                        {
                            visitURL = visitURL.substring(0, visitURL.indexOf("#") - 1);
                        }

                        // Remove last / from URL
                        if (visitURL.endsWith("/"))
                        {
                            visitURL = visitURL.substring(0, visitURL.length() - 1);
                        }
                        try
                        {
                            URL VisitUrl = new URL(visitURL);
                            URLConnection ConnectVisitURL = VisitUrl.openConnection();
                            if (ConnectVisitURL.getContent() != null) {
                                if (ConnectVisitURL.getContentType().matches(".*\\bhtml\\b.*")) {
                                    if (!URLstobeVisited.contains(visitURL) && !robots.contains(visitURL) && !completedURLs.contains(visitURL)) //Duplication condition
                                    {
                                        this.addTOToBeCrawled(visitURL);
                                        System.out.println("Site added for crawling:  " + visitURL);
                                    }
                                    if (!pageLinks.contains(visitURL))
                                    {
                                        pageLinks.add(visitURL);
                                        System.out.println("Added to page links:  " + visitURL);
                                    }
                                }
                            }
                        }
                        catch (IOException IOEx)
                        {
                            System.out.println("Not added to page links: IO Exception");
                        }
                    }
                }
                this.addTOCrawled(htmlDoc.title(), nextURL, content, pageLinks); //Add to crawled pages in database after crawling
            }
        }
    }

    @Override
    public void run() { crawl(); }

}

