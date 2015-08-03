import sim.util.Int2D;

import java.util.List;

/**
 * This class is a wrapper class for an ArrayList that manages a path. and keeps place of next item.
 */
public class Route
{
    int currentPosition;
    List<Int2D> path;//list of places this person needs to go
    public Route(List<Int2D> path)
    {
        this.path = path;
        currentPosition = 0;
    }

    /**
     * @return next location to move, null if no more moves
     */
    public Int2D getNext()
    {
        if(currentPosition >= path.size())
            return null;
        Int2D location = path.get(currentPosition);
        currentPosition++;
        return location;
    }

    public boolean isEmpty()
    {
        return currentPosition >= path.size();
    }
}
