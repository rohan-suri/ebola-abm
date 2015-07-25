import sim.util.Bag;

/**
 * Created by rohansuri on 7/20/15.
 */
public class School extends Structure {
    private int size;

    public School(int x, int y) {
        super(x, y);

        members = new Bag();
    }

    public int getSize() {
        return members.size();
    }

    public void setSize(int size) {
        this.size = size;
    }
}
