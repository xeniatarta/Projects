package model;

public class Task {
    private final int id;
    private int arrivalTime;
    private int serviceTime;
    private int serviceStartTime = -1;

    public Task(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public int getArrivalTime() {return arrivalTime; }
    public void setArrivalTime(int time) { arrivalTime = time; }
    public int getServiceTime() { return serviceTime; }
    public void setServiceTime(int time) { serviceTime = time; }
    public int getServiceStartTime() { return serviceStartTime; }
    public void setServiceStartTime(int time) { serviceStartTime = time; }

    public int getWaitingTime() {
        return serviceStartTime - arrivalTime;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d,%d)", id, arrivalTime, serviceTime);
    }
}