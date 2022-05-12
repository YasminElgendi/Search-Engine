package WebCrawler;

import Database.MongoDatabase;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler implements Runnable
{
    private static final int LIMIT = 5000;
    private static final HashSet<String> completedURLs = new HashSet<String>();
    private static final Queue<String> URLstobeVisited = new LinkedList<String>();
    public static final HashMap<String,ArrayList<String>> pagesLinks = new HashMap<String,ArrayList<String>>();
    MongoDatabase Database;
    static int id;

    public Crawler()
    {
        Database = new MongoDatabase("SearchEngineDatabase");
        id = Database.getCrawledCount();
        Database.checkCrawlerState();
    }

    public void addTOCrawled(String title, String URL, String content)
    {
        synchronized (this.Database)
        {
            try
            {
                synchronized (this)
                {
                    id++;
                }
                Database.insertCrawledWebsites(id ,title, URL, content);
                completedURLs.add(URL);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void addTOToBeCrawled(String URL)
    {
        synchronized (this.Database)
        {
            try
            {
                Database.insertToBeCrawledWebsites(URL);
                URLstobeVisited.add(URL);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getDisallows(String url) throws IOException
    {
        ArrayList<String> disallow = new ArrayList<String>();
        URLConnection connect = new URL(url + "/robots.txt").openConnection();
        String robotsFile, line;
        InputStreamReader inputStream = new InputStreamReader(connect.getInputStream());
        BufferedReader reader = new BufferedReader(inputStream);
        StringBuffer Robots = new StringBuffer();
        try
        {
            while((line = reader.readLine()) != null)
            {
                if (line.contains("User-agent:"))
                {
                    if(line.contains("*"))
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

    public void crawl() throws IOException, MalformedURLException
    {
        while (Database.getCrawledCount() < LIMIT)
        {
            System.out.println(Database.getCrawledCount());
            Document document = Database.retrieveAndDeleteNextToBeCrawled();
            if(document != null)
            {
                String nextURL = document.getString("URL");
                if(nextURL == null || nextURL == "")
                {
                    continue;
                }

                try
                {
                    URL url = new URL(nextURL);
                    URLConnection connect = url.openConnection();
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                    if(connect.getContentType() != null)
                    {
                        if(connect.getContentType().matches(".*\\bhtml\\b.*")) {
                            org.jsoup.nodes.Document htmlDoc = Jsoup.connect(nextURL).get();

                            this.addTOCrawled(htmlDoc.title(), nextURL, htmlDoc.text());
                            System.out.println("\n ********Site Crawled:  " + nextURL + "******");


                            ArrayList<String> robots = new ArrayList<String>();
                            String html = htmlDoc.html();

                            Pattern pattern = Pattern.compile("http[s]*://(\\w+\\.)*(\\w+)");
                            Matcher matcher = pattern.matcher(html);

                            try
                            {
                                robots = getDisallows(nextURL);
                            }
                            catch (IOException e)
                            {
                                System.out.println("Error reading robots.txt");
                            }

                            while (matcher.find())
                            {
                                String visitURL = matcher.group();
                                if (!URLstobeVisited.contains(visitURL) && !robots.contains(visitURL) && !completedURLs.contains(visitURL))
                                {
                                    URLstobeVisited.add(visitURL);
                                    this.addTOToBeCrawled(visitURL);
                                    System.out.println("Site added for crawling:  " + visitURL);
                                    if (pagesLinks.containsKey(nextURL))
                                    {
                                        pagesLinks.get(nextURL).add(visitURL);
                                    }
                                    else
                                    {
                                        ArrayList<String> links = new ArrayList<>();
                                        links.add(visitURL);
                                        pagesLinks.put(nextURL, links);
                                    }
                                }
                            }
                        }
                    }
                }
                catch(MalformedURLException e)
                {
                    System.out.println("****Malformed URL:  "+ nextURL +"  ****");
                }
                catch(IOException ioe)
                {
                    System.out.println("****IOException in URL:  "+ nextURL +"  ****");
                }
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            crawl();
        }
        catch (IOException ioe)
        {

        }
    }

}
