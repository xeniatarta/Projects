package com.example.colocvbd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;

import java.sql.*;
import java.io.*;

public class HelloController {

    private String url = "jdbc:mysql://localhost:3306/agentie_imobiliara?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private String uid = "root";
    private String pw = "model123";
    private BufferedReader reader;
    private Connection con;

    // Inițializează conexiunea la baza de date
    private void init() {
        System.out.println("Inițializare conexiune la baza de date...");

        try {
            // Încarcă driverul MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL încărcat.");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL nu a fost găsit: " + e);
            return;
        }

        try {
            con = DriverManager.getConnection(url, uid, pw);
            System.out.println("Conexiune reușită la baza de date!");
        } catch (SQLException ex) {
            System.err.println("Eroare la conectarea la baza de date: " + ex);
        }

        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @FXML
    private TableView<ObservableList<Object>> tableView;

    // Funcția pentru resetarea meniului principal
    @FXML
    public void returnToMenu() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }

    // Metoda initialize() pentru JavaFX
    @FXML
    public void initialize() {
        init(); // Inițializează conexiunea la baza de date
    }

    private void executeQuery(String queryStr) {
        if (con == null) {
            System.err.println("Conexiunea la baza de date nu este inițializată. Apel 'init()' înainte de a executa interogări.");
            return;
        }

        tableView.getColumns().clear(); // Golește coloanele anterioare
        tableView.getItems().clear();  // Golește datele anterioare

        try (Statement stmt = con.createStatement();
             ResultSet rst = stmt.executeQuery(queryStr)) {

            ResultSetMetaData rsmd = rst.getMetaData();
            int colCount = rsmd.getColumnCount();

            // Adaugă coloanele la tabel
            for (int i = 1; i <= colCount; i++) {
                TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(rsmd.getColumnName(i));
                final int colIndex = i - 1;

                col.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue().get(colIndex)));
                tableView.getColumns().add(col);
            }

            // Adaugă rândurile în tabel
            ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
            while (rst.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rst.getObject(i));
                }
                data.add(row);
            }

            tableView.setItems(data);
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex);
        }
    }


    // Funcții pentru fiecare interogare
    @FXML
    public void ex3a() {
        String sqlST = "SELECT * FROM Spatiu WHERE adresa LIKE 'Turda%' ORDER BY zona ASC, suprafata DESC;";
        executeQuery(sqlST);
    }

    @FXML
    public void ex3b() {
        String sqlST = "SELECT * FROM Oferta WHERE vanzare = 'D' AND moneda = 'EUR' AND pret BETWEEN 10000 AND 50000 ORDER BY pret ASC;";
        executeQuery(sqlST);
    }

    @FXML
    public void ex4a() {
        String sqlST = "SELECT S.adresa, S.zona, S.suprafata, T.caracteristici FROM Spatiu S JOIN Tip T ON S.id_tip = T.id_tip JOIN Oferta O ON S.id_spatiu = O.id_spatiu WHERE O.vanzare = 'N' AND T.denumire = 'apartament' AND O.moneda = 'EUR' AND O.pret BETWEEN 100 AND 400;";
        executeQuery(sqlST);
    }

    @FXML
    public void ex4b() {
        String sqlST = "SELECT o1.id_spatiu AS id_spatiu1, o2.id_spatiu AS id_spatiu2 FROM Oferta o1 JOIN Oferta o2 ON o1.id_agentie = o2.id_agentie WHERE o1.vanzare = 'D' AND o2.vanzare = 'D' AND o1.id_spatiu < o2.id_spatiu AND o1.moneda = 'EUR' AND o2.moneda = 'EUR' AND ABS(o1.pret - o2.pret) < 100;";
        executeQuery(sqlST);
    }

    @FXML
    public void ex5a() {
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
        executeQuery(sqlST);
    }

    @FXML
    public void ex5b() {
        String sqlST = "SELECT a.nume " +
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
        executeQuery(sqlST);
    }

    @FXML
    public void ex6a() {
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
        executeQuery(sqlST);
    }

    @FXML
    public void ex6b() {
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
        executeQuery(sqlST);
    }

    // Închide conexiunea la baza de date
    private void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Conexiunea la baza de date a fost închisă.");
            }
        } catch (SQLException ex) {
            System.err.println("Eroare la închiderea conexiunii: " + ex);
        }
    }

    @FXML
    private Label messageLabel; // Label pentru afișarea mesajelor

    @FXML
    private void proc() {
        String sql = "{CALL AdaugaExcepții()}";

        try (CallableStatement stmt = con.prepareCall(sql)) {
            stmt.execute();
            updateMessageLabel("Procedura a fost executată cu succes!", "green");
        } catch (SQLException e) {
            updateMessageLabel("Eroare la executarea procedurii: " + e.getMessage(), "red");
        }
    }

    private void updateMessageLabel(String message, String color) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
    }


    @FXML
    public void testConnection() {
        if (con != null) {
            System.out.println("Conexiunea la baza de date este activă.");
        } else {
            System.err.println("Conexiunea la baza de date NU este activă.");
        }
    }
}
