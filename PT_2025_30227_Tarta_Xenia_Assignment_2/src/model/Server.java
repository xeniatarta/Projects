package model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
    private final BlockingQueue<Task> tasks = new LinkedBlockingQueue<>();
    private final AtomicInteger totalWaitingTime = new AtomicInteger(0);
    private volatile boolean running = true;
    private final int id;
    private Task currentTask = null;
    private int completionTime = 0;
    private final List<Task> completedTasks = new ArrayList<>();
    private volatile int currentTime;

    public Server(int id) {
        this.id = id;
    }

    public synchronized void addTask(Task newTask) {
        tasks.add(newTask);
        totalWaitingTime.addAndGet(newTask.getServiceTime());
    }

    public synchronized Task processTask(int currentTime) {
        Task completedTask = null;
        this.currentTime = currentTime;

        if (currentTask == null && !tasks.isEmpty()) {
            currentTask = tasks.poll();
            currentTask.setServiceStartTime(currentTime);
            completionTime = currentTime + currentTask.getServiceTime();
            totalWaitingTime.addAndGet(-currentTask.getServiceTime());
        }

        if (currentTask != null && currentTime >= completionTime) {
            completedTask = currentTask;
            completedTasks.add(completedTask);
            currentTask = null;
        }

        return completedTask;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public synchronized void stop() {
        running = false;
    }

    public synchronized int getTotalWaitingTime() {
        int time = totalWaitingTime.get();
        if (currentTask != null) {
            time += (completionTime - currentTime);
        }
        return time;
    }

    public synchronized int getQueueSize() {
        return tasks.size() + (currentTask != null ? 1 : 0);
    }

    public synchronized boolean isBusy() {
        return currentTask != null || !tasks.isEmpty();
    }

//    public synchronized boolean isIdle() {
//        return currentTask == null && tasks.isEmpty();
//    }

    public synchronized String getStatus(int currentTime) {
        if (!isBusy())
            return "closed";

        StringBuilder status = new StringBuilder();
        if (currentTask != null) {
            int remaining = Math.max(0, completionTime - currentTime);
            status.append(String.format("[%d (%ds)] ", currentTask.getId(), remaining));
        }

        tasks.forEach(task -> status.append(task).append(" "));
        return status.toString().trim();
    }

    public synchronized Task getCurrentTask() {
        return currentTask;
    }

    public synchronized List<Task> getCompletedTasks() {
        return new ArrayList<>(completedTasks);
    }

    public int getId() {
        return id;
    }
}