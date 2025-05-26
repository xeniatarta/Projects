import businesslogic.SimulationManager;

public class Main {
    public static void main(String[] args) {
        SimulationManager manager = new SimulationManager(
                60,   // timeLimit
                1,    // minArrivalTime
                30,   // maxArrivalTime
                1,    // minServiceTime
                7,    // maxServiceTime
                50,   // numberOfClients
                5     // numberOfServers

        );
        new Thread(manager).start();
    }
}