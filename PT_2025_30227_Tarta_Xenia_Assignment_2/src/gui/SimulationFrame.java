package gui;

import businesslogic.SimulationManager;
import businesslogic.SelectionPolicy;
import model.Server;
import model.Task;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationFrame extends JFrame {
    private SimulationManager simulationManager;
    private Timer guiTimer;
    private JTextArea logArea;
    private JPanel queuesPanel;
    private boolean simulationRunning = false;
    private boolean simulationPaused = false;

    private final JTextField timeLimitField = new JTextField("");
    private final JTextField minArrivalField = new JTextField("");
    private final JTextField maxArrivalField = new JTextField("");
    private final JTextField minServiceField = new JTextField("");
    private final JTextField maxServiceField = new JTextField("");
    private final JTextField clientsField = new JTextField("");
    private final JTextField queuesField = new JTextField("");
    private final JComboBox<SelectionPolicy> strategyCombo = new JComboBox<>(SelectionPolicy.values());

    private final JButton startButton = new JButton("Start");
    private final JButton stopButton = new JButton("Stop");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton saveLogButton = new JButton("Save Log");

    private final JLabel currentTimeLabel = new JLabel("Time: 0/0");
    private final JLabel avgWaitingTimeLabel = new JLabel("Avg Wait: -");
    private final JLabel avgServiceTimeLabel = new JLabel("Avg Service: -");
    private final JLabel peakHourLabel = new JLabel("Peak Hour: -");
    private final JLabel clientsLabel = new JLabel("Clients: 0/0");

    private final StringBuilder fullLog = new StringBuilder();
    private final AtomicInteger simulationTime = new AtomicInteger(0);

    public SimulationFrame() {
        configureWindow();
        createUIComponents();
        setupEventHandlers();
    }

    private void configureWindow() {
        setTitle("Queues Management Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private void createUIComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(4, 4, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));

        inputPanel.add(new JLabel("Number of Clients (N):"));
        inputPanel.add(clientsField);
        inputPanel.add(new JLabel("Number of Queues (Q):"));
        inputPanel.add(queuesField);

        inputPanel.add(new JLabel("Simulation Time (sec):"));
        inputPanel.add(timeLimitField);
        inputPanel.add(new JLabel("Strategy:"));
        inputPanel.add(strategyCombo);

        inputPanel.add(new JLabel("Min Arrival Time:"));
        inputPanel.add(minArrivalField);
        inputPanel.add(new JLabel("Max Arrival Time:"));
        inputPanel.add(maxArrivalField);

        inputPanel.add(new JLabel("Min Service Time:"));
        inputPanel.add(minServiceField);
        inputPanel.add(new JLabel("Max Service Time:"));
        inputPanel.add(maxServiceField);

        JPanel controlPanel = new JPanel();
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
        saveLogButton.setEnabled(false);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(pauseButton);
        controlPanel.add(saveLogButton);

        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statsPanel.add(currentTimeLabel);
        statsPanel.add(avgWaitingTimeLabel);
        statsPanel.add(avgServiceTimeLabel);
        statsPanel.add(peakHourLabel);
        statsPanel.add(clientsLabel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.SOUTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log"));
        logScroll.setPreferredSize(new Dimension(400, 500));

        queuesPanel = new JPanel();
        queuesPanel.setLayout(new BoxLayout(queuesPanel, BoxLayout.Y_AXIS));
        JScrollPane queuesScroll = new JScrollPane(queuesPanel);
        queuesScroll.setBorder(BorderFactory.createTitledBorder("Queues"));
        queuesScroll.setPreferredSize(new Dimension(400, 500));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, queuesScroll, logScroll);
        splitPane.setResizeWeight(0.5);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        guiTimer = new Timer(1000, e -> updateUI());
    }

    private void setupEventHandlers() {
        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
        pauseButton.addActionListener(e -> togglePause());
        saveLogButton.addActionListener(e -> saveLogToFile());
    }

    private void startSimulation() {
        try {
            int timeLimit = Integer.parseInt(timeLimitField.getText());
            int minArrival = Integer.parseInt(minArrivalField.getText());
            int maxArrival = Integer.parseInt(maxArrivalField.getText());
            int minService = Integer.parseInt(minServiceField.getText());
            int maxService = Integer.parseInt(maxServiceField.getText());
            int clients = Integer.parseInt(clientsField.getText());
            int queues = Integer.parseInt(queuesField.getText());

            if (minArrival >= maxArrival || minService >= maxService ||
                    clients <= 0 || queues <= 0 || timeLimit <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid parameters!");
                return;
            }

            simulationManager = new SimulationManager(
                    timeLimit, minArrival, maxArrival,
                    minService, maxService, clients, queues);

            simulationManager.getScheduler().changeStrategy(
                    (SelectionPolicy) strategyCombo.getSelectedItem());

            resetUI();

            new Thread(() -> {
                simulationManager.run();
                SwingUtilities.invokeLater(() -> {
                    simulationRunning = false;
                    stopButton.setEnabled(false);
                    pauseButton.setEnabled(false);
                    saveLogButton.setEnabled(true);
                    log("\nSimulation completed at time " + simulationManager.getCurrentTime());
                    logStatistics();
                });
            }).start();

            guiTimer.start();
            simulationRunning = true;
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);

            log("Simulation started with " + clients + " clients and " + queues + " queues");
            log("Clients generated:");
            for (Task task : simulationManager.getGeneratedTasks()) {
                log(task.toString());
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
        }
    }

    private void resetUI() {
        logArea.setText("");
        fullLog.setLength(0);
        queuesPanel.removeAll();

        for (int i = 0; i < Integer.parseInt(queuesField.getText()); i++) {
            JTextArea queueArea = new JTextArea(8, 30);
            queueArea.setEditable(false);
            JPanel queuePanel = new JPanel(new BorderLayout());
            queuePanel.setBorder(BorderFactory.createTitledBorder("Queue " + (i + 1)));
            queuePanel.add(new JScrollPane(queueArea), BorderLayout.CENTER);
            queuesPanel.add(queuePanel);
        }

        queuesPanel.revalidate();
        queuesPanel.repaint();
    }

    private void stopSimulation() {
        if (simulationManager != null) {
            simulationManager.stopSimulation();
        }
        guiTimer.stop();
        simulationRunning = false;
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
        log("\nSimulation stopped at time " + simulationTime.get());
        logStatistics();
    }

    private void togglePause() {
        if (simulationManager != null) {
            simulationPaused = !simulationPaused;
            simulationManager.setPaused(simulationPaused);
            pauseButton.setText(simulationPaused ? "Resume" : "Pause");
            log("Simulation " + (simulationPaused ? "paused" : "resumed") + " at time " + simulationTime.get());
        }
    }

    private void updateUI() {
        if (simulationManager != null && simulationRunning) {
            int currentTime = simulationManager.getCurrentTime();
            simulationTime.set(currentTime);

            currentTimeLabel.setText("Time: " + currentTime + "/" + simulationManager.getTimeLimit());
            updateQueuesDisplay();
            updateStatistics();
            logTimeStep(currentTime);

            if (currentTime >= simulationManager.getTimeLimit() ||
                    (simulationManager.getGeneratedTasks().isEmpty() &&
                            !simulationManager.getScheduler().areServersBusy())) {
                stopSimulation();
            }
        }
    }

    private void logTimeStep(int currentTime) {
        log("\nTime " + currentTime);

        StringBuilder waitingClients = new StringBuilder("Waiting clients: ");
        boolean hasWaiting = false;

        for (Task task : simulationManager.getGeneratedTasks()) {
            if (task.getArrivalTime() > currentTime) {
                waitingClients.append(task.toString()).append("; ");
                hasWaiting = true;
            }
        }

        if (hasWaiting) {
            log(waitingClients.toString().replaceAll("; $", ""));
        } else {
            log("No waiting clients");
        }

        log("Queues status:");
        for (Server server : simulationManager.getScheduler().getServers()) {
            log("Queue " + server.getId() + ": " + server.getStatus(currentTime));
        }
    }

    private void updateQueuesDisplay() {
        List<Server> servers = simulationManager.getScheduler().getServers();
        Component[] components = queuesPanel.getComponents();

        for (int i = 0; i < components.length && i < servers.size(); i++) {
            JPanel queuePanel = (JPanel) components[i];
            JScrollPane scrollPane = (JScrollPane) queuePanel.getComponent(0);
            JTextArea queueArea = (JTextArea) scrollPane.getViewport().getView();
            Server server = servers.get(i);
            queueArea.setText(server.getStatus(simulationTime.get()));
        }
    }

    private void updateStatistics() {
        avgWaitingTimeLabel.setText(String.format("Avg Wait: %.2fs",
                simulationManager.getAverageWaitingTime()));
        avgServiceTimeLabel.setText(String.format("Avg Service: %.2fs",
                simulationManager.getAverageServiceTime()));
        peakHourLabel.setText(String.format("Peak: %d (%d)",
                simulationManager.getScheduler().getPeakHour(),
                simulationManager.getScheduler().getPeakHourLoad()));
        clientsLabel.setText(String.format("Clients: %d/%d",
                simulationManager.getCompletedTasks().size(),
                simulationManager.getNumberOfClients()));
    }

    private void logStatistics() {
        log(String.format("\nAverage waiting time: %.2f seconds", simulationManager.getAverageWaitingTime()));
        log(String.format("Average service time: %.2f seconds", simulationManager.getAverageServiceTime()));
        log(String.format("Peak hour: %d with %d clients",
                simulationManager.getScheduler().getPeakHour(),
                simulationManager.getScheduler().getPeakHourLoad()));
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            fullLog.append(message).append("\n");
        });
    }

    private void saveLogToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Log");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(fileChooser.getSelectedFile()))) {
                writer.write(fullLog.toString());
                JOptionPane.showMessageDialog(this, "Log saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving log: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationFrame frame = new SimulationFrame();
            frame.setVisible(true);
        });
    }
}