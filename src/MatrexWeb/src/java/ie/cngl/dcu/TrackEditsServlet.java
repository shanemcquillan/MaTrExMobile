/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ie.cngl.dcu;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.net.URLDecoder;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author shane
 */
public class TrackEditsServlet extends HttpServlet {

    private String databaseURL;

    @Override
    public void init(ServletConfig config) throws ServletException {
        databaseURL = config.getInitParameter("databaseURL");
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        Connection con = null;
        Statement stmt = null;

        //Connect to translations database
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionUrl = "jdbc:mysql://" + databaseURL + "/TranslationsDB?" +
                                        "user=root&password=";
            System.err.println(connectionUrl);
            con = DriverManager.getConnection(connectionUrl);
        } catch (SQLException e) {
            System.err.println("SQL Exception: "+ e.toString());
        } catch (ClassNotFoundException cE) {
            System.err.println("Class Not Found Exception: "+ cE.toString());
        }

        //Expected parameters
        String editid = request.getParameter("editid");
        String transid = request.getParameter("transid");
        String text = request.getParameter("text");
        text = URLDecoder.decode(text, "utf8");

        try {
            //Adding translation info to database
            String updateInfo = "INSERT INTO postedits(editid, transid, text) VALUES(" + Integer.parseInt(editid) + ", "
                                    + Integer.parseInt(transid) + ", " + "\'" + StringEscapeUtils.escapeSql(text) + "\')";

            try {
              stmt = con.createStatement();
              stmt.executeUpdate(updateInfo);
            } catch (SQLException e) { System.err.println("SQL ERROR: " + e.getErrorCode() + ", could not update table."); }

            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) { System.err.println("SQL ERROR: " + e.getErrorCode()); }
        } catch (Exception ex) { }

        //There is no response to this action

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
