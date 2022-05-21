package com.ram.Results;

import java.util.Scanner;

public class MainTest
{
    public static void main(String[] args)
    {
        System.out.println("Enter Search Query");
        Scanner QueryScanner = new Scanner(System.in);
        String Query = QueryScanner.nextLine();
        QueryProcessor SearchQuery = new QueryProcessor(Query);
    }
}
