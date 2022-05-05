package WebCrawler;

import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.PreparedStatement;  
import java.sql.SQLException;  
import java.sql.ResultSet;  
import java.sql.Statement;  

   


public class Connect {
	
	 /** 
     * Connect to a sample database 
     */  
    
    public Connection connect() {  
        // SQLite connection string  
        String url = "jdbc:sqlite:E:\\Gam3a\\2nd year CE\\2nd term\\Advanced Programming Techniques\\Project\\moi\\Database\\SearchEngine.db";  
        
        Connection conn = null;  
        try {  
            conn = DriverManager.getConnection(url);  
            System.out.println("Connection to SQLite has been established.");  

        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
        return conn;  
    }  
   
    public void insert_toBeCrawledWebsites(String URL, int id) {  
        String sql = "INSERT INTO toBeCrawled(URL, id) VALUES(?,?)";  
   
        try{  
            Connection conn = this.connect();  
            PreparedStatement pstmt = conn.prepareStatement(sql);  
            pstmt.setString(1, URL);  
            pstmt.setInt(2, id);  
            pstmt.executeUpdate();  
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
    }  
    
    public void insert_CrawledWebsites(String URL, int HASHVALUE) {  
        String sql = "INSERT INTO CrawledWebsites(URL, HASHVALUE) VALUES(?,?)";  
   
        try{  
            Connection conn = this.connect();  
            PreparedStatement pstmt = conn.prepareStatement(sql);  
            pstmt.setString(1, URL);  
            pstmt.setInt(2, HASHVALUE);  
            pstmt.executeUpdate();  
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
    }  
    
    public void selectAll(){  
        String sql = "SELECT * FROM crawledWebsites";  
          
        try {  
            Connection conn = this.connect();  
            Statement stmt  = conn.createStatement();  
            ResultSet rs    = stmt.executeQuery(sql);  
              
            // loop through the result set  
            while (rs.next()) {  
                System.out.println(rs.getString("URL") +  "\t" +   
                                   rs.getInt("HASHVALUE") + "\t");
            }  
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
    }  
    /** 
     * @param args the command line arguments 
     */  
    public static void main(String[] args) {  
    	Connect app = new Connect();  
        app.selectAll();  
       
    }  
}  


