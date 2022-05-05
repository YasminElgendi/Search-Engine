import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;


public class Crawler implements Runnable
{
    //Root URL
    String root;
    // URLs that has been visited and processed
    private static final HashSet<String> completedURLs = new HashSet<String>();
    //URLs to visit and process
    private static final Queue<String> URLstobeVisited = new LinkedList<String>();
    //Set of disallowed linkes per page
    private static  ArrayList<String> robots = new ArrayList<String>();

    //Maximum number of pages to be crawled
    private static final int Limit = 5000;
    //Database of crawler
    Database database = new Database();

    public Crawler(){}

    public void addTOCrawled(String url) {
        synchronized (this.database)
        {
            try
            {
                int result = database.insert_CrawledWebsites(url);
                if (result == 1)
                {
                	this.completedURLs.add(url);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void addTOtobeCrawled(String url) {

        synchronized (this.database) {
        	
            
	        try {
	            	int result = database.insert_toBeCrawledWebsites(url);
	                if (result == 1)
	                {
	                    this.URLstobeVisited.add(url);
	                }
	                
	            }
	        catch (Exception e)
	        {
	                e.printStackTrace();
	        }
       }
        
    }

    public static String downloadHTML(URL url)
    {
        try
        {
            URLConnection connect = url.openConnection();
            if(connect.getContentType().matches(".*\\bhtml\\b.*"))
            {
                FileWriter htmlDoc = new FileWriter("htmlDocumnet.txt");
                String html;
                InputStreamReader inputStream = new InputStreamReader(connect.getInputStream());
                BufferedReader reader = new BufferedReader(inputStream);
                StringBuffer htmlPage = new StringBuffer();
                while((html = reader.readLine()) != null)
                {
                    htmlPage.append(html + '\n');
                }
                htmlDoc.write(htmlPage.toString());
                return htmlPage.toString();
            }
        }
        catch (IOException e)
        {
        }
        return null;
    }

    public static ArrayList<String> getDisallows(String url) throws IOException
    {
        ArrayList<String> disallow = new ArrayList<String>();
//        url = url + "/robots.txt";
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
        }catch(Exception e)
        {
        	
        }
        return disallow;
    }

    public static int getCompletedSize()
    {
        synchronized (completedURLs)
        {
            return completedURLs.size();
        }
    }

    public static int gettobeCrawledSize()
    {
        synchronized (URLstobeVisited)
        {
            return URLstobeVisited.size();
        }
    }


    public static URL clean(URL url)
    {
        try
        {
            return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), null).toURL();
        }
        catch (MalformedURLException | URISyntaxException e)
        {
            return url;
        }
    }

    public void crawl () throws IOException
    {
        String nextURL = null;
        synchronized (this.database) {
            try {
                
            	nextURL = (String) database.retrieveNextToBeCrawled();
                if (nextURL == null) {
                    System.out.println(Thread.currentThread().getName() + " sleep\n");
                    // this.pages_to_visit.wait();
                }
                database.delete_toBeCrawledWebsites(nextURL);
                
            } catch (Exception e) {
                // System.out.println("error1");
            }
        }
        while(completedURLs.size() < 100)
        {
            URLstobeVisited.add(nextURL);
            //addTOtobeCrawled(nextURL);
            
            BufferedReader br = null;
            while(!URLstobeVisited.isEmpty())
            {
                String crawledURL = URLstobeVisited.poll();
                completedURLs.add(crawledURL);
                addTOCrawled(crawledURL);
                System.out.println("\n ********Site Crawled:  "+crawledURL +"******");

                boolean ok = false;
                URL url = null;

                while(!ok)
                {
                    try
                    {
                        url = new URL(crawledURL);
                        br = new BufferedReader(new InputStreamReader(url.openStream()));
                        ok = true;
                    }
                    catch(MalformedURLException e)
                    {
                        System.out.println("****Malformed URL:  "+ crawledURL +"  ****");
                        completedURLs.remove(crawledURL);
                        database.delete_CrawledWebsites(crawledURL);
                        crawledURL = URLstobeVisited.poll();
                        ok = false;
                    }
                    catch(IOException ioe)
                    {
                        System.out.println("****IOException in URL:  "+ crawledURL +"  ****");
                        completedURLs.remove(crawledURL);
                        database.delete_CrawledWebsites(crawledURL);
                        crawledURL = URLstobeVisited.poll();
                        ok = false;
                    }
                }

                StringBuilder s = new StringBuilder();
                String tmp = null;
                
                while((tmp = br.readLine()) != null)
                {
                    s.append(tmp);
                }

                tmp = s.toString();

                Pattern pattern = Pattern.compile("http[s]*://(\\w+\\.)*(\\w+)");
                Matcher matcher = pattern.matcher(tmp);

                try
                {
                    robots = getDisallows(crawledURL);
                }
                catch(IOException e)
                {
                	System.out.println("robots bayza y3m");
                }
                
                while(matcher.find())
                {
                  
                    String x = matcher.group();
                    if(!URLstobeVisited.contains(x)&& !robots.contains(x))
                    {
                    	// && !robots.contains(x)
                        URLstobeVisited.add(x);
                        addTOtobeCrawled(x);
                        System.out.println("Site added for crawling:  " + x);
                        

                    }
                }
            }

            if(br!=null)
            {
                br.close();
            }
        }

    }

    public static void results()
    {
        System.out.println("\n ***********Results************");
        System.out.println(" Websites Crawled: " + completedURLs.size()+"\n");

        for(String x : completedURLs)
        {
            System.out.println(x);
        }
    }

    @Override
    public void run()
    {
    	try
    	{
            crawl();
    	}
    	catch(IOException e)
    	{
    		System.out.println("bayz y3m");
    	}
    }
    
  public static void main(String[] args)
  {
	  Thread koko = new Thread (new Crawler());
	  koko.setName("koko");
	  koko.start();
  }

}
