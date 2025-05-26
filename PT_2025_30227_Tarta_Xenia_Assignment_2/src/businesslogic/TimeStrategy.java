package businesslogic;

import model.Server;
import model.Task;
import java.util.List;

public class TimeStrategy implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task task) {
        Server selected = null;
        int minTime = Integer.MAX_VALUE;

        for (Server server : servers) {
            int waitTime = server.getTotalWaitingTime();
            if (waitTime < minTime || (waitTime == minTime && server.getCurrentTask() == null)) {
                minTime = waitTime;
                selected = server;
            }
        }

        if (selected != null) {
            selected.addTask(task);
        }
    }
}