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
import java.sql.PreparedStatement;

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
        String searchTitle = req.getParameter("title");
        String searchAuthor = req.getParameter("author");

        out.println("<h2>Lista książek</h2>");
        out.println("<form method='GET'>");
        out.println("Tytuł: <input type='text' name='title' />");
        out.println("Autor: <input type='text' name='author' />");
        out.println("<input type='submit' value='Szukaj' />");
        out.println("</form>");

        try (Connection con = dataSource.getConnection()) {
            String query = getQuery(searchTitle, searchAuthor);

            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                int paramIndex = 1;
                if (searchTitle != null && !searchTitle.isEmpty()) {
                    pstmt.setString(paramIndex++, "%" + searchTitle + "%");
                }
                if (searchAuthor != null && !searchAuthor.isEmpty()) {
                    pstmt.setString(paramIndex, "%" + searchAuthor + "%");
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    out.println("<table border='1'>");
                    out.println("<tr><th>ISBN</th><th>Autor</th><th>Tytuł</th><th>Rok</th><th>Wydawca</th><th>Cena</th></tr>");
                    while (rs.next()) {
                        String isbn = rs.getString("ISBN");
                        String tytul = rs.getString("TYTUL");
                        String autor = rs.getString("AUTOR_NAME");
                        String wydawca = rs.getString("WYDAWCA_NAME");
                        int rok = rs.getInt("ROK");
                        float cena = rs.getFloat("CENA");
                        out.println("<tr><td>" + isbn + "</td><td>" + autor + "</td><td>" + tytul + "</td><td>" + rok + "</td><td>" + wydawca + "</td><td>" + cena + "</td></tr>");
                    }
                    out.println("</table>");
                }
            }
        } catch (SQLException e) {
            out.println("Błąd podczas uzyskiwania danych: " + e.getMessage());
        } finally {
            out.close();
        }
    }

    private static String getQuery(String searchTitle, String searchAuthor) {
        String query = "SELECT POZYCJE.ISBN, POZYCJE.TYTUL, POZYCJE.CENA, POZYCJE.ROK, AUTOR.NAME as AUTOR_NAME, WYDAWCA.NAME as WYDAWCA_NAME " +
                "FROM POZYCJE " +
                "JOIN AUTOR ON POZYCJE.AUTID = AUTOR.AUTID " +
                "JOIN WYDAWCA ON POZYCJE.WYDID = WYDAWCA.WYDID";

        if (searchTitle != null && !searchTitle.isEmpty() || searchAuthor != null && !searchAuthor.isEmpty()) {
            query += " WHERE";
            boolean addAnd = false;
            if (searchTitle != null && !searchTitle.isEmpty()) {
                query += " POZYCJE.TYTUL LIKE ?";
                addAnd = true;
            }
            if (searchAuthor != null && !searchAuthor.isEmpty()) {
                if (addAnd) {
                    query += " AND";
                }
                query += " AUTOR.NAME LIKE ?";
            }
        }
        return query;
    }
}
