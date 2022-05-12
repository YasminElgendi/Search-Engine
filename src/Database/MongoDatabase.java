package Database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class MongoDatabase
{
    public static final int LIMIT = 5000;
    MongoCollection<Document> CrawledCollection;
    MongoCollection<Document> IndexedPagesCollection;
    MongoCollection<Document> WordIndexerCollection;
    MongoCollection<Document> ToBeCrawledCollection;

   public MongoDatabase(String DatabaseName)
   {
        try
        {
            // Create the DB server connection string
            String DB_URI = System.getenv("DB_URI") == null ? "mongodb://localhost:27017/" : System.getenv("DB_URI");
            ConnectionString connString = new ConnectionString(DB_URI);

            // Build the DB server settings
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .retryWrites(true)
                    .build();

            // Connect to DB server
            MongoClient mongoClient = MongoClients.create(settings);

            // Create the DB
            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(DatabaseName);

            // Create the needed collections
            CrawledCollection = database.getCollection("CrawledPages");
            ToBeCrawledCollection = database.getCollection("ToBeCrawled");
            IndexedPagesCollection = database.getCollection("IndexedPages");
            WordIndexerCollection = database.getCollection("WordIndexer");


            System.out.println("Connected to database successfully");

        }
        catch (Exception e)
        {
            System.out.println("Failed to connect to database");
            e.printStackTrace();
        }
    }

    public void checkCrawlerState()
    {
        if(CrawledCollection.countDocuments() >= LIMIT)
        {
            System.out.println("Crawler was done before starting from the beginning");
            CrawledCollection.drop();
            ToBeCrawledCollection.drop();
        }

        if((int) ToBeCrawledCollection.countDocuments() == 0 && CrawledCollection.countDocuments() < LIMIT)
        {
            //Crawler hasn't started before
            try {
                FileReader seeds = new FileReader("SeedsSet.txt");
                BufferedReader bufferedReader = new BufferedReader(seeds);  //creates a buffering character input stream
                StringBuffer stringBuffer=new StringBuffer();    //constructs a string buffer with no characters
                String line;
                while((line = bufferedReader.readLine())!=null)
                {
                    Document url = new Document("URL", line);
                    ToBeCrawledCollection.insertOne(url);
                }
                bufferedReader.close();    //closes the stream and release the resources
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
        else
        {
            //Crawler was interrupted
            System.out.println("Resuming crawler...");
        }
    }

    /******************************Functions that handle ToBeCrawled collection******************************/
    public void insertToBeCrawledWebsites(String URL)
    {
        Document document = new Document("URL", URL);
        ToBeCrawledCollection.insertOne(document);
    }
    public FindIterable<Document> retrieveNextToBeCrawled(String URL)
    {
        return ToBeCrawledCollection.find(new Document("URL", URL));
    }

    public Document retrieveAndDeleteNextToBeCrawled()
    {
        return ToBeCrawledCollection.findOneAndDelete(new Document());
    }

    public FindIterable<Document> getFromToBeCrawled(String URL)
    {
        return ToBeCrawledCollection.find(new Document("URL", URL));
    }

    public FindIterable<Document> retrieveAllToBeCrawled()
    {
        return ToBeCrawledCollection.find(new Document());
    }

    public void deleteFromToBeCrawled(String URL)
    {
        ToBeCrawledCollection.deleteOne(Filters.eq("URL", URL));
    }

    /******************************Functions that handle CrawledPages collection******************************/
    public void insertCrawledWebsites(int id, String title ,String URL, String content)
    {
        Document document = new Document("ID", id).append("Title", title).append("URL", URL).append("Content", content);
        CrawledCollection.insertOne(document);
    }

    public FindIterable<Document> retrieveAllCrawled()
    {
        return CrawledCollection.find(new Document());
    }

    public int getCrawledCount()
    {
        return (int)CrawledCollection.countDocuments();
    }

    public void deleteFromCrawled(String URL)
    {
        CrawledCollection.deleteOne(Filters.eq("URL", URL));
    }

    /**********************************Indexer Functions**********************************/

    public boolean isIndexed(String URL)
    {
        return IndexedPagesCollection.find(new Document("URL", URL)).iterator().hasNext();

    }

    public void insertIndexedPage(String URL)
    {
        Document document = new Document("URL", URL);
        IndexedPagesCollection.insertOne(document);
    }

    public void insertIndexedWords(String word, List<Document> pagesList)
    {
            Document indexerDocument = new org.bson.Document("Word", word)
            .append("IDF", Math.log(CrawledCollection.countDocuments() / (float) pagesList.size()))
            .append("Pages", pagesList);
        WordIndexerCollection.insertOne(indexerDocument);
    }

    public void updateWordIDF(String word)
    {
        int wordPagesSize = WordIndexerCollection.find(new Document("Word", word)).iterator().next()
                .getList("Pages", Document.class).size();

        WordIndexerCollection.updateOne(new Document("Word", word),
                new Document("$set",
                        new Document("IDF", Math.log(CrawledCollection.countDocuments() / (float) wordPagesSize))));
    }

    public void updateIDFForAllWords()
    {
        Iterator nextWord = WordIndexerCollection.find().iterator();
        while (nextWord.hasNext())
        {
            updateWordIDF((String) nextWord.next());
        }
    }
}
