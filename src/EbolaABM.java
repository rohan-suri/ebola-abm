import com.sun.corba.se.impl.orb.ParserAction;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
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


    public int pop_width;
    public int pop_height;
    public int total_scaled_pop = 0; //number of agents in the model (scaled from total_pop)
    public int total_pop = 0; //actual population (not scaled)
    int urban_pop = 0;
    int rural_pop = 0;

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
        EbolaBuilder.initializeWorld(this, Parameters.POP_PATH);
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
