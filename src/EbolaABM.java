import com.sun.corba.se.impl.orb.ParserAction;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import ec.util.MersenneTwisterFast;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.xy.XYSeries;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.distribution.Poisson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by rohansuri on 7/7/15.
 */
public class EbolaABM extends SimState
{
    public Continuous2D world;
    public SparseGrid2D householdGrid;
    public SparseGrid2D urbanAreasGrid;
    public SparseGrid2D schoolGrid;
    public SparseGrid2D farmGrid;
    public Network roadNetwork = new Network();
    public GeomVectorField roadLinks;
    public SparseGrid2D allRoadNodes;
    public DoubleGrid2D road_cost; //accumalated cost to get to nearest node on the road network
    public IntGrid2D admin_id;//contains id for each location (939 x 990)

    DefaultCategoryDataset distribution = new DefaultCategoryDataset(); //dataset for seeing age groups of infected

    public Bag schools = new Bag();
    public Bag farms = new Bag();
    public ArrayList<Map<EbolaBuilder.Node, Structure>> workNodeStructureMap = new ArrayList<>();
    public Map<EbolaBuilder.Node, Structure> householdNodes = new HashMap<>(10000);
    public Map<Integer, List<MovementPattern>> movementPatternMapSLE = new HashMap<>();
    public Map<Integer, List<MovementPattern>> movementPatternMapLIB = new HashMap<>();
    public Map<Integer, List<MovementPattern>> movementPatternMapGIN = new HashMap<>();

    public Map<Integer, Bag> admin_id_sle_residents = new HashMap<>();
    public Map<Integer, Bag> admin_id_lib_residents = new HashMap<>();
    public Map<Integer, Bag> admin_id_gin_residents = new HashMap<>();

    public Map<Integer, List<Int2D>> admin_id_sle_urban = new HashMap<>();
    public Map<Integer, List<Int2D>> admin_id_lib_urban = new HashMap<>();
    public Map<Integer, List<Int2D>> admin_id_gin_urban = new HashMap<>();


    double max_distance = 0;
    double distance_sum = 0;
    int distance_count = 0;
    int no_school_count = 0;

    public int pop_width;
    public int pop_height;
    public int world_width;
    public int world_height;
    public int total_scaled_pop = 0; //number of agents in the model (scaled from total_pop)
    public long total_pop = 0; //actual population (not scaled)
    int total_urban_pop = 0;
    int lib_urban_pop = 0;
    int sl_urban_pop = 0;
    int guinea_urban_pop = 0;
    int total_lib_pop = 0;
    int total_sl_pop = 0;
    int total_guinea_pop = 0;
    int total_no_school_count = 0;

    double route_distance_count;
    double route_distance_sum;
    double max_route_distance;
    int[] roadDistanceHistogram = new int[50];
    boolean updatedChart = false;

    public int firstResidentHash;

    //xy series for health status
    public XYSeries totalsusceptibleSeries = new XYSeries("Susceptible"); // shows  number of Susceptible agents
    public XYSeries totalExposedSeries = new XYSeries("Exposed");
    public XYSeries totalInfectedSeries = new XYSeries(" Infected"); //shows number of infected agents
    public XYSeries totalRecoveredSeries = new XYSeries(" Recovered"); // shows number of recovered agents
    public XYSeries totalDeadSeries = new XYSeries(" Dead"); // shows number of recovered agents


    // timer graphics
    DefaultValueDataset hourDialer = new DefaultValueDataset(); // shows the current hour
    DefaultValueDataset dayDialer = new DefaultValueDataset(); // counts

    public Bag residents;

    public EbolaABM(long seed)
    {
        super(seed);
    }

    @Override
    public void start()
    {
        super.start();
        residents = new Bag();
        EbolaBuilder.initializeWorld(this, Parameters.POP_PATH, Parameters.ADMIN_PATH, Parameters.AGE_DIST_PATH);

        Steppable chartUpdater = new Steppable()
        {
            @Override
            public void step(SimState simState)
            {
//                long cStep = simState.schedule.getSteps();
//
//                Bag allResidents = world.getAllObjects();
//                int total_sus = 0;
//                int total_infectious = 0;
//                int total_recovered = 0;
//                int total_exposed = 0;
//                int total_dead = 0;
//                for(Object o: allResidents)
//                {
//                    Resident resident = (Resident)o;
//                    if(resident.getHealthStatus() == Constants.SUSCEPTIBLE)
//                        total_sus++;
//                    else if(resident.getHealthStatus() == Constants.EXPOSED)
//                        total_exposed++;
//                    else if(resident.getHealthStatus() == Constants.INFECTIOUS)
//                        total_infectious++;
//                    else if(resident.getHealthStatus() == Constants.RECOVERED)
//                        total_recovered++;
//                    else if(resident.getHealthStatus() == Constants.DEAD)
//                        total_dead++;
//                }
//
//                //update health chart
//                totalsusceptibleSeries.add(cStep*Parameters.TEMPORAL_RESOLUTION, total_sus);//every hour
//                totalInfectedSeries.add(cStep*Parameters.TEMPORAL_RESOLUTION, total_infectious);//every hour
//                totalDeadSeries.add(cStep*Parameters.TEMPORAL_RESOLUTION, total_dead);//every hour
//                totalExposedSeries.add(cStep*Parameters.TEMPORAL_RESOLUTION, total_exposed);//every hour
//
//                //update hourDialer and day Dialer
//                double day = cStep*Parameters.TEMPORAL_RESOLUTION/24;
//                double hour = cStep*Parameters.TEMPORAL_RESOLUTION%24;
//                hourDialer.setValue(hour);
//                dayDialer.setValue(day);
            }
        };
        this.schedule.scheduleRepeating(chartUpdater);

        Steppable movementManager = new Steppable()
        {
            @Override
            public void step(SimState simState)
            {
                long cStep = simState.schedule.getSteps();
                if(cStep % Math.round(24.0/Parameters.TEMPORAL_RESOLUTION) == 0)//only do this on the daily
                {
                    long now = System.currentTimeMillis();
                    EbolaABM ebolaSim = (EbolaABM)simState;
                    System.out.println("GIN");
                    moveResidents(ebolaSim.movementPatternMapGIN, ebolaSim.admin_id_gin_residents, ebolaSim.random, ebolaSim);
                    System.out.println("SLE");
                    moveResidents(ebolaSim.movementPatternMapSLE, ebolaSim.admin_id_sle_residents, ebolaSim.random, ebolaSim);
                    System.out.println("LIB");
                    moveResidents(ebolaSim.movementPatternMapLIB, ebolaSim.admin_id_lib_residents, ebolaSim.random, ebolaSim);
                    System.out.println("Managing population flow [" + (System.currentTimeMillis()-now)/1000 + " sec]");
                }
            }
            private void moveResidents(Map<Integer, List<MovementPattern>> movementPatternMap, Map<Integer, Bag> admin_id_residents, MersenneTwisterFast random, EbolaABM ebolaSim)
            {
                Iterator<Integer> it = movementPatternMap.keySet().iterator();
                while(it.hasNext())
                {
                    int key = it.next();
                    List<MovementPattern> list = movementPatternMap.get(key);
                    for(MovementPattern mp: list)
                    {
                        Poisson poisson = new Poisson(mp.annual_amnt/365.0*Parameters.POPULATION_FLOW_SCALE, random);
                        int move_num = poisson.nextInt();
                        //System.out.println("Moving " + move_num + " people w/ mean of " + mp.annual_amnt/365.0);
                        if(move_num > 0)
                        {
                            //System.out.println(mp.source_admin + " " + key);
                            Bag residents = admin_id_residents.get(mp.source_admin);
                            if(residents == null)
                            {
                                System.out.println("NO RESIDENTS IN DISTRICT " + mp.source_admin);
                                return;
                            }
                            while(move_num > 0)
                            {
                                Resident randomResident;
                                int count = 0;
                                do
                                {
                                    randomResident = (Resident)residents.get(random.nextInt(residents.size()));
                                    count++;
                                    if(count > 1000)//timeout to ensure we don't infinitely loop
                                        return;
                                }while(!residentGood(randomResident, ebolaSim) && !randomResident.moveResidency(mp.to_admin, ebolaSim));
                                if(randomResident != null)
                                {
                                    residents.remove(randomResident);
                                    randomResident.moveResidency(mp.to_admin, ebolaSim);
                                }
                                move_num--;
                            }
                        }
                    }
                }
            }

            private boolean residentGood(Resident resident, EbolaABM ebolaSim)
            {
                if(resident.getAge() < 15)
                    return false;
                double rand = ebolaSim.random.nextDouble();
                if(rand < 0.2 && !resident.getIsUrban())
                    return false;
                return true;
            }

        };



        this.schedule.scheduleRepeating(movementManager);
    }

    @Override
    public void finish()
    {
        super.finish();
    }

    public static void main(String[] args)
    {
        doLoop(EbolaABM.class, args);
        System.exit(0);
    }

    public static class MovementPattern
    {
        public int source_admin;
        public int to_admin;
        public double annual_amnt;
        public Int2D destination;
    }
}
