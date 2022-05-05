import java.sql.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class Indexer
{
    public static ArrayList<String> stoppingWords = new ArrayList<String>();

    public Indexer()
    {
        getStopWords();
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

    public static void main(String[] args)
    {

    }
}
