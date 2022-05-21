package com.ram.WebCrawler;

import com.ram.Database.MongoDatabase;

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

        // if numbers of thread less than one --> run on a single thread
        if (NumberOfThreads < 1)
        {
            System.out.println("Invalid number of threads");
            System.out.println("Running the Crawler using main thread only");
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
            System.out.println(thread.getName() + " joined");
            thread.join();
        }

        System.out.println("Finished Crawling Successfully!");

    }
}
