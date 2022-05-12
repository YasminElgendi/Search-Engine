package WebIndexer;

import Database.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.Iterator;

public class IndexerThreaded
{
    public static void main(String[] args) throws InterruptedException
    {
        MongoDatabase Database = new MongoDatabase("SearchEngineDatabase");
        Indexer testIndexer = new Indexer(Database);

        Thread[] threads = new Thread[10];

        for(int i = 0; i < 10; i++)
        {
            threads[i] = new Thread(testIndexer);
            threads[i].setName("Indexer " + (i + 1));
            threads[i].start();
        }

        // Wait until all the threads finish crawling
        for (Thread thread : threads)
        {
            thread.join();
        }

        System.out.println("Updating Indexer Database....");
        testIndexer.updateDataBase();

        Database.updateIDFForAllWords();
    }
}
