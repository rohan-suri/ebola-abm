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
    public static Continuous2D world;
    public static SparseGrid2D householdGrid;

    public static int pop_width;
    public static int pop_height;
    public static int total_scaled_pop = 0; //number of agents in the model (scaled from total_pop)
    public static int total_pop = 0; //actual population (not scaled)

    public EbolaABM(long seed)
    {
        super(seed);
    }

    @Override
    public void start()
    {
        super.start();
    }

    @Override
    public void finish()
    {
        super.finish();
    }

    public static void main(String[] args)
    {

    }

}
