import sim.util.Int2D;

/**
 * Created by geoint on 8/5/15.
 */
public class WorkLocation extends Structure
{
    private int capacity;

    public WorkLocation(Int2D location)
    {
        super(location);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

}
