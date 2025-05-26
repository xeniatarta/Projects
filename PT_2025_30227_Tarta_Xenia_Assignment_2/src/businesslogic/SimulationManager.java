package businesslogic;

import model.Server;
import model.Task;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationManager implements Runnable {
    private final int timeLimit;
    private final int minArrivalTime;
    private final int maxArrivalTime;
    private final int minServiceTime;
    private final int maxServiceTime;
    private final int numberOfClients;
    private final int numberOfServers;

    private final Scheduler scheduler;
    private final List<Task> generatedTasks;
    private final AtomicInteger currentTime = new AtomicInteger(0);
    private final List<Task> completedTasks = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger totalServiceTime = new AtomicInteger(0);
    private final AtomicInteger totalWaitingTime = new AtomicInteger(0);
    private final AtomicInteger processedClients = new AtomicInteger(0);

    private volatile boolean paused = false;
    private volatile boolean stopped = false;

    public SimulationManager(int timeLimit, int minArrivalTime, int maxArrivalTime, int minServiceTime, int maxServiceTime, int numberOfClients, int numberOfServers) {
        this.timeLimit = timeLimit;
        this.minArrivalTime = minArrivalTime;
        this.maxArrivalTime = maxArrivalTime;
        this.minServiceTime = minServiceTime;
        this.maxServiceTime = maxServiceTime;
        this.numberOfClients = numberOfClients;
        this.numberOfServers = numberOfServers;
        this.scheduler = new Scheduler(numberOfServers);
        this.generatedTasks = generateTasks();
    }

    public int getCurrentTime() {
        return currentTime.get();
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public List<Task> getGeneratedTasks() {
        return new ArrayList<>(generatedTasks);
    }

    public List<Task> getCompletedTasks() {
        return new ArrayList<>(completedTasks);
    }

    public int getNumberOfClients() {
        return numberOfClients;
    }

    public double getAverageWaitingTime() {
        return processedClients.get() > 0 ? (double) totalWaitingTime.get() / processedClients.get() : 0;
    }

    public double getAverageServiceTime() {
        return processedClients.get() > 0 ? (double) totalServiceTime.get() / processedClients.get() : 0;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void stopSimulation() {
        this.stopped = true;
        scheduler.stopServers();
    }

    private List<Task> generateTasks() {
        Random rand = new Random();
        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < numberOfClients; i++) {
            Task task = new Task(i + 1);
            task.setArrivalTime(rand.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime);
            task.setServiceTime(rand.nextInt(maxServiceTime - minServiceTime + 1) + minServiceTime);
            tasks.add(task);
        }

        tasks.sort(Comparator.comparingInt(Task::getArrivalTime));
        return tasks;
    }

    @Override
    public void run() {
        while (currentTime.get() <= timeLimit && !stopped && (!generatedTasks.isEmpty() || scheduler.areServersBusy())) {
            while (paused && !stopped) {
                try { Thread.sleep(100); }
                catch (InterruptedException e) {
                    return;
                }
            }

            if (stopped) break;

            dispatchArrivingTasks();
            List<Task> newlyCompleted = scheduler.processTasks(currentTime.get());
            processCompletedTasks(newlyCompleted);

            try { Thread.sleep(1000); }
            catch (InterruptedException e) { break; }

            currentTime.incrementAndGet();
        }
        scheduler.stopServers();
    }

    private void processCompletedTasks(List<Task> newlyCompleted) {
        for (Task task : newlyCompleted) {
            totalServiceTime.addAndGet(task.getServiceTime());
            totalWaitingTime.addAndGet(task.getWaitingTime());
            processedClients.incrementAndGet();
        }
        completedTasks.addAll(newlyCompleted);
    }

    private void dispatchArrivingTasks() {
        Iterator<Task> iterator = generatedTasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.getArrivalTime() <= currentTime.get()) {
                scheduler.dispatchTask(task);
                iterator.remove();
            }
        }
    }
}