import sim.util.Bag;
import sim.util.Int2D;

/**
 * Created by rohansuri on 7/24/15.
 */
public abstract class Structure
{
    protected Int2D location;
    protected EbolaBuilder.Node nearestNode;
    protected Bag members;//all people that go to this structure on the daily.  Could be students, household members, hospital staff, etc

    public Structure(Int2D location)
    {
        this.location = location;
        members = new Bag();
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
}
