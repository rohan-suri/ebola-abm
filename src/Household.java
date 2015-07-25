import sim.util.Bag;

/**
 * Created by rohansuri on 7/8/15.
 */
public class Household extends Structure
{
    private int country;

    public Household(int x, int y)
    {
        super(x, y);
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }
}
