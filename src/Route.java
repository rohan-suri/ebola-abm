import sim.util.Int2D;

import java.util.ArrayList;
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

    public Route reverse()
    {
        List<Int2D> reversedPath = new ArrayList<Int2D>(path.size());
        for(int i = path.size()-1; i >= 0; i--)
            reversedPath.add(path.get(i));
        return new Route(reversedPath, this.distance, this.end, this.start);
    }

    public void addToEnd(Int2D location)
    {
        path.add(location);
    }
}
