import java.util.Date;

public class Task {
    private String title;
    private String description;
    private boolean status;
    private Date creationDate;
    private Date dueDate;

    public Task(String title, String description, Date dueDate) {
        this.title = title;
        this.description = description;
        this.status = false;
        this.creationDate = new Date();
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return title + (status ? " (Completed)" : " (Pending)");
    }
}
