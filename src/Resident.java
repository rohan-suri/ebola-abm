import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rohansuri on 7/7/15.
 */
public class Resident implements Steppable
{
    private Int2D location;
    private boolean inactive;

    private Household household;
    private ETC myETC;
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

    private int healthStatus;

    private boolean isMoving = false;
    private boolean shouldApplyToETC = false;
    private int isMovingCountdown = 1;

    boolean doomed_to_die = false;
    double time_to_resolution = -1;
    double time_to_infectious = -1;
    double time_to_apply_to_etc = -1;
    int infected_count = 0;
	int day_infected = 0;

    private int daysFollowedUp = 0;
    private boolean hasBeenFollowedUp = false;

    //this residents disease model parameters
    private double SUSCEPTIBLE_TO_EXPOSED;
    private double INCUBATION_PERIOD_AVERAGE;
    private double INCUBATION_PERIOD_STDEV;
    private double CASE_FATALITY_RATIO;
    private double RECOVERY_PERIOD_AVERAGE;
    private double RECOVERY_PERIOD_STDEV;
    private double FATALITY_PERIOD_AVERAGE;
    private double FATALITY_PERIOD_STDEV;

    private int religion;

    public Resident(Int2D location, Household household, int sex, int age, boolean isUrban)
    {
        this.location = location;
        this.household = household;
        this.age = age;
        this.sex = sex;
        this.isUrban = isUrban;
        this.sector_id = -1;//set default to no sector
        this.employed = false;//default isfalse
        this.healthStatus = Constants.SUSCEPTIBLE;

        //set current status of disease parameters
        SUSCEPTIBLE_TO_EXPOSED = Parameters.SUSCEPTIBLE_TO_EXPOSED;
        INCUBATION_PERIOD_AVERAGE = Parameters.INCUBATION_PERIOD_AVERAGE;
        INCUBATION_PERIOD_STDEV = Parameters.INCUBATION_PERIOD_STDEV;
        CASE_FATALITY_RATIO = Parameters.CASE_FATALITY_RATIO;
        RECOVERY_PERIOD_AVERAGE = Parameters.RECOVERY_PERIOD_AVERAGE;
        RECOVERY_PERIOD_STDEV = Parameters.RECOVERY_PERIOD_STDEV;
        FATALITY_PERIOD_AVERAGE = Parameters.FATALITY_PERIOD_AVERAGE;
        FATALITY_PERIOD_STDEV = Parameters.FATALITY_PERIOD_STDEV;
    }

    @Override
    public void step(SimState state)
    {
        if(healthStatus == Constants.DEAD)
            return;

        EbolaABM ebolaSim = (EbolaABM) state;
        long cStep = ebolaSim.schedule.getSteps();

        if(healthStatus == Constants.EXPOSED)
        {
            if(time_to_infectious == -1)//time to infectious has not been determined yet
            {
                time_to_infectious = ((ebolaSim.random.nextGaussian()*INCUBATION_PERIOD_STDEV)+INCUBATION_PERIOD_AVERAGE)*24.0 * Parameters.TEMPORAL_RESOLUTION;

                //decide whether you will die or stay alive
                double rand = ebolaSim.random.nextDouble();
                if(rand < CASE_FATALITY_RATIO)
                    doomed_to_die = true;//this case will die
                else
                    doomed_to_die = false;//this case will recover

                //update the hotspots
                if(ebolaSim.hotSpotsGrid.getObjectsAtLocation(location.getX()/10, location.getY()/10) == null)
                    ebolaSim.hotSpotsGrid.setObjectLocation(new Object(), location.getX()/10, location.getY()/10);
            }
            else if(time_to_infectious <= 0)//now become infectious
            {
                synchronized (ebolaSim)
                {
                    ebolaSim.total_infectious++;
                    ebolaSim.total_exposed--;
                }
                this.setHealthStatus(Constants.INFECTIOUS);
            }
            else if(!isMoving())
                time_to_infectious--;

        }
        else if(healthStatus == Constants.INFECTIOUS)//infect everyone!!!
        {
            if(time_to_resolution == -1 && shouldApplyToETC)
            {
                time_to_apply_to_etc = ((ebolaSim.random.nextGaussian()*Parameters.STD_TIME_TO_REPORT)+Parameters.AVG_TIME_TO_REPORT)*24.0 * Parameters.TEMPORAL_RESOLUTION;
            }
            else if (shouldApplyToETC && time_to_apply_to_etc <= 0)
            {
                //apply to etc here
                ETC etc = ebolaSim.myETCmanager.getNearestETC(this);
                if(etc != null)//TODO apply everyday
                {
                    etc.requestAdmission(this);
                    shouldApplyToETC = false;
                }
                //apply for contact tracing

            }
            else if (shouldApplyToETC)
                time_to_apply_to_etc--;

            if(doomed_to_die && time_to_resolution == -1)
            {
				day_infected = ((int)cStep)/24;
                //decide to kill or be recovered
                time_to_resolution = ((ebolaSim.random.nextGaussian()*FATALITY_PERIOD_STDEV)+FATALITY_PERIOD_AVERAGE)*24.0 * Parameters.TEMPORAL_RESOLUTION;
            }
            else if(time_to_resolution == -1)
            {
                //set date infected
                day_infected = ((int)cStep)/24;
                //decide when to recover
                time_to_resolution = ((ebolaSim.random.nextGaussian()*RECOVERY_PERIOD_STDEV)+RECOVERY_PERIOD_AVERAGE)*24.0 * Parameters.TEMPORAL_RESOLUTION;
            }
            else if(time_to_resolution <= 0)
            {
                if (doomed_to_die) {
                    setHealthStatus(Constants.DEAD);
                    ebolaSim.total_infectious--;

                    //request Burial and contact traced
                    if(time_to_apply_to_etc > 0)
                    {
                        //TODO apply to get buried and contact traced
                    }
                } else {
                    setHealthStatus(Constants.RECOVERED);
                    ebolaSim.total_infectious--;
                }
                //add your infected count to the xyseries
                synchronized (ebolaSim.effectiveReproductiveRates.get(this.getHousehold().getCountry()))
                {
                    ebolaSim.effectiveReproductiveRates.get(this.getHousehold().getCountry()).add(day_infected, infected_count);
                }
            }
            else if(!isMoving())//TODO remove this condition
            {
                time_to_resolution--;
                time_to_apply_to_etc--;
            }

            //now infect nearby people
            Bag nearByPeople = ebolaSim.world.getNeighborsWithinDistance(new Double2D(location), 1);

            //Determine current structure TODO move this to its own method
            Structure currentStructure = null;//null if traveling
            if(location.equals(household.getLocation()))
                currentStructure = household;
            else if(workDayDestination != null && location.equals(workDayDestination.getLocation()))
                currentStructure = workDayDestination;
            else if(myETC != null && location.equals(myETC.getLocation()))
                currentStructure = myETC;
            if(nearByPeople == null)//if you are nearby no one just return
                return;
            for(Object o: nearByPeople)
            {
                Resident resident = (Resident)o;
                if(resident.getHealthStatus() == Constants.SUSCEPTIBLE)
                {
                    if(!Parameters.INFECT_ONLY_YOUR_STRUCTURE || (currentStructure != null && currentStructure.getMembers().contains(resident)))
                    {
                        double rand = ebolaSim.random.nextDouble();
                        //incorporate awareness if needed
                        double contact_rate = SUSCEPTIBLE_TO_EXPOSED;
                        if(Parameters.AWARENESS_ON && ((int)cStep)/24 > Parameters.AWARENESS_START)
                        {
                            double awareness = Parameters.calcAwareness(calcNetworkSick());
                            contact_rate = Parameters.calcReducedProb(awareness);
                        }
                        if(rand < (contact_rate))//infect this agent
                       	{
                            resident.setHealthStatus(Constants.EXPOSED);
                            synchronized (ebolaSim)
                            {
                                ebolaSim.total_exposed++;
                                infected_count++;
                                //update the tally for each region
                                if(!ebolaSim.adminInfectedTotals.get(resident.getHousehold().getCountry()).containsKey(resident.getHousehold().getAdmin_id()))
                                    ebolaSim.adminInfectedTotals.get(resident.getHousehold().getCountry()).put(resident.getHousehold().getAdmin_id(), 0);
                                ebolaSim.adminInfectedTotals.get(resident.getHousehold().getCountry())
                                    .put(resident.getHousehold().getAdmin_id(), ebolaSim.adminInfectedTotals.get(resident.getHousehold().getCountry()).get(resident.getHousehold().getAdmin_id())+1);

                                //update the tally for each country
                                if(resident.getHousehold().getCountry() == Parameters.LIBERIA)
                                    ebolaSim.totalLiberiaInt++;
                                else if(resident.getHousehold().getCountry() == Parameters.SL)
                                    ebolaSim.totalSierra_LeoneInt++;
                                else if(resident.getHousehold().getCountry() == Parameters.GUINEA)
                                    ebolaSim.totalGuineaInt++;
                            }
                        }
                    }
                }
            }

        }
        if(workDayDestination == null || myETC != null)
            return;


//        if(ebolaSim.firstResidentHash == 0  && cStep > 3 && this.isMoving())
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
            if(this.location.equals(goal.getLocation()))//we are at goal
            {
                if(!this.location.equals(household.getLocation()))//make sure we are not at home
                {
                    if (atGoalLength < 0) {
                        //go back home
                        setGoal(this.goal, household, 100, Parameters.WALKING_SPEED);
                    }
                    atGoalLength -= 1*Parameters.TEMPORAL_RESOLUTION ;
                }
                else
                {
                    if(isMoving)
                    {
                        //arrived after traveling
                        isMoving = false;
                    }
                    goal = null;
                }
            }
            else//if we aren't at goal just move towards it
            {
                if(route == null)
                    return;
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
        long days_since_start = cStep/24;
        int dayOfWeek = (int)((cStep*Parameters.TEMPORAL_RESOLUTION)/24%7);
        if(dayOfWeek < 5)//weekday
        {
            int hourOfDay = (int)((cStep*Parameters.TEMPORAL_RESOLUTION)%24);
            if(hourOfDay > 8 && hourOfDay < 14)
            {
                double rand = ebolaSim.random.nextDouble();
                if(rand < 0.7)
                {
                    //check to make sure schools are open
                    if(workDayDestination instanceof School)//we are a student
                    {
                        boolean schools_closed = Parameters.CLOSE_SCHOOLS && days_since_start >= Parameters.CLOSE_SCHOOLS_START;
                        if(!schools_closed)
                            setGoal(this.getHousehold(), workDayDestination, dailyWorkHours, Parameters.WALKING_SPEED);
                    }
                    else
                        setGoal(this.getHousehold(), workDayDestination, dailyWorkHours, Parameters.WALKING_SPEED);
                }
            }

        }
    }

    private void setGoal(Structure from, Structure to, int stayDuration, double speed)
    {
        this.goal = to;
        this.atGoalLength = stayDuration;
        if(speed < 20)
            this.route = from.getRoute(to, speed);
        this.routePosition = 0;
    }

    public void updatePositionOnMap(EbolaABM ebolaSim)
    {
        double randX = ebolaSim.random.nextDouble();
        double randY = ebolaSim.random.nextDouble();
        ebolaSim.world.setObjectLocation(this, new Double2D(location.getX() + randX, location.getY() + randY));
        ebolaSim.worldPopResolution.setObjectLocation(this, location.getX()/10, location.getY()/10);
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

    public int getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(int healthStatus) {
        int old_health_status = this.healthStatus;
        this.healthStatus = healthStatus;
        if(old_health_status == Constants.EXPOSED && this.healthStatus == Constants.INFECTIOUS) {
            if(Parameters.ETC_ON)
            {
                shouldApplyToETC = true;
            }
        }
    }

    public int getReligion() {
        return religion;
    }

    public void setReligion(int religion) {
        this.religion = religion;
    }
	//returns the percentage of agents in this agent's house or workplace
	public double calcNetworkSick()
	{
		HashSet<Resident> residents = household.getMembers();
		if(workDayDestination != null)
			residents.addAll(workDayDestination.getMembers());//TODO Make sure there are actually people in this members
		int infected_count = 0;
		for(Resident resident: residents)
			if(resident.getHealthStatus() != Constants.SUSCEPTIBLE || resident.getHealthStatus() != Constants.EXPOSED)
				infected_count++;
		return residents.size()*1.0/infected_count;
			
	}

    public Set<Resident> getAllContacts()
    {
        HashSet<Resident> residents = household.getMembers();
        if(workDayDestination != null)
            residents.addAll(workDayDestination.getMembers());//TODO Make sure there are actually people in this members
        return residents;
    }

    /**
     * @param newAdminId
     * @param ebolaSim
     * @return true when route is not null, if route is null this person cannot move and stays and returns false
     */
    public boolean moveResidency(int newAdminId, int to_country, EbolaABM ebolaSim)
    {
        //first pick a location for the new house
        int old_country = this.getHousehold().getCountry();
        List<Int2D> urban_locations = null;
        if(to_country == Parameters.GUINEA)
            urban_locations = ebolaSim.admin_id_gin_urban.get(newAdminId);
        else if(to_country == Parameters.SL)
            urban_locations = ebolaSim.admin_id_sle_urban.get(newAdminId);
        else if(to_country == Parameters.LIBERIA)
            urban_locations = ebolaSim.admin_id_lib_urban.get(newAdminId);

        //pick a random urban location
        if(urban_locations == null )
        {
            //System.out.println("NO URBAN LOCATIONS!!! on id " + newAdminId);
            return true;
        }
        Int2D urban_location = urban_locations.get(ebolaSim.random.nextInt(urban_locations.size()));

        //now that we have determined an urban location pick a resident in this location to live with
        Bag residentsInUrbanArea = ebolaSim.worldPopResolution.getObjectsAtLocation(urban_location);
        if(residentsInUrbanArea == null)
        {
            //System.out.println("Looking at urban area = " + urban_location);
            //System.out.println("Couldn't find anyone in this urban_location!");
            return false;
        }
        //randomly pick someone
        Resident residentToMoveInWith;
        do
        {
            int ran = ebolaSim.random.nextInt(residentsInUrbanArea.size());
            residentToMoveInWith = (Resident)residentsInUrbanArea.get(ran);
            residentsInUrbanArea.remove(ran);

        } while(residentToMoveInWith.getWorkDayDestination() == null && residentsInUrbanArea.size() > 0);

        if(residentToMoveInWith.getWorkDayDestination() == null)//TODO getting Nullpointer Exception
            return false;

        Household newHousehold = residentToMoveInWith.getHousehold();



        if(workDayDestination == null)// || newHousehold.getRoute(this.household, 50.0) == null)
        {
            //bail out we can't get to it
            //but first we must remove the link we just made
            return false;
        }

        //update bag
        //TODO make urban/rural population ratio stay the same.  Since all people move to urban areas and only some people go from urban.  Net flow is towards urban areas.
        //used for movement flow
        int country = household.getCountry();
        addToAdminIdMap(ebolaSim, old_country, true);//remove yourself from the old one
        addToAdminIdMap(ebolaSim, country, false);//add yourself to the new one
        if(getIsUrban())
            addToAdminIdMapUrban(ebolaSim, old_country, true);//remove from the old one (urban)
        addToAdminIdMapUrban(ebolaSim, country, false);//add to the old one ()

        isMoving = true;
        isMovingCountdown = 1;
//        if(ebolaSim.firstResidentHash == 0  && workDayDestination instanceof WorkLocation && isMoving())
//            ebolaSim.firstResidentHash = this.hashCode();

        //be sure to add teh household to the grid
        ebolaSim.householdGrid.setObjectLocation(newHousehold, newHousehold.getLocation());

        //update goal
        setGoal(this.getHousehold(), newHousehold, 0, 50.0);
        ArrayList<Int2D> path = new ArrayList<>();
        path.add(newHousehold.getLocation());
        this.route = new Route(path, this.getLocation().distance(this.getHousehold().getLocation()), getHousehold().getNearestNode(), newHousehold.getNearestNode(), 10000);
        setHousehold(newHousehold);

        //find work near your new household
        if(isEmployed())
            EbolaBuilder.setWorkDestination(this);

        return true;
    }

    public void addToAdminIdMap(EbolaABM ebolaSim, int country, boolean remove)
    {
        if(country == Parameters.SL)
        {
            Bag residents;
            if(!ebolaSim.admin_id_sle_residents.containsKey(getHousehold().getAdmin_id()))
                residents = ebolaSim.admin_id_sle_residents.put(getHousehold().getAdmin_id(), new Bag());
            residents = ebolaSim.admin_id_sle_residents.get(getHousehold().getAdmin_id());
            if(remove)
                residents.remove(this);
            else
                residents.add(this);
        }
        else if(country == Parameters.GUINEA)
        {
            Bag residents;
            if(!ebolaSim.admin_id_gin_residents.containsKey(getHousehold().getAdmin_id()))
                residents = ebolaSim.admin_id_gin_residents.put(getHousehold().getAdmin_id(), new Bag());
            residents = ebolaSim.admin_id_gin_residents.get(getHousehold().getAdmin_id());
            if(remove)
                residents.remove(this);
            else
                residents.add(this);
        }
        else if(country == Parameters.LIBERIA)
        {
            Bag residents;
            if(!ebolaSim.admin_id_lib_residents.containsKey(getHousehold().getAdmin_id()))
                residents = ebolaSim.admin_id_lib_residents.put(getHousehold().getAdmin_id(), new Bag());
            residents = ebolaSim.admin_id_lib_residents.get(getHousehold().getAdmin_id());
            if(remove)
                residents.remove(this);
            else
                residents.add(this);
        }
    }

    public void addToAdminIdMapUrban(EbolaABM ebolaSim, int country, boolean remove)
    {
        if(country == Parameters.SL)
        {
            Bag residents;
            if(!ebolaSim.admin_id_sle_urban_residents.containsKey(getHousehold().getAdmin_id()))
                residents = ebolaSim.admin_id_sle_residents.put(getHousehold().getAdmin_id(), new Bag());
            residents = ebolaSim.admin_id_sle_residents.get(getHousehold().getAdmin_id());
            if(remove)
                residents.remove(this);
            else
                residents.add(this);
        }
        else if(country == Parameters.GUINEA)
        {
            Bag residents;
            if(!ebolaSim.admin_id_gin_urban_residents.containsKey(getHousehold().getAdmin_id()))
                residents = ebolaSim.admin_id_gin_urban_residents.put(getHousehold().getAdmin_id(), new Bag());
            residents = ebolaSim.admin_id_gin_urban_residents.get(getHousehold().getAdmin_id());
            if(remove)
                residents.remove(this);
            else
                residents.add(this);
        }
        else if(country == Parameters.LIBERIA)
        {
            Bag residents;
            if(!ebolaSim.admin_id_lib_urban_residents.containsKey(getHousehold().getAdmin_id()))
                residents = ebolaSim.admin_id_lib_urban_residents.put(getHousehold().getAdmin_id(), new Bag());
            residents = ebolaSim.admin_id_lib_urban_residents.get(getHousehold().getAdmin_id());
            if(remove)
                residents.remove(this);
            else
                residents.add(this);
        }
    }

    public boolean isMoving()
    {
        return isMoving;
    }

    /**
     * Moves this agent to this ETC, changes disease model parameters and prevents the agent from participating in daily activities
     * @param myETC
     */
    public void admittedToETC(ETC myETC)
    {
        //set disease parameters to the ETC
        this.SUSCEPTIBLE_TO_EXPOSED = Parameters.ETC_SUSCEPTIBLE_TO_EXPOSED;
        this.CASE_FATALITY_RATIO = Parameters.CASE_FATALITY_RATIO;

        //now move locations to the ETC
        this.setLocation(myETC.getLocation());
        this.myETC = myETC;
    }

    public void dischargeFromETC()
    {
        //set myETC to null
        this.myETC = null;

        //move back to your household
        this.setLocation(this.getHousehold().getLocation());
    }

    int getFollowedUpDays()
    {
        return daysFollowedUp;
    }

    void incrementFollowUpDays()
    {
        daysFollowedUp++;
    }

    public void setFollowedUp(boolean _followedUp)
    {
        hasBeenFollowedUp = _followedUp;
    }

    public boolean hasBeenFollowedUp()
    {
        return hasBeenFollowedUp;
    }
}
