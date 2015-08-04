import sim.util.Int2D;

import java.util.List;

/**
 * This class is a wrapper class for an ArrayList that manages a path and other information
 */
public class Route
{
    private List<Int2D> path;//list of places this person needs to go
    private double distance;
    private EbolaBuilder.Node start;
    private EbolaBuilder.Node end;

    public Route(List<Int2D> path, double distance, EbolaBuilder.Node start, EbolaBuilder.Node end)
    {
        this.path = path;
        this.distance = distance;
        this.start = start;
        this.end = end;
    }

    /**
     * @return next location to move, null if no more moves
     */
    public Int2D getLocation(int index)
    {
        Int2D location = path.get(index);
        return location;
    }

    public double getTotalDistance()
    {
        return distance;
    }

    public int getNumSteps()
    {
        return path.size();
    }

    public EbolaBuilder.Node getStart()
    {
        return start;
    }

    public EbolaBuilder.Node getEnd()
    {
        return end;
    }
}
