/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ie.cngl.dcu;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.File;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author shane
 */
public class MatrexServlet extends HttpServlet {

    private String databaseURL;
    private String [] sources;
    private String [] targets;
    private String [] domains;
    private String [] ips;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        databaseURL = config.getInitParameter("databaseURL");
        String langPairsFile = config.getInitParameter("langPairsFile");

        File file = new File(getServletContext().getRealPath(langPairsFile));

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            //NodeList of all pair tags
            NodeList nodeLst = doc.getElementsByTagName("pair");
            int numPairs = nodeLst.getLength();
            //Set the size of each array
            sources = new String[numPairs]; targets = new String[numPairs];
            domains = new String[numPairs]; ips = new String[numPairs];

            //Get the info from each pair an insert into arrays
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node fstNode = nodeLst.item(i);
                Element fstElmnt = (Element) fstNode;

                NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("source");
                Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                NodeList fstNm = fstNmElmnt.getChildNodes();
                sources[i] = ((Node) fstNm.item(0)).getNodeValue();

                NodeList scndNmElmntLst = fstElmnt.getElementsByTagName("target");
                Element scndNmElmnt = (Element) scndNmElmntLst.item(0);
                NodeList scndNm = scndNmElmnt.getChildNodes();
                targets[i] = ((Node) scndNm.item(0)).getNodeValue();

                NodeList trdNmElmntLst = fstElmnt.getElementsByTagName("domain");
                Element trdNmElmnt = (Element) trdNmElmntLst.item(0);
                NodeList trdNm = trdNmElmnt.getChildNodes();
                domains[i] = ((Node) trdNm.item(0)).getNodeValue();

                NodeList frtNmElmntLst = fstElmnt.getElementsByTagName("ip");
                Element frtNmElmnt = (Element) frtNmElmntLst.item(0);
                NodeList frtNm = frtNmElmnt.getChildNodes();
                ips[i] = ((Node) frtNm.item(0)).getNodeValue();
            }
        } catch (Exception e) { System.err.println("Could not read from xml file"); }
    }

    //Matrex service cannot process the vertical bar | symbol
    String vertBarRemover(String input) {
        return input.replace('|', ' ');
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        Connection con = null;
        Statement stmt = null;
        ResultSet result = null;

        //Connect to translations database
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionUrl = "jdbc:mysql://" + databaseURL + "/TranslationsDB?" +
                                   "user=root&password=";
            con = DriverManager.getConnection(connectionUrl);
        } catch (SQLException e) {
            System.err.println("SQL Exception: "+ e.toString() + ", couldn't connect to database.");
        } catch (ClassNotFoundException cE) {
            System.err.println("Class Not Found Exception: "+ cE.toString());
        }

        //Parameters expected from servlet call
        String input = request.getParameter("text");
        input = vertBarRemover(input);

        String inLang = request.getParameter("inl");
        String outLang = request.getParameter("outl");
        String subject = request.getParameter("subject");
        int transId = 0;

        String output = "";

        if (input != null) {
            try {
                input = URLDecoder.decode(input, "utf8");
                MatrexClient client = null;
                for(int i = 0; i < sources.length; i++)     //Order N - effecient as number of language pairs will likely never exceed 100.
                    if(sources[i].equals(inLang) && targets[i].equals(outLang) && domains[i].equals(subject))   //When all indexes match
                        client = new MatrexClient(ips[i]);  //We have the appropriate model

                if(client != null) {
                    client.init();
                    output = client.translate(input);

                    String numQuery = "SELECT COUNT(*) FROM translations";      //Number of rows, next is used as transID
                    try {
                      stmt = con.createStatement();
                      result = stmt.executeQuery(numQuery);
                      while(result.next())
                        transId = result.getInt(1);
                    } catch (SQLException e) {
                        System.err.println("SQL ERROR: " + e.getErrorCode() + ", could not calculate next translation ID.");
                    }

                    //Adding translation info to database
                    String insertInfo = "INSERT INTO translations(transId, time, date, source, target, domain, input, output, edits, timeInMS) "
                                            + "VALUES(" + transId + ", CURTIME(), CURDATE(), \'" + inLang + "\', \'" + outLang + "\', \'" + subject +
                                                "\', \'" + StringEscapeUtils.escapeSql(input) + "\', \'" + StringEscapeUtils.escapeSql(output) + "\', null, null)";
                                        //eg. "VALUES(31, 15:25, 12/2/11, 'english', 'french', 'sport', 'the referee', 'la arbitre', null, null)";
                                        //The nulls are reserved for post-editing
                    try {
                      stmt = con.createStatement();
                      stmt.executeUpdate(insertInfo);
                    } catch (SQLException e) { System.err.println("SQL ERROR: " + e.getErrorCode() + ", could not add translation information to database."); }
                }
                else {
                    if(inLang.equals(outLang))  //If source and target are the same simply return the input
                        output = input;
                    else
                        output = "Sorry, we do not have the appropriate language model for this translation.";
                }

                try {
                    if (con != null) 
                        con.close();
                } catch (SQLException e) { System.err.println("SQL ERROR: " + e.getErrorCode() + ", couldn't close connection to database."); }
            } catch (Exception ex) { }
        }

        out.println("<result><transid>" + transId + "</transid><translation>" + output + "</translation></result>");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
}
