import sim.util.Bag;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohansuri on 7/8/15.
 */
public class Household extends Structure
{
    private int country;

    private List<Household> relatives;
    //TODO separate relatives and household because when people move households, their relatives shouldn't change

    public Household(Int2D location)
    {
        super(location);
        relatives = new ArrayList<>();
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public List<Household> getRelatives()
    {
        return this.relatives;
    }

    public void addRelatives(Household h)
    {
        this.relatives.add(h);
    }
}
