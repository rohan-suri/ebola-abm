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

    private Structure goal;
    private double atGoalLength;

    private int sector_id;//sector works in as defined in Constants, -1 for no sector (not working)
    private boolean employed;
    private int dailyWorkHours;

    public Resident(Int2D location, Household household, int sex, int age, boolean isUrban)
    {
        this.location = location;
        this.household = household;
        this.age = age;
        this.sex = sex;
        this.isUrban = isUrban;
        this.sector_id = -1;//set default to no sector
        this.employed = false;//default isfalse
    }

    @Override
    public void step(SimState state)
    {
        if(workDayDestination == null)
            return;
        EbolaABM ebolaSim = (EbolaABM) state;
        long cStep = ebolaSim.schedule.getSteps();

//        if(ebolaSim.firstResidentHash == 0  && workDayDestination instanceof WorkLocation)
//            ebolaSim.firstResidentHash = this.hashCode();
//        if(this.hashCode() == ebolaSim.firstResidentHash)
//            System.out.println("FOUDN ASLKDFJASFJ");

        //check if we have a goal
        if(goal == null)//calc goal
        {
            calcGoal(cStep, ebolaSim);
        }
        if(goal != null)
        {
            if(this.location.distance(goal.getLocation()) == 0)//we are at goal
            {
                if(this.location.distance(household.getLocation()) != 0)//make sure we are not at home
                {
                    if (atGoalLength < 0) {
                        //go back home
                        setGoal(this.goal, household, 100);
                    }
                    atGoalLength -= 1*Parameters.TEMPORAL_RESOLUTION ;
                }
                else
                {
                    goal = null;
                }
            }
            else//if we aren't at goal just move towards it
            {
                if(routePosition < route.getNumSteps())
                {
                    Int2D nextStep = route.getLocation(routePosition++);
                    this.setLocation(nextStep);
                    updatePositionOnMap(ebolaSim);
                }
            }
        }

//        if(route == null && goToSchool && !cannotMove)
//        {
//            route = household.getRoute(this.workDayDestination);
//            routePosition = 0;
//            if(cStep == 0 && route != null)
//            {
//                ebolaSim.route_distance_sum += route.getTotalDistance();
//                if((int)Math.round(Parameters.convertToKilometers(route.getTotalDistance())) < ebolaSim.roadDistanceHistogram.length)
//                    ebolaSim.roadDistanceHistogram[(int)Math.round(Parameters.convertToKilometers(route.getTotalDistance()))]++;
//                else
//                    ebolaSim.roadDistanceHistogram[49]++;
//                if(ebolaSim.max_route_distance < Parameters.convertToKilometers(route.getTotalDistance()))
//                    ebolaSim.max_route_distance = Parameters.convertToKilometers(route.getTotalDistance());
//                //System.out.println("Average distance = " + Parameters.convertToKilometers(ebolaSim.route_distance_sum / ++ebolaSim.route_distance_count));
//            }
//            goToSchool = false;
//            updatePositionOnMap(ebolaSim);
//            return;
//
//        }
//        else if(route == null && !goToSchool && !cannotMove)
//        {
//            route = workDayDestination.getRoute(this.household);
//            routePosition = 0;
//            goToSchool = true;
//            updatePositionOnMap(ebolaSim);
//            return;
//        }
//        else if(route != null && route.getNumSteps() == routePosition)
//        {
//            route = null;
//        }
//        else if(route != null)
//        {
//            Int2D loc = route.getLocation(routePosition++);
//            location = loc;
//            if(route.getNumSteps() == routePosition)
//                route = null;
//            updatePositionOnMap(ebolaSim);
//            return;
//        }
//        return;

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

    private void calcGoal(long cStep, EbolaABM ebolaSim)
    {
        int dayOfWeek = (int)((cStep*Parameters.TEMPORAL_RESOLUTION)/24%7);
        if(dayOfWeek < 5)//weekday
        {
            int hourOfDay = (int)((cStep*Parameters.TEMPORAL_RESOLUTION)%24);
            if(hourOfDay > 8 && hourOfDay < 14)
            {
                double rand = ebolaSim.random.nextDouble();
                if(rand < 0.7)
                {
                    setGoal(this.getHousehold(), workDayDestination, dailyWorkHours);
                }
            }

        }
    }

    private void setGoal(Structure from, Structure to, int stayDuration)
    {
        this.goal = to;
        this.atGoalLength = stayDuration;
        this.route = from.getRoute(to);
        this.routePosition = 0;
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

    public int getSector_id() {
        return sector_id;
    }

    public void setSector_id(int sector_id) {
        this.sector_id = sector_id;
    }

    public boolean isEmployed() {
        return employed;
    }

    public void setEmployed(boolean employed) {
        this.employed = employed;
    }

    public int getDailyWorkHours() {
        return dailyWorkHours;
    }

    public void setDailyWorkHours(int dailyWorkHours) {
        this.dailyWorkHours = dailyWorkHours;
    }
}
