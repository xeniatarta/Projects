package datamodel;

public final class SimpleTask extends Task {
    private static final long serialVersionUID = 1L;
    private int startHour;
    private int endHour;

    public SimpleTask(int startHour, int endHour, int task, String statusTask) {
        super(task, statusTask);
        this.startHour = startHour;
        this.endHour = endHour;
    }

//    public int getStartHour() {return startHour;
//    }
//
//    public void setStartHour(int startHour) {
//        this.startHour = startHour;
//    }
//
//    public int getEndHour() {
//        return endHour;
//    }
//
//    public void setEndHour(int endHour) {
//        this.endHour = endHour;
//    }

    public int estimateDuration(){
        return endHour - startHour;
    }
}
