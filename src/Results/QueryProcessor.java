package Results;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class QueryProcessor extends HttpServlet
{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String searchValue = request.getParameter("SearchWord");
        String message = "Search Word is "+ searchValue;

        response.setContentType("text/html");

        response.sendRedirect("search.html");
    }

}
