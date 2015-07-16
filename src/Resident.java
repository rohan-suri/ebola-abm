import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;

/**
 * Created by rohansuri on 7/7/15.
 */
public class Resident implements Steppable
{
    public double x;
    public double y;
    public Household household;
    private boolean isUrban;//true - urban, false - rural

    int age;

    public EbolaBuilder.Node getNearestNode() {
        return nearestNode;
    }

    public void setNearestNode(EbolaBuilder.Node nearestNode) {
        this.nearestNode = nearestNode;
    }

    public EbolaBuilder.Node nearestNode;

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
        EbolaABM ebolaSim = (EbolaABM)state;
        //ebolaSim.world.setObjectLocation(this, new Double2D(this.x+ebolaSim.random.nextDouble(), this.y+ebolaSim.random.nextDouble()));
        if(nearestNode == null)
            nearestNode = (EbolaBuilder.Node)ebolaSim.closestNodes.get((int)x,(int)y);
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
}
