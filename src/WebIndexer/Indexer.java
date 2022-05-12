package WebIndexer;

import Database.MongoDatabase;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import ca.rmen.porterstemmer.PorterStemmer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Indexer implements Runnable
{
    //word -> [{URL1,TF, Positions},{URL2,TF, Positions},{URL3,TF, Positions},{URL4,TF, Positions},......]
    public static ArrayList<String> stoppingWords = new ArrayList<String>();
    HashMap<String, List<Document>> indexer = new HashMap<>();
    HashSet<String> indexed = new HashSet<>();
    MongoDatabase Database;
    MongoCursor<Document> cursor;
    public static Integer index;

    public Indexer(MongoDatabase database)
    {
        getStopWords();
        this.Database = database;
        this.cursor = Database.retrieveAllCrawled().iterator();
    }

    public boolean checkNextDocument()
    {
        synchronized (this.cursor)
        {
            return cursor.hasNext();
        }
    }

    public Document getNextDocument()
    {
        synchronized (this.cursor)
        {
            return cursor.next();
        }
    }

    public void Index(String url, String webPageContent) throws IOException {
        BitSet position;
        if(Database.isIndexed(url))
        {
            //Page already indexed check if updated to reindex
            return;
        }

        HashMap<String, Integer> eachWordCount = new HashMap<>();
        String [] contents = webPageContent.split(" ");
        int wordsCount = contents.length;

        for(int i = 0; i < contents.length ;i++)
        {
            String preStemmedWord = stemSpecialCharacters(contents[i]);
            preStemmedWord = preStemmedWord.toLowerCase();
            PorterStemmer porterStemmer = new PorterStemmer();
            String stemmedWord = porterStemmer.stemWord(preStemmedWord);

            if(!stoppingWords.contains(stemmedWord))
            {
                if (eachWordCount.containsKey(stemmedWord)) {
                    eachWordCount.put(stemmedWord, eachWordCount.get(stemmedWord) + 1);
                } else {
                    eachWordCount.put(stemmedWord, 1);
                }
            }
        }

        for (String word : eachWordCount.keySet())
        {
            position = getWordPositions(word,url);
            Document document = new Document();
            document.append("URL", url);
            document.append("TF", eachWordCount.get(word) / (float) wordsCount);
            document.append("Position",toInt(position));
            synchronized (this.indexer)
            {
                if (indexer.containsKey(word))
                {
                    indexer.get(word).add(document);
                }
                else
                {
                    List<Document> pagesContainingWord = new ArrayList<>();
                    pagesContainingWord.add(document);
                    indexer.put(word, pagesContainingWord);
                }
            }
        }

        synchronized (this.indexed)
        {
            indexed.add(url);
        }
    }

    @Override
    public void run()
    {
        while (checkNextDocument())
        {
            Document CurrentDocument = getNextDocument();
            String URL = CurrentDocument.getString("URL");
            String pageContent = CurrentDocument.getString("Content");

            System.out.println(Thread.currentThread().getName() + " indexing URL " + URL);
            try
            {
                Index(URL, pageContent);
            }
            catch (IOException ioe)
            {
                //Error in Index
                System.out.println("Error in index");
            }
        }
    }

    public void getStopWords()
    {
        try (FileReader inFile = new FileReader("StopWords.txt"))
        {
            StringBuffer sbuffer = new StringBuffer();
            while (inFile.ready()) {
                char c = (char) inFile.read();
                if (c == '\n') {
                    stoppingWords.add(sbuffer.toString());
                    sbuffer = new StringBuffer();
                } else {
                    sbuffer.append(c);
                }
            }
            if (sbuffer.length() > 0) {
                stoppingWords.add(sbuffer.toString());
            }
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading file");
        }
    }

    public String stemSpecialCharacters(String word)
    {
        try {
            int i = 0;
            while (i < word.length() && (word.charAt(i) < 'A'
                    || (word.charAt(i) > 'Z' && word.charAt(i) < 'a') || word.charAt(i) > 'z'))
                i++;

            if (i == word.length()) {
                char[] empty = new char[0];
                return new String(empty);
            }

            int j = word.length() - 1;
            while (word.charAt(j) < 'A' || (word.charAt(j) > 'Z' && word.charAt(j) < 'a')
                    || word.charAt(j) > 'z')
                j--;

            char[] charArr = new char[j - i + 1];
            int l = i;
            for (int k = 0; k < j - i + 1; k++) {
                charArr[k] = word.charAt(l);
                l++;
            }

            return new String(charArr);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    public static BitSet getWordPositions(String word, String url) throws IOException
    {
        BitSet positions = new BitSet(12);
        positions.clear();

        String tempString;
        org.jsoup.nodes.Document htmlDoc = Jsoup.connect(url).get();
        Elements title = htmlDoc.getElementsByTag("title");
        for (Element element : title)
        {
            tempString = element.toString().toLowerCase();
            if(tempString.contains(word))
            {
                positions.set(11);
            }
        }

            Elements h1 = htmlDoc.getElementsByTag("h1");
            for (Element element : h1)
            {
                tempString = element.toString().toLowerCase();
                if(tempString.contains(word))
                {
                    positions.set(10);
                }

            }

            Elements h2 = htmlDoc.getElementsByTag("h2");
            for (Element element : h2)
            {
                tempString = element.toString().toLowerCase();
                if(tempString.contains(word))
                {
                    positions.set(9);
                }

            }
            Elements h3 = htmlDoc.getElementsByTag("h3");
            for (Element element : h3)
            {
                tempString = element.toString().toLowerCase();
                if(tempString.contains(word))
                {
                    positions.set(8);
                }
            }

            Elements h4 = htmlDoc.getElementsByTag("h4");
            for (Element element : h4)
            {
                tempString = element.toString().toLowerCase();
                if(tempString.contains(word))
                {
                    positions.set(7);
                }

            }
            Elements h5 = htmlDoc.getElementsByTag("h5");
            for (Element element : h5)
            {
                tempString = element.toString().toLowerCase();
                if(tempString.contains(word))
                {
                    positions.set(6);
                }

            }
            Elements h6 = htmlDoc.getElementsByTag("h6");
            for (Element element : h6)
            {
                tempString = element.toString().toLowerCase();
                if(tempString.contains(word))
                {
                    positions.set(5);
                }

            }

            Elements paragraph = htmlDoc.getElementsByTag("p");
            for (Element element : paragraph)
            {
                tempString = element.toString().toLowerCase();
                if(tempString.contains(word))
                {
                    positions.set(4);
                }
            }
        return positions;
    }

    public static int toInt(BitSet bitSet) {
        int intValue = 0;
        for (int bit = 0; bit < bitSet.length(); bit++) {
            if (bitSet.get(bit)) {
                intValue |= (1 << bit);
            }
        }
        return intValue;
    }

    public void updateDataBase()
    {

        for (String word : indexer.keySet() )
        {
            Database.insertIndexedWords(word , indexer.get(word));
        }

        for(String url : indexed)
        {
            Database.insertIndexedPage(url);
        }
    }

}
