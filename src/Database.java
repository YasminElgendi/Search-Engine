import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Database
{
    private Connection conn = null;
    private static int hash1 = 0;
    private static int hash2 = 11;
    /**
     * Connect to a sample database
     */
    public Database()
    {
         conn = this.connect();
    }

    public Connection connect()
    {
        // SQLite connection string
        String url = "jdbc:sqlite:C:\\Users\\yazmi\\OneDrive\\Desktop\\Uni\\Second Year\\Second Semester\\APT\\Project\\Search_Engine\\Search-Engine\\Jessie\\Database\\SearchEngine.db";

        try
        {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");

        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public int insert_toBeCrawledWebsites(String URL) {
        String sql = "INSERT INTO toBeCrawled(URL, id) VALUES(?,?)";
        try
        {
            hash2++;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, URL);
            pstmt.setInt(2, hash2);
            pstmt.executeUpdate();
            return 1;
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public int insert_CrawledWebsites(String URL)
    {
        String sql = "INSERT INTO CrawledWebsites(URL, HASHVALUE) VALUES(?,?)";

        hash1++;
        try
        {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, URL);
            pstmt.setInt(2, hash1);
            pstmt.executeUpdate();
            return 1;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public HashSet<String> retrieveAllCrawled()
    {
        String sql = "SELECT * FROM crawledWebsites";
        HashSet<String> URLsCrawled = new HashSet<String>();
        try
        {
//            Connection conn = this.connect();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next())
            {
                URLsCrawled.add(rs.getString("URL"));
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return URLsCrawled;
    }

    public Queue<String> retrieveAllToBeCrawled()
    {
        String sql = "SELECT * FROM toBeCrawled";
        Queue<String> URLstobeVisited = new LinkedList<String>();

        try {
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next())
            {
                URLstobeVisited.add(rs.getString("URL"));
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return URLstobeVisited;

    }
    public String retrieveNextToBeCrawled()
    {
    	String sql = "SELECT * FROM toBeCrawled limit 1";
    	String url = null;
    	try {
    		//Connection conn = this.connect(); 
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);

                url = rs.getString("URL");
                System.out.println(url);
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    	
    	return url;
    	
    }
    public int delete_toBeCrawledWebsites(String URL) {  
        String sql = "DELETE FROM toBeCrawled WHERE URL = ?";  
   
        try{  
            //Connection conn = this.connect();  
            PreparedStatement pstmt = conn.prepareStatement(sql);  
            pstmt.setString(1, URL);  
            pstmt.executeUpdate();  
            return 1;
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
        return -1;
    }  
    public int delete_CrawledWebsites(String URL) {  
        String sql = "DELETE FROM CrawledWebsites WHERE URL=(?)";  
   
        try{  
            //Connection conn = this.connect();  
            PreparedStatement pstmt = conn.prepareStatement(sql);  
            pstmt.setString(1, URL);    
            pstmt.executeUpdate();  
            return 1;
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
        return -1;
    }  

    /**
     * @param args the command line arguments
     */
//    public static void main(String[] args)
//    {
//        Database app = new Database();
//        app.retrieveNextToBeCrawled();
//        app.delete_toBeCrawledWebsites("https://www.nationalgeographic.com/");
//        System.out.println(app.retrieveAllToBeCrawled());
//
//    }
}
