import sim.util.Bag;

/**
 * Created by rohansuri on 7/20/15.
 */
public class School
{
    private int x;
    private int y;
    private Bag members;
    private int size;
    private EbolaBuilder.Node nearestNode;

    public School(int x, int y)
    {
        this.x = x;
        this.y = y;
        members = new Bag();
    }

    public void addMember(Resident r)
    {
        members.add(r);
    }

    public Bag getMembers()
    {
        return members;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getSize()
    {
        return members.size();
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public EbolaBuilder.Node getNearestNode()
    {
        return nearestNode;
    }

    public void setNearestNode(EbolaBuilder.Node nearestNode)
    {
        this.nearestNode = nearestNode;
    }
}
