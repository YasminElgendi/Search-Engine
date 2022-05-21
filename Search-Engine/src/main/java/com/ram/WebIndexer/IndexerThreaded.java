package com.ram.WebIndexer;

import com.ram.Database.MongoDatabase;

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

//      Wait until all the threads finish indexing
        for (Thread thread : threads)
        {
            thread.join();
        }

    }
}
