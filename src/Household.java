import sim.util.Bag;

/**
 * Created by rohansuri on 7/8/15.
 */
public class Household
{
    private Bag members;
    public int x;
    public int y;

    public Household()
    {
        super();
        members = new Bag();
    }

    public void addMember(Resident res)
    {
        members.add(res);
    }

    public Bag getMembers()
    {
        return this.members;
    }

    public void setMembers(Bag members)
    {
        this.members = members;
    }

}
