package com.example.colocvbd;

import java.sql.*;
import java.io.*;

public class OrderJDBC {
    private String url = "jdbc:mysql://localhost:3306/agentie_imobiliara?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private String uid = "root";
    private String pw = "model123";
    private BufferedReader reader;
    private Connection con;

    public static void main(String[] args) {
        OrderJDBC app = new OrderJDBC();
        app.init();
        app.run();
    }

    private void init() {
        // Înregistrează driverul MySQL și realizează conexiunea
        try {
            // Încarcă driverul MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException: " + e);
        }

        // Inițializează conexiunea
        con = null;
        try {
            con = DriverManager.getConnection(url, uid, pw);
            System.out.println("Conexiune reușită la baza de date!");
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex);
            System.exit(1);
        }

        // Setează reader-ul pentru input
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    private void run() {
        String choice, sqlSt;
        choice = "1";
        while (!choice.equalsIgnoreCase("X")) {
            printMenu();
            choice = getLine();

            switch (choice) {
                case "1": // List all customers
                    sqlSt = "SELECT * FROM agentie;";
                    doQuery(sqlSt);
                    break;
                case "S": // List all customers
                    System.out.println("enter customer id:");
                    String cid = getLine();
                    sqlSt = "SELECT * FROM Customer where CustomerId = "+ cid +";";
                    doQuery(sqlSt);
                    break;
                case "2": // List all orders for a customer
                    System.out.print("Enter customer id: ");
                    String cusid = getLine();
                    sqlSt = "SELECT * FROM Orders WHERE  = '" + cusid + "' ;";
                    System.out.println(sqlSt);
                    doQuery(sqlSt);
                    break;
                case "3": // List all lineitems for an order
                    System.out.print("Enter order id: ");
                    String onum = getLine();
                    sqlSt = "SELECT * FROM OrderedProduct WHERE orderId = '" + onum + "';";
                    doQuery(sqlSt);
                    break;
                case "4": // List all customers
                    sqlSt = "SELECT * FROM Employee;";
                    doQuery(sqlSt);
                    break;
                case "A": // Add customer
                    addCustomer();
                    break;
                case "D": // Delete customer
                    deleteCustomer();
                    break;
                case "U": // Delete customer
                    updateCustomer();
                    break;
                case "X":
                    System.out.println("Exiting!");
                    closeConnection();
                    return;
                case "3a":
                    ex3a();
                    break;
                case "3b":
                    ex3b();
                    break;
                case "4a":
                    ex4a();
                    break;
                case "4b":
                    ex4b();
                    break;
                case "5a":
                    ex5a();
                    break;
                case "5b":
                    ex5b();
                    break;
                case "6a":
                    ex6a();
                    break;
                case "6b":
                    ex6b();
                    break;
                case "proc":
                    proc();
                    break;
                default:
                    System.out.println("Invalid input!");
                    break;
            }
        }
    }

    private void addCustomer() {
        try {
            System.out.print("Enter customer id: ");
            String cid = getLine();
            System.out.print("Enter customer name: ");
            String cname = getLine();
            cname = convertSQLString(cname);

            String sqlSt = "INSERT INTO Customer (customerId, customerName) VALUES ('" + cid + "', '" + cname + "');";
            doUpdate(sqlSt);
        } catch (Exception e) {
            System.out.println("Failed to add customer: " + e);
        }
    }
    private void updateCustomer() {
        try {
            System.out.print("Enter customer id: ");
            String cid = getLine();
            System.out.print("Enter new customer name: ");
            String cname = getLine();
            cname = convertSQLString(cname);

            String sqlSt = "UPDATE Customer set CustomerName  = '"+cname+"' where CustomerId ="+ cid + ";";
            doUpdate(sqlSt);
        } catch (Exception e) {
            System.out.println("Failed to update customer: " + e);
        }
    }



    private void deleteCustomer() {
        System.out.print("Enter customer id to delete: ");
        String cid = getLine();
        String sqlSt = "DELETE FROM Customer WHERE customerId = '" + cid + "';";
        doUpdate(sqlSt);
    }

    private void doUpdate(String updateStr) {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(updateStr);
            System.out.println("Operation successful!");
        } catch (SQLException e) {
            System.out.println("Operation failed: " + e);
        }
    }

    private void doQuery(String queryStr) {
        try (Statement stmt = con.createStatement();
             ResultSet rst = stmt.executeQuery(queryStr)) {
            ResultSetMetaData rsmd = rst.getMetaData();
            int colCount = rsmd.getColumnCount();

            // Print header
            for (int i = 1; i <= colCount; i++) {
                System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();

            // Print rows
            while (rst.next()) {
                for (int i = 1; i <= colCount; i++) {
                    System.out.print(rst.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex);
        }
    }

    private void ex3a(){
        String sqlST = "SELECT * FROM Spatiu WHERE adresa LIKE 'Turda%' ORDER BY zona ASC, suprafata DESC;";
        doQuery(sqlST);
    }

    private void ex3b(){
        String sqlST = "SELECT * FROM Oferta WHERE  vanzare = 'D' AND moneda = 'EUR' AND pret BETWEEN 10000 AND 50000 ORDER BY pret ASC;";
        doQuery(sqlST);
    }

    private void ex4a(){
        String sqlST = "SELECT S.adresa, S.zona, S.suprafata, T.caracteristici FROM Spatiu S JOIN Tip T ON S.id_tip = T.id_tip JOIN Oferta O ON S.id_Spatiu = O.id_Spatiu WHERE O.vanzare = 'N' AND T.denumire = 'apartament' AND O.moneda = 'EUR' AND O.pret BETWEEN 100 AND 400;";
        doQuery(sqlST);
    }

    private void ex4b(){
        String sqlST = "SELECT o1.id_spatiu AS id_spatiu1, o2.id_spatiu AS id_spatiu2 FROM Oferta o1 JOIN  Oferta o2 ON o1.id_agentie = o2.id_agentie WHERE o1.vanzare = 'D' AND o2.vanzare = 'D' AND o1.id_spatiu < o2.id_spatiu AND o1.moneda = 'EUR' AND o2.moneda = 'EUR' AND ABS(o1.pret - o2.pret) < 100;";
        doQuery(sqlST);
    }

    private void ex5a(){
        String sqlST = "SELECT s.id_spatiu, s.adresa, s.zona, s.suprafata " +
                "FROM Spatiu s " +
                "JOIN Oferta o ON s.id_spatiu = o.id_spatiu " +
                "WHERE o.vanzare = 'D' " +
                "  AND s.id_tip IN ( " +
                "      SELECT t.id_tip " +
                "      FROM Tip t " +
                "      WHERE t.caracteristici LIKE '%3 camere%' " +
                "  ) " +
                "  AND o.pret = ( " +
                "      SELECT MIN(o2.pret) " +
                "      FROM Oferta o2 " +
                "      WHERE o2.vanzare = 'D' " +
                "  );";
        doQuery(sqlST);
    }

    private void ex5b(){
        String sqlST =  "SELECT a.nume " +
                "FROM Agentie a " +
                "WHERE EXISTS ( " +
                "    SELECT 1 " +
                "    FROM Oferta o1 " +
                "    JOIN Oferta o2 ON o1.id_agentie = o2.id_agentie " +
                "    WHERE o1.id_spatiu = 1 " +
                "      AND o1.id_agentie = 1 " +
                "      AND o2.id_agentie = a.id_agentie " +
                "      AND o1.pret = o2.pret " +
                "      AND o1.moneda = o2.moneda " +
                ");";
        doQuery(sqlST);
    }

    private void ex6a(){
        String sqlST = "SELECT " +
                "    o.moneda, " +
                "    MIN(o.pret) AS pret_minim, " +
                "    AVG(o.pret) AS pret_mediu, " +
                "    MAX(o.pret) AS pret_maxim " +
                "FROM " +
                "    Oferta o " +
                "JOIN " +
                "    Spatiu s ON o.id_spatiu = s.id_spatiu " +
                "JOIN " +
                "    Tip t ON s.id_tip = t.id_tip " +
                "WHERE " +
                "    t.denumire = 'garsonieră' " +
                "    AND o.vanzare = 'D' " +
                "GROUP BY " +
                "    o.moneda;";
        doQuery(sqlST);
    }


    private void ex6b(){
        String sqlST = "SELECT " +
                "    s.zona, " +
                "    MIN(o.pret) AS pret_minim, " +
                "    AVG(o.pret) AS pret_mediu, " +
                "    MAX(o.pret) AS pret_maxim " +
                "FROM " +
                "    Oferta o " +
                "JOIN " +
                "    Spatiu s ON o.id_spatiu = s.id_spatiu " +
                "JOIN " +
                "    Tip t ON s.id_tip = t.id_tip " +
                "WHERE " +
                "    t.denumire = 'garaj' " +
                "    AND o.vanzare = 'N' " +
                "GROUP BY " +
                "    s.zona;";
        doQuery(sqlST);
    }

    private void proc() {
        String sql = "{CALL AdaugaExcepții()}"; // Call the stored procedure

        try (CallableStatement stmt = con.prepareCall(sql)) {
            // Execute the stored procedure
            stmt.execute();
            System.out.println("Excepții adăugate cu succes!");
        } catch (SQLException e) {
            System.out.println("Eroare la adăugarea excepțiilor: " + e.getMessage());
        }
    }

    private String getLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
            return null;
        }
    }

    private String convertSQLString(String st) {
        return st.replaceAll("'", "''");
    }

    private void printMenu() {
        System.out.println("\n\nSelect one of these options: ");
        System.out.println("  1 - List all customers");
        System.out.println("  2 - List all orders for a customer");
        System.out.println("  3 - List all lineitems for an order");
        System.out.println("  4 - List all employees");
        System.out.println("  A - Add a customer");
        System.out.println("  D - Delete a customer");
        System.out.println("  X - Exit application");
        System.out.println("  3a");
        System.out.println("  3b");
        System.out.println("  4a");
        System.out.println("  4b");
        System.out.println("  5a");
        System.out.println("  5b");
        System.out.println("  6a");
        System.out.println("  6b");
        System.out.println("  proc");
        System.out.print("Your choice: ");
    }

    private void closeConnection() {
        try {
            if (con != null) {
                con.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException ex) {
            System.err.println("Exception during connection close: " + ex);
        }
    }
}
