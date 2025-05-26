package businesslogic;

import model.Server;
import model.Task;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Scheduler {
    private final List<Server> servers;
    private Strategy strategy;
    private final Map<Integer, Integer> hourlyLoad = new HashMap<>();
    private int peakHour = 0;
    private int maxLoad = 0;

    public Scheduler(int maxNoServers) {
        this.servers = new CopyOnWriteArrayList<>();
        this.strategy = new TimeStrategy();

        for (int i = 0; i < maxNoServers; i++) {
            Server server = new Server(i + 1);
            servers.add(server);
            new Thread(server).start();
        }
    }

    public void changeStrategy(SelectionPolicy policy) {
        switch (policy) {
            case SHORTEST_QUEUE:
                strategy = new ShortestQueueStrategy();
                break;
            case SHORTEST_TIME:
                strategy = new TimeStrategy();
                break;
            default:
                throw new IllegalArgumentException("Unknown policy: " + policy);
        }
    }

    public void dispatchTask(Task task) {
        if (task == null) return;
        strategy.addTask(servers, task);
    }

    public List<Task> processTasks(int currentTime) {
        List<Task> newlyCompleted = new ArrayList<>();
        updatePeakHourTracking(currentTime);

        for (Server server : servers) {
            Task completed = server.processTask(currentTime);
            if (completed != null) {
                newlyCompleted.add(completed);
            }
        }

        return newlyCompleted;
    }

    private void updatePeakHourTracking(int currentTime) {
        int currentLoad = getTotalWaitingClients();
        hourlyLoad.put(currentTime, currentLoad);

        if (currentLoad > maxLoad) {
            maxLoad = currentLoad;
            peakHour = currentTime;
        }
    }

    public List<Server> getServers() {
        return servers;
    }

    public boolean areServersBusy() {
        return servers.stream().anyMatch(Server::isBusy);
    }

    public int getTotalWaitingClients() {
        return servers.stream().mapToInt(server -> server.getQueueSize() + (server.getCurrentTask() != null ? 1 : 0)).sum();
    }

    public int getPeakHour() {
        return peakHour;
    }

    public int getPeakHourLoad() {
        return maxLoad;
    }

    public void stopServers() {
        servers.forEach(Server::stop);
    }

    public String getServersStatus(int currentTime) {
        StringBuilder sb = new StringBuilder();
        servers.forEach(server ->
                sb.append("Queue ").append(server.getId())
                        .append(": ").append(server.getStatus(currentTime))
                        .append("\n"));
        return sb.toString();
    }
}