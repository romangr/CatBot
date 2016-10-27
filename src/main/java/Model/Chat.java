package Model;

/**
 * Roman 27.10.2016.
 */
public class Chat {
    private int id;
    private String title;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Model.User{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
