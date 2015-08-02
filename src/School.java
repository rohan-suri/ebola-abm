import sim.util.Bag;
import sim.util.Int2D;

/**
 * Created by rohansuri on 7/20/15.
 */
public class School extends Structure {
    private int size;

    public School(Int2D location) {
        super(location);

        members = new Bag();
    }

    public int getSize() {
        return members.size();
    }

    public void setSize(int size) {
        this.size = size;
    }
}
