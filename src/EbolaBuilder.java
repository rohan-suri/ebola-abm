import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by rohansuri on 7/8/15.
 */
public class EbolaBuilder
{
    public static EbolaABM ebolaSim;

    public static void initializeWorld(EbolaABM sim, String pop_file)
    {
        ebolaSim = sim;

        addHousesAndResidents(pop_file);
    }

    private static void addHousesAndResidents(String pop_file)
    {
        try
        {
            // buffer reader - read ascii file
            BufferedReader land = new BufferedReader(new FileReader(pop_file));
            String line;

            // first read the dimensions
            line = land.readLine(); // read line for width
            String[] tokens = line.split("\\s+");
            int width = Integer.parseInt(tokens[1]);
            ebolaSim.pop_width = width;

            line = land.readLine();
            tokens = line.split("\\s+");
            int height = Integer.parseInt(tokens[1]);
            ebolaSim.pop_height = height;

            //instantiate world to hold agents
            ebolaSim.world = new Continuous2D(Parameters.WORLD_DISCRETIZTION, width*Parameters.WORLD_TO_POP_SCALE, height*Parameters.WORLD_TO_POP_SCALE);
            //instantiate grid to hold houses
            ebolaSim.householdGrid = new SparseGrid2D((int)(width*Parameters.WORLD_TO_POP_SCALE), (int)(height*Parameters.WORLD_TO_POP_SCALE));

            for(int i = 0; i < 4; i++)//skip the next couple of lines (contain useless metadata)
                land.readLine();

            int max_pop_density = 0;
            for(int i = 0; i < width; i++)
            {
                line = land.readLine();
                tokens = line.split("\\s+");
                for(int j = 0; j < tokens.length; j++)
                {
                    if(tokens[j] .equals(""))
                        continue;
                    int num_people = Integer.parseInt(tokens[j]);
                    if(num_people > 0)
                    {
                        ebolaSim.total_pop += num_people;

                        if(num_people > max_pop_density)
                            max_pop_density = num_people;
                        num_people = scale(num_people, Parameters.SCALE);//Scale the number of agents to reduce size of simulation

                        ebolaSim.total_scaled_pop += num_people;


                        while(num_people > 0)
                        {
                            Household h = new Household();

                            int x_coord, y_coord;
                            do
                            {
                                y_coord = (i*Parameters.WORLD_TO_POP_SCALE) + (int)(ebolaSim.random.nextDouble() * Parameters.WORLD_TO_POP_SCALE);
                                x_coord = (j*Parameters.WORLD_TO_POP_SCALE) + (int)(ebolaSim.random.nextDouble() * Parameters.WORLD_TO_POP_SCALE);

                            } while (false);//ebolaSim.householdGrid.getObjectsAtLocation(x_coord, y_coord) != null);

                            ebolaSim.householdGrid.setObjectLocation(h, new Int2D(x_coord, y_coord));
                            //System.out.println("House location: " + x_coord + ", " + y_coord);
                            int household_size  = pickHouseholdSize();

                            for(int m = 0; m < household_size; m++)
                            {
                                if(num_people == 0)
                                    break;
                                num_people--;
                                Resident r = new Resident();
                                ebolaSim.schedule.scheduleRepeating(r);
                                r.household = h;
                                ebolaSim.world.setObjectLocation(r, new Double2D(x_coord + ebolaSim.random.nextDouble(), y_coord + ebolaSim.random.nextDouble()));
                                h.addMember(r);
                            }
                        }
                    }
                }
            }

            System.out.println("total scaled pop = " + ebolaSim.total_scaled_pop);
            System.out.println("total pop = " + ebolaSim.total_pop);
            System.out.println("expected scaled pop = " + ebolaSim.total_pop*1.0*Parameters.SCALE);
            System.out.println("max_pop_density = " + max_pop_density);
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Scales integer values and rounds the number appropiately so aggregate scaling is similar to segregated scaling.
     * @param val The value that neeeds scaling.  Must be an int
     * @param scalar The percentage to scale normally a ndouble from 0-1
     * @return the value scaled
     */
    public static int scale(int val, double scalar)
    {
        int scaled = 0;
        double val_scaled = val*scalar;
        scaled = (int)val_scaled;
        val_scaled -= (int)val_scaled;
        if(ebolaSim.random.nextDouble() < val_scaled)
            scaled += 1;
        return scaled;
    }

    public static int pickHouseholdSize()
    {
        double average = Parameters.LIB_AVG_HOUSEHOLD_SIZE;
        double stdv = Parameters.LIB_HOUSEHOLD_STDEV;


    }
}
