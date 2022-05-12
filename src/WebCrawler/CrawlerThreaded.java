package WebCrawler;

import Database.MongoDatabase;

import java.io.IOException;
import java.util.Scanner;

public class CrawlerThreaded
{
    public static void main(String[] args) throws InterruptedException
    {
        // Ask user fo the number of threads
        Scanner input = new Scanner(System.in);
        System.out.println("Enter the number of threads you want to run:");
        int NumberOfThreads = input.nextInt();
        input.close();

        // if the entered number of threads is invalid, then make it equal to 1.
        if (NumberOfThreads < 1)
        {
            System.out.println("Invalid number of threads");
            System.out.println("Running the Crawler in a Single thread");
            NumberOfThreads = 1;
        }

        Crawler crawler = new Crawler();

        // Create N threads which will crawl the URLs
        Thread[] threads = new Thread[NumberOfThreads];
        for (int i = 0; i < NumberOfThreads; i++) {
            threads[i] = new Thread(crawler);
            threads[i].setName("Crawler " + (i + 1));
            threads[i].start();
        }

        // Wait until all the threads finish crawling
        for (Thread thread : threads)
        {
            thread.join();
        }

        System.out.println("Finished Crawling Successfully!");

    }
}
