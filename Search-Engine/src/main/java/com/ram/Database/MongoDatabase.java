package com.ram.Database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import java.io.*;
import java.util.*;

public class MongoDatabase
{
    public static final int LIMIT = 5000; //Maximum number of crawled pages
    MongoCollection<Document> CrawledCollection; //Collection to store pages that have been crawled
    MongoCollection<Document> IndexedPagesCollection; //Collection to store pages that have been indexed
    MongoCollection<Document> InvertedIndexCollection; //Inverted index collection
    MongoCollection<Document> ToBeCrawledCollection; //Collection to store the links to be crawled

    public MongoDatabase(String DatabaseName)
    {
        try
        {
            // Database server connection string creation
            String DB_URI = System.getenv("DB_URI") == null ? "mongodb://localhost:27017/" : System.getenv("DB_URI");
            ConnectionString connString = new ConnectionString(DB_URI);

            // Build Database settings
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .retryWrites(true)
                    .build();

            // Database connection to server
            MongoClient mongoClient = MongoClients.create(settings);

            // Create the database if it does not exist or connect to existing one
            com.mongodb.client.MongoDatabase database = mongoClient.getDatabase(DatabaseName);

            // Create all collections
            CrawledCollection = database.getCollection("CrawledPages");
            ToBeCrawledCollection = database.getCollection("ToBeCrawled");
            IndexedPagesCollection = database.getCollection("IndexedPages");
            InvertedIndexCollection = database.getCollection("InvertedIndex");

            //Make sure we are connected to the database
            System.out.println("Connected to database successfully");

        }
        catch (Exception e)
        {
            System.out.println("Failed to connect to database");
            e.printStackTrace();
        }
    }

    //Return the connected database instance
    public MongoDatabase getConnectedDatabase()
    {
        return this;
    }

    /*Checks crawler state whether to:
     * Start crawling from beginning
     * Or continue crawling
     */
    public void checkCrawlerState()
    {
        //Check if crawled document in database reached 5000 --> Crawling was done before
        if(CrawledCollection.countDocuments() >= LIMIT)
        {
            System.out.println("Crawler was done before starting from the beginning");
            CrawledCollection.drop();
            ToBeCrawledCollection.drop();
        }

        // If starting crawler from the beginning
        if((int) ToBeCrawledCollection.countDocuments() == 0 && CrawledCollection.countDocuments() < LIMIT)
        {
            //Crawler hasn't started before
            try {
                FileReader seeds = new FileReader("SeedsSet.txt"); // Reads from file seed links and add to to be crawled list
                BufferedReader bufferedReader = new BufferedReader(seeds);  //creates a buffering character input stream
                String line;
                while((line = bufferedReader.readLine()) != null)
                {
                    System.out.println(line);
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
            //Crawler was interrupted while running before
            System.out.println("Continue crawling...");
        }
    }

    /******************************Functions that handle ToBeCrawled collection******************************/
    // Insert new link to to be crawled collection in database
    public void insertToBeCrawledWebsites(String URL)
    {
        Document document = new Document("URL", URL);
        ToBeCrawledCollection.insertOne(document);
    }

    //Get a link from to to be crawled collection in database
    public FindIterable<Document> retrieveNextToBeCrawled(String URL)
    {
        return ToBeCrawledCollection.find(new Document("URL", URL));
    }

    //Pops a link from to be crawled collection in database
    public Document retrieveAndDeleteNextToBeCrawled()
    {
        return ToBeCrawledCollection.findOneAndDelete(new Document());
    }

    //Returns all links from to be crawled collection in database
    public Queue<String> getToBeCrawledList()
    {
        Queue<String> ToBeCrawledFromDatabase = new LinkedList<>();
        FindIterable<Document> LinksDocs = this.ToBeCrawledCollection.find(); //Retrieves all documents to be crawled

        //Iterates on all documents to extract links and add to array
        for (Document LinkDoc : LinksDocs)
        {
            ToBeCrawledFromDatabase.add((String) LinkDoc.get("URL"));
        }
        return ToBeCrawledFromDatabase;
    }

    //Given a url it deletes the corresponding document from the database
    public void deleteFromToBeCrawled(String URL)
    {
        ToBeCrawledCollection.deleteOne(Filters.eq("URL", URL));
    }

    /******************************Functions that handle CrawledPages collection******************************/
    //Inserts a crawled document into the Crawled collection in database
    public void insertCrawledWebsites(int id, String title , String URL, String content, ArrayList<String> PageLinks)
    {
        Document document = new Document("ID", id).append("Title", title).append("URL", URL).append("Content", content).append("PageLinks", PageLinks);
        CrawledCollection.insertOne(document);
    }
    // Retrieves all crawled documents from the database
    public FindIterable<Document> retrieveAllCrawled()
    {
        return CrawledCollection.find(new Document());
    }

    // Returns number of crawled documents from the database
    public int getCrawledCount()
    {
        return (int)CrawledCollection.countDocuments();
    }

    //Returns the links from the crawled documents from the database
    public HashSet<String> getCrawledLinksFromDatabase ()
    {
        HashSet<String> CrawledList = new HashSet<>();
        FindIterable<Document> CrawledDocs = retrieveAllCrawled();
        for (Document CrawledDoc : CrawledDocs)
        {
            CrawledList.add(CrawledDoc.getString("URL"));
        }

        return CrawledList;
    }

    public String getTitleOfPage(String URL)
    {
        FindIterable<Document> document =  CrawledCollection.find(new Document("URL", URL));
        String Title = "";
        for (Document doc : document)
        {
            Title = (String) doc.get("Title");
        }

        return Title;
    }

    public String getContentOfPage(String URL)
    {
        FindIterable<Document> document =  CrawledCollection.find(new Document("URL", URL));
        String Content = "";
        for (Document doc : document)
        {
            Content = (String) doc.get("Content");
        }

        return Content;
    }

    public boolean checkIfSameContent(String content)
    {
        FindIterable<Document> ContentDocs =  CrawledCollection.find();
        for (Document ContentDoc : ContentDocs)
        {
            if(ContentDoc.getString("Content").equals(content))
                return true;
        }
        return false;
    }

    //Searches the database for a document from crawled collection given the url
    public FindIterable<Document> getSearchDocument(String URL)
    {
        return CrawledCollection.find(new Document("URL", URL));
    }

    //Given the document's url it deletes it from the database
    public void deleteFromCrawled(String URL)
    {
        CrawledCollection.deleteOne(Filters.eq("URL", URL));
    }

    /**********************************Indexer Functions**********************************/

    //Checks if the url has been indexed before
    public boolean isIndexed(String URL)
    {
        return IndexedPagesCollection.find(new Document("URL", URL)).iterator().hasNext();

    }

    //Inserts a new url to indexed pages collection
    public void insertIndexedPage(String URL)
    {
        Document document = new Document("URL", URL);
        IndexedPagesCollection.insertOne(document);
    }

    //Inserts a new document into the inverted index collection
    public void insertIndexedWords(String word, List<Document> pagesList)
    {
        Document indexerDocument = new org.bson.Document("Word", word)
                .append("IDF", Math.log(CrawledCollection.countDocuments() / (float) pagesList.size()))
                .append("Pages", pagesList);
        InvertedIndexCollection.insertOne(indexerDocument);
    }

    //Insert new document in inverted index array of documents of word
    public void insertNewDocumentInIndexed(String word, Document document)
    {
        FindIterable<Document> WordDocument = InvertedIndexCollection.find(new Document("Word", word));
        Document UniqueDocument = new Document();
        for (Document doc : WordDocument) { UniqueDocument = doc;}
        List<Document> pagesList = new LinkedList<>();
        pagesList.add(document);
        if(UniqueDocument.size() == 0)
        {
            //Insert new word
            Document indexerDocument = new Document("Word", word)
                    .append("IDF", Math.log(CrawledCollection.countDocuments() / (float) pagesList.size()))
                    .append("Pages", pagesList);
            InvertedIndexCollection.insertOne(indexerDocument);
        }
        else
        {
            //Add document to array of documents of word
            InvertedIndexCollection.findOneAndUpdate(Filters.eq("Word", word), Updates.pushEach("Pages",pagesList));
        }
    }

    //Searches the inverted index and returns all documents in the array
    public List<Document> getDocumentsOfWord(String SearchWord)
    {
        FindIterable<Document> wordDocument =  InvertedIndexCollection.find(Filters.eq("Word",SearchWord));
        List<Document> pages = new ArrayList<>();
        for (Document document : wordDocument)
        {
            pages =  document.getList("Pages", Document.class);
        }
        return pages;
    }

    //Returns IDF of a given word from inverted index collection
    public double getWordIDF(String word)
    {
        FindIterable<Document> WordDocs = InvertedIndexCollection.find(new Document("Word", word));
        double IDF = 0;
        for (Document WordDoc : WordDocs)
        {
            IDF = (double) WordDoc.get("IDF");
        }

        return IDF;
    }

    //Updates IDF for a certain word after database is updated
    public void updateWordIDF(String word)
    {
        int wordPagesSize = InvertedIndexCollection.find(new Document("Word", word)).iterator().next()
                .getList("Pages", Document.class).size();

        InvertedIndexCollection.updateOne(new Document("Word", word),
                new Document("$set",
                        new Document("IDF", Math.log(CrawledCollection.countDocuments() / (float) wordPagesSize))));
    }

    //Updates IDF for all words in the inverted index collection
    public void updateIDFForAllWords()
    {
        FindIterable<Document> nextWord = InvertedIndexCollection.find();
        for (Document doc : nextWord)
        {
            String word = doc.getString("Word");
            updateWordIDF(word);
        }
    }

    //Returns all links that point to a given url --> for calculating page popularity
    public ArrayList<String> getIngoingLinks(String URL)
    {
        String[] values = {URL};
        ArrayList<String> IngoingLinks = new ArrayList<>();
        FindIterable<Document> LinksDocs = this.CrawledCollection.find(
                Filters.in(
                        "PageLinks",
                        Arrays.asList(values)
                )).projection(Projections.fields(Projections.include("URL")));

        for (Document LinksDoc : LinksDocs)
        {
            IngoingLinks.add((String) LinksDoc.get("URL"));
        }

        return IngoingLinks;
    }

    //Returns all links in a certain page from crawled collection
    public List<String> getPageLinks(String URL) {
        List<String> PageLinks = new ArrayList<>();
        FindIterable<Document> PageLinksDoc = CrawledCollection.find(new Document("URL", URL));

        for (Document doc : PageLinksDoc) {
            PageLinks = doc.getList("PageLinks", String.class);
        }

        return PageLinks;
    }
}



