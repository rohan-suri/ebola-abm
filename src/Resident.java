import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.Grid2D;
import sim.util.Double2D;
import sim.util.DoubleBag;
import sim.util.IntBag;

import java.util.ArrayList;

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
    private ArrayList<EbolaBuilder.Location> pathToSchool;
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

        if(pathToSchool != null)
        {
            EbolaBuilder.Location loc = pathToSchool.remove(0);
            this.x = loc.getX();
            this.y = loc.getY();
            if(pathToSchool.isEmpty())
                pathToSchool = null;
            return;
        }

        //if we are already at a source no need to move closer, now check if you can go to school
        if(ebolaSim.road_cost.get((int)this.x, (int)this.y) == 0)
        {
            EbolaBuilder.Node closestNode = EbolaBuilder.getNearestNode((int)this.x, (int)this.y);
            this.x = closestNode.location.getX();//teleport to nearest node, most likely only a couple cells away
            this.y = closestNode.location.getY();
            //get path to school
//            double distance = closestNode.location.distanceTo(this.nearestSchool.getNearestNode().location);
//            distance *= (Parameters.POP_BLOCK_METERS/Parameters.WORLD_TO_POP_SCALE)/1000.0;//convert to kilometers
//            System.out.println("Getting path to school... distance = " + distance);

//            if(true)
//            {
//                distance = new Double2D(nearestSchool.getX(), nearestSchool.getY()).distance(nearestSchool.getNearestNode().location.getX(), nearestSchool.getNearestNode().location.getY());
//                distance *= (Parameters.POP_BLOCK_METERS/Parameters.WORLD_TO_POP_SCALE)/1000.0;//convert to kilometers
//                System.out.println("distance between school and person = " + distance);
//
//                distance = new Double2D(household.getX(), household.getY()).distance(nearestSchool.getNearestNode().location.getX(), nearestSchool.getNearestNode().location.getY());
//                distance *= (Parameters.POP_BLOCK_METERS/Parameters.WORLD_TO_POP_SCALE)/1000.0;//convert to kilometers
//                System.out.println("distance between school and household = " + distance);
//            }
            long t = System.currentTimeMillis();
            pathToSchool = AStar.astarPath(ebolaSim, closestNode, nearestSchool.getNearestNode());
            if((System.currentTimeMillis()-t)/1000 > 1)
                System.out.println("time = " + (System.currentTimeMillis()-t)/1000 + " seconds");
            if(pathToSchool == null)
                System.out.println("CANNOT Reach SCHOOL!!!!!!!!!!!");
            return;
        }
        //this code moves guy closer to each
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

    public EbolaBuilder.Node getNearestTrimmedNode(EbolaBuilder.Node start)
    {
//        LineString ls = start.lineString;
//        CoordinateSequence cs = ls.getCoordinateSequence();
        return null;
    }

}
