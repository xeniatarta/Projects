package businesslogic;

import model.Server;
import model.Task;
import java.util.List;

public class ShortestQueueStrategy implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task task) {
        Server selected = null;
        int minSize = Integer.MAX_VALUE;

        for (Server server : servers) {
            int size = server.getQueueSize();
            if (size < minSize || (size == minSize && server.getCurrentTask() == null)) {
                minSize = size;
                selected = server;
            }
        }

        if (selected != null) {
            selected.addTask(task);
        }
    }
}