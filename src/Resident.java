import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.Grid2D;
import sim.util.Double2D;
import sim.util.DoubleBag;
import sim.util.IntBag;

/**
 * Created by rohansuri on 7/7/15.
 */
public class Resident implements Steppable
{
    public double x;
    public double y;
    public Household household;
    private boolean isUrban;//true - urban, false - rural
    private School nearestSchool;

    int age;


    public int getPop_density()
    {
        return pop_density;
    }

    public void setPop_density(int pop_density)
    {
        this.pop_density = pop_density;
    }

    private int pop_density;

    @Override
    public void step(SimState state)
    {
        EbolaABM ebolaSim = (EbolaABM) state;
        DoubleBag val = new DoubleBag();
        IntBag x = new IntBag();
        IntBag y = new IntBag();

        ebolaSim.road_cost.getRadialNeighbors((int)this.x, (int)this.y, 1, Grid2D.BOUNDED, true, val, x, y);
        double min = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < val.size(); i++)
            if (val.get(i) < min)
            {
                min = val.get(i);
                index = i;
            }

        boolean all_equal_no_value = true;

        for(int i = 0; i < val.size(); i++)
            if(val.get(i) != -9999)
                all_equal_no_value = false;
        if(all_equal_no_value)
            if(x.get(index) != this.x && y.get(index) != this.y)
            {
                System.out.println("NO IDEA WHERE TO GO!!!! BUT MOVING ANWAYS LOL");
                System.out.println("(" + x.get(index) + ", " + y.get(index) + ") from " + "(" + this.x + ", " + this.y + ")");
            }
        this.x = x.get(index);
        this.y = y.get(index);

        ebolaSim.world.setObjectLocation(this, new Double2D(this.x, this.y));
    }

    public void setIsUrban(boolean val)
    {
        isUrban = val;
    }

    public boolean getIsUrban()
    {
        return isUrban;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public School getNearestSchool()
    {
        return nearestSchool;
    }
    public void setNearestSchool(School school)
    {
        this.nearestSchool = school;
    }
}
