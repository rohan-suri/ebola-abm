import com.sun.corba.se.impl.orb.ParserAction;
import org.jfree.data.category.DefaultCategoryDataset;
import sim.engine.SimState;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by rohansuri on 7/7/15.
 */
public class EbolaABM extends SimState
{
    public Continuous2D world;
    public SparseGrid2D householdGrid;
    public SparseGrid2D urbanAreasGrid;
    public SparseGrid2D schoolGrid;
    public Network roadNetwork = new Network();
    public GeomVectorField roadLinks;
    public SparseGrid2D allRoadNodes;
    public DoubleGrid2D road_cost; //accumalated cost to get to nearest node on the road network

    DefaultCategoryDataset roadNetworkDistribution = new DefaultCategoryDataset(); //dataset for seeing age groups of infected

    public Bag schools = new Bag();

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
        int i = 0;
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

}
