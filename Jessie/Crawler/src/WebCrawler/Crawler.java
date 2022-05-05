package WebCrawler;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Crawler {

	public static Queue<String> queue = new LinkedList<String>();
	public static Set<String> crawled = new HashSet<String>();
	public static Set<String> tobeCrawled = new HashSet<String>();
	public static String regex = "http[s]*://(\\w+\\.)*(\\w+)";
	
	public static void getLinks (String root) throws IOException
	{
		queue.add(root);
		BufferedReader br = null;
		while(!queue.isEmpty())
		{
			String crawledURL = queue.poll();
			crawled.add(crawledURL);
			System.out.println("\n ********Site Crawled:  "+crawledURL +"******");
			
			if(crawled.size() >100)
			{
				return;
			}
			
			boolean ok = false;
			URL url = null;
		
		
			while(!ok)
			{
				try
				{
					url = new URL(crawledURL);
					br = new BufferedReader(new InputStreamReader(url.openStream()));
					ok = true;
				}catch(MalformedURLException e)
				{
					System.out.println("****Malformed URL:  "+ crawledURL +"  ****");
				}
				catch(IOException ioe)
				{
					System.out.println("****IOException in URL:  "+ crawledURL +"  ****");
					crawled.remove(crawledURL);
					crawledURL = queue.poll();
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
			
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(tmp);
			
			while(matcher.find())
			{
				String x = matcher.group();
				if(!tobeCrawled.contains(x))
				{
					tobeCrawled.add(x);
					System.out.println("Site added for crawling:  " + x);
					queue.add(x);
				}
			}
		}
		if(br!=null)
		{
			br.close();
		}
	}
	
	public static void results()
	{
		System.out.println("\n ***********Results************");
		System.out.println(" Websites Crawled: " +crawled.size()+"\n");
		
		for(String x:crawled)
		{
			System.out.println(x);
		}
	}
	
	public static void main(String args[]) throws Exception
	{
		try
		{
			getLinks("https://www.wikipedia.org/");
			results();
		}
		catch(IOException ioe)
		{
			
		}
	}
	
}
