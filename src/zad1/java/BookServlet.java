package zad1.java;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BookServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init() throws ServletException {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/ksidb");
        } catch (NamingException e) {
            throw new ServletException("Nie mogę uzyskać źródła danych", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serviceRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serviceRequest(req, resp);
    }

    private void serviceRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<h2>Lista dostępnych książek</h2>");

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pozycje")) {

            out.println("<ol>");
            while (rs.next()) {
                String tytul = rs.getString("tytul");
                float cena = rs.getFloat("cena");
                out.println("<li>" + tytul + " - cena: " + cena + "</li>");
            }
            out.println("</ol>");
        } catch (SQLException e) {
            out.println("Błąd podczas uzyskiwania danych: " + e.getMessage());
        } finally {
            out.close();
        }
    }
}
