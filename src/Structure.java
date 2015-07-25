import sim.util.Bag;

/**
 * Created by rohansuri on 7/24/15.
 */
public abstract class Structure
{
    protected int x;
    protected int y;
    protected EbolaBuilder.Node nearestNode;
    protected Bag members;//all people that go to this structure on the daily.  Could be students, household members, hospital staff, etc

    public Structure(int x, int y)
    {
        this.x = x;
        this.y = y;
        members = new Bag();
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
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
