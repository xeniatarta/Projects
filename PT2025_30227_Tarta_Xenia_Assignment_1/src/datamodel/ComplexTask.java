package datamodel;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public final class ComplexTask extends Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Task> tasks = new ArrayList<>();

    public ComplexTask(int idTask, String statusTask) {

        super(idTask, statusTask);
    }


    @Override
    public int estimateDuration() {
        int totalDuration = 0;
        for (Task task : tasks) {
            totalDuration += task.estimateDuration();
        }
        return totalDuration;
    }

}
