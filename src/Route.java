import sim.util.Int2D;

import java.lang.reflect.Array;
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
    private double speed;

    public Route(List<Int2D> path, double distance, EbolaBuilder.Node start, EbolaBuilder.Node end, double speed)
    {
        this.path = path;
        this.distance = distance;
        this.start = start;
        this.end = end;
        refactorPathToStep(speed);
        this.speed = speed;
    }

    private void refactorPathToStep(double speed)
    {
        //speed should be in km/hour
        //adjust speed to temporal resolution
        speed *= Parameters.TEMPORAL_RESOLUTION;//now km per step

        //convert speed to cell block per step
        speed = Parameters.convertFromKilometers(speed);

        ArrayList<Int2D> newPath = new ArrayList<>(path.size());
        newPath.add(path.get(0));//start by adding the beginning
        int old_index = 1;
        int new_index = 1;
        double mod_speed = speed;
        while(old_index < path.size())
        {
            double distance = path.get(old_index).distance(newPath.get(old_index-1));
            while(mod_speed < distance)
            {
                newPath.add(getPointAlongLine(newPath.get(new_index-1), path.get(old_index), mod_speed/distance));
                new_index++;
                mod_speed = speed;
                distance = path.get(old_index).distance(newPath.get(new_index-1));
            }
            mod_speed -= distance;
            old_index++;
        }
        if(path.size() > 1)
            newPath.add(path.get(path.size()-1));
        this.path = newPath;
    }

    /**
     * Gets a point a certain percent a long the line
     * @param start
     * @param end
     * @param percent the percent along the line you want to get.  Must be less than 1
     * @return
     */
    private Int2D getPointAlongLine(Int2D start, Int2D end, double percent)
    {
        return new Int2D((int)Math.round((end.getX()-start.getX())*percent + start.getX()), (int)Math.round((end.getY()-start.getY())*percent + start.getX()));
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
        return new Route(reversedPath, this.distance, this.end, this.start, speed);
    }

    public void addToEnd(Int2D location)
    {
        path.add(location);
    }
}
