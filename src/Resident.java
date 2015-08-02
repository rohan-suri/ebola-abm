import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.Grid2D;
import sim.util.Double2D;
import sim.util.DoubleBag;
import sim.util.Int2D;
import sim.util.IntBag;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by rohansuri on 7/7/15.
 */
public class Resident implements Steppable
{
    private Int2D location;

    private Household household;
    private boolean isUrban;//true - urban, false - rural
    private School nearestSchool;
    private LinkedList<Int2D> pathToSchool;
    private int age;
    private int pop_density;

    public Resident(Int2D location)
    {
        this.location = location;
    }

    @Override
    public void step(SimState state)
    {
        EbolaABM ebolaSim = (EbolaABM) state;
        long cStep = ebolaSim.schedule.getSteps();
        if(cStep == 0)
        {
            //if we are already at a source no need to move closer, now check if you can go to school
            EbolaBuilder.Node closestNode = household.getNearestNode();
            location = new Int2D(closestNode.location.getX(), closestNode.location.getY());//teleport to nearest node, most likely only a couple cells away
            //get path to school
            long t = System.currentTimeMillis();
            pathToSchool = AStar.astarPath(ebolaSim, closestNode, nearestSchool.getNearestNode());

            if(pathToSchool == null)
            {
                System.out.println((ebolaSim.roadLinks.getMBR().getMinX() + (closestNode.location.getX()*0.000833333333)) + "," + (ebolaSim.roadLinks.getMBR().getMinY() + (((ebolaSim.world_height-closestNode.location.getY())*0.000833333333))));
                System.out.println(cStep + " CANNOT Reach SCHOOL no_school_count =  " + ebolaSim.no_school_count++);
            }
            updatePositionOnMap(ebolaSim);
            return;

        }
        else
        {
            if(location.getX() == this.nearestSchool.getNearestNode().location.getX() && location.getY() == this.nearestSchool.getNearestNode().location.getY())
            {
                //if we found it go back to home
                //get path to school
                long t = System.currentTimeMillis();
                pathToSchool = AStar.astarPath(ebolaSim, nearestSchool.getNearestNode(), household.getNearestNode());

//                if(pathToSchool == null)
//                {
//                    System.out.println((ebolaSim.roadLinks.getMBR().getMinX() + (closestNode.location.getX()*0.000833333333)) + "," + (ebolaSim.roadLinks.getMBR().getMinY() + (((ebolaSim.world_height-closestNode.location.getY())*0.000833333333))));
//                    System.out.println(cStep + " CANNOT Reach SCHOOL no_school_count =  " + ebolaSim.no_school_count++);
//                }
                updatePositionOnMap(ebolaSim);
                return;
            }
            if(pathToSchool != null)
            {
                Int2D loc = pathToSchool.remove(0);
                location = loc;
                if(pathToSchool.isEmpty())
                    pathToSchool = null;
                updatePositionOnMap(ebolaSim);
                return;
            }
            return;
        }

        //this code moves guy closer to each
//        DoubleBag val = new DoubleBag();
//        IntBag x = new IntBag();
//        IntBag y = new IntBag();
//        ebolaSim.road_cost.getRadialNeighbors(location.getX(), location.getY(), 1, Grid2D.BOUNDED, true, val, x, y);
//        double min = Double.MAX_VALUE;
//        int index = 0;
//        for (int i = 0; i < val.size(); i++)
//            if (val.get(i) < min)
//            {
//                min = val.get(i);
//                index = i;
//            }
//
//        location = new Int2D(x.get(index), y.get(index));
//
//        updatePositionOnMap(ebolaSim);
    }

    public void updatePositionOnMap(EbolaABM ebolaSim)
    {
        double randX = ebolaSim.random.nextDouble();
        double randY = ebolaSim.random.nextDouble();
        ebolaSim.world.setObjectLocation(this, new Double2D(location.getX() + randX, location.getY() + randY));
    }


    //-----------Getters and Setters--------------//

    public Household getHousehold()
    {
        return household;
    }

    public void setHousehold(Household household)
    {
        this.household = household;
    }

    public Int2D getLocation() {
        return location;
    }

    public void setLocation(Int2D location) {
        this.location = location;
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

    public int getPop_density()
    {
        return pop_density;
    }

    public void setPop_density(int pop_density)
    {
        this.pop_density = pop_density;
    }
}
