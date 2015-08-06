import sim.util.Bag;
import sim.util.Int2D;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by rohansuri on 7/24/15.
 */
public class Structure
{
    protected Int2D location;
    protected EbolaBuilder.Node nearestNode;
    protected Bag members;//all people that go to this structure on the daily.  Could be students, household members, hospital staff, etc
    protected HashMap<Structure, Route> cachedPaths;
    int currentMembers;
    protected int capacity;

    public Structure(Int2D location)
    {
        this.location = location;
        members = new Bag();
        cachedPaths = new HashMap<>();
    }

    public Int2D getLocation()
    {
        return location;
    }

    public void setLocation(Int2D location)
    {
        this.location = location;
    }

    public void setNearestNode(EbolaBuilder.Node node)
    {
        nearestNode = node;
    }

    public EbolaBuilder.Node getNearestNode()
    {
        return nearestNode;
    }

    public void addMembers(Bag people)
    {
        members.addAll(people);
    }

    public void addMember(Resident r)
    {
        members.add(r);
    }

    public Bag getMembers()
    {
        return members;
    }

    /** Uses Astar to find shortest path and keeps caches of all previously found paths.
     * @param destination Destination Structure
     * @return null if no path exist, otherwise uses AStar to find shortest path to destination
     */
    public Route getRoute(Structure destination)
    {
        if(cachedPaths.containsKey(destination))//means we have this path cached
        {
            Route route = cachedPaths.get(destination);
            return route;
        }
        else
        {
            //check if the route has already been cached for the other way (destination -> here)
            if(destination.getCachedRoutes().containsKey(this))
            {
                Route route = destination.getRoute(this).reverse();//be sure to reverse the route
                cachedPaths.put(destination, route);
                return route;
            }
            else
            {
                Route route = AStar.astarPath(this.getNearestNode(), destination.getNearestNode());
                cachedPaths.put(destination, route);
                return route;
            }
        }
    }

    public void cacheRoute(Route route, Structure destination)
    {
        cachedPaths.put(destination, route);
    }

    public Map<Structure, Route> getCachedRoutes()
    {
        return cachedPaths;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentMembers() {
        return members.size();
    }
}
