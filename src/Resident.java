import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.LinkedList;

/**
 * Created by rohansuri on 7/7/15.
 */
public class Resident implements Steppable
{
    private Int2D location;
    private boolean inactive;

    private Household household;
    private boolean isUrban;//true - urban, false - rural
    private School nearestSchool;
    private Route route;
    private int routePosition;
    boolean cannotMove = false;

    private int age;
    private int sex;//male or female male = 0, female = 1
    private int pop_density;
    private boolean goToSchool = true;
    private Structure workDayDestination;//destination that the individual goes to every work

    public Resident(Int2D location, Household household, int sex, int age, boolean isUrban)
    {
        this.location = location;
        this.household = household;
        this.age = age;
        this.sex = sex;
        this.isUrban = isUrban;
    }

    @Override
    public void step(SimState state)
    {
        if(workDayDestination == null)
            return;
        EbolaABM ebolaSim = (EbolaABM) state;
        long cStep = ebolaSim.schedule.getSteps();
        if(cStep == 2 && !ebolaSim.updatedChart)
        {
            for(int i = 0; i < ebolaSim.roadDistanceHistogram.length; i++)
                ebolaSim.distribution.addValue((Number)ebolaSim.roadDistanceHistogram[i], "All distances", i);
            ebolaSim.updatedChart = true;
            System.out.println("Max route distance = " + ebolaSim.max_route_distance);
        }
        if(route == null && goToSchool && !cannotMove)
        {
            route = household.getRoute(this.workDayDestination);
            routePosition = 0;
            if(cStep == 0 && route != null)
            {
                ebolaSim.route_distance_sum += route.getTotalDistance();
                if((int)Math.round(Parameters.convertToKilometers(route.getTotalDistance())) < ebolaSim.roadDistanceHistogram.length)
                    ebolaSim.roadDistanceHistogram[(int)Math.round(Parameters.convertToKilometers(route.getTotalDistance()))]++;
                else
                    ebolaSim.roadDistanceHistogram[49]++;
                if(ebolaSim.max_route_distance < Parameters.convertToKilometers(route.getTotalDistance()))
                    ebolaSim.max_route_distance = Parameters.convertToKilometers(route.getTotalDistance());
                //System.out.println("Average distance = " + Parameters.convertToKilometers(ebolaSim.route_distance_sum / ++ebolaSim.route_distance_count));
            }
            //get path to school
            long t = System.currentTimeMillis();
//            if(!household.cachedPaths.containsKey(nearestSchool))
//            {
//                pathToDestination = AStar.astarPath(ebolaSim, household.getNearestNode(), nearestSchool.getNearestNode());
//                if(pathToDestination == null)
//                {
//                    cannotMove = true;
//                    household.addPath(nearestSchool, null);
//                    System.out.println((ebolaSim.roadLinks.getMBR().getMinX() + (household.location.getX()*0.000833333333)) + "," + (ebolaSim.roadLinks.getMBR().getMinY() + (((ebolaSim.world_height-household.location.getY())*0.000833333333))));
//                    System.out.println(cStep + " CANNOT Reach SCHOOL no_school_count =  " + ebolaSim.no_school_count++);
//                }
//                else
//                {
//                    household.addPath(nearestSchool, (LinkedList) pathToDestination.clone());
//                    //System.out.println("Found path with size " + pathToDestination.size());
//                }
//            }
//            else
//            {
//                pathToDestination = (LinkedList)household.getPath(nearestSchool);
//                if(pathToDestination != null)
//                    pathToDestination = (LinkedList)pathToDestination.clone();
//            }
            goToSchool = false;
            updatePositionOnMap(ebolaSim);
            return;

        }
        else if(route == null && !goToSchool && !cannotMove)
        {
            route = workDayDestination.getRoute(this.household);
            routePosition = 0;
            //if we found it go back to home
            //get path to school
//            long t = System.currentTimeMillis();
//            if(!nearestSchool.cachedPaths.containsKey(household))
//            {
//                nearestSchool.addPath(household, null);
//                pathToDestination = AStar.astarPath(ebolaSim, nearestSchool.getNearestNode(), household.getNearestNode());
//                nearestSchool.addPath(household, pathToDestination);
//                if(pathToDestination == null)
//                {
//                    cannotMove = true;
//                }
//                else
//                    nearestSchool.addPath(household, (LinkedList)pathToDestination.clone());
//
//            }
//            else
//            {
//                pathToDestination = (LinkedList)nearestSchool.getPath(household);
//                if(pathToDestination != null)
//                    pathToDestination = (LinkedList)pathToDestination.clone();
//            }

//                if(pathToDestination == null)
//                {
//                    System.out.println((ebolaSim.roadLinks.getMBR().getMinX() + (nearestSchool.location.getX()*0.000833333333)) + "," + (ebolaSim.roadLinks.getMBR().getMinY() + (((ebolaSim.world_height-nearestSchool.location.getY())*0.000833333333))));
//                    System.out.println(cStep + " CANNOT Reach SCHOOL no_school_count =  " + ebolaSim.no_school_count++);
//                }
            goToSchool = true;
            updatePositionOnMap(ebolaSim);
            return;
        }
        else if(route != null && route.getNumSteps() == routePosition)
        {
            route = null;
        }
        else if(route != null)
        {
            Int2D loc = route.getLocation(routePosition++);
            location = loc;
            if(route.getNumSteps() == routePosition)
                route = null;
            updatePositionOnMap(ebolaSim);
            return;
        }
        return;

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

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public Structure getWorkDayDestination() {
        return workDayDestination;
    }

    public void setWorkDayDestination(Structure workDayDestination) {
        this.workDayDestination = workDayDestination;
    }
}
