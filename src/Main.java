import java.net.URL;
import java.util.ArrayList;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Crawler testCrawler = new Crawler();
        URL testURL = new URL("https://www.vox.com/");

        try
        {
            ArrayList<String> disallows = new ArrayList<String>();
            disallows = testCrawler.getDisallows(testURL.toString());
            for (int i = 0; i < disallows.size(); i++)
            {
                System.out.println(disallows.get(i) + '\n');
            }
        }
        catch(Exception e)
        {
            System.out.println("Null");
        }
    }
}

