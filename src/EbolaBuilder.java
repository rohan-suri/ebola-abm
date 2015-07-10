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
            String[] curr_tokens = line.split("\\s+");
            String[] prev_tokens = null;
            String[] next_tokens = null;
            int width = Integer.parseInt(curr_tokens[1]);
            ebolaSim.pop_width = width;

            line = land.readLine();
            curr_tokens = line.split("\\s+");
            int height = Integer.parseInt(curr_tokens[1]);
            ebolaSim.pop_height = height;

            //instantiate world to hold agents
            ebolaSim.world = new Continuous2D(Parameters.WORLD_DISCRETIZTION, width*Parameters.WORLD_TO_POP_SCALE, height*Parameters.WORLD_TO_POP_SCALE);
            //instantiate grid to hold houses
            ebolaSim.householdGrid = new SparseGrid2D((int)(width*Parameters.WORLD_TO_POP_SCALE), (int)(height*Parameters.WORLD_TO_POP_SCALE));
            ebolaSim.urbanAreasGrid = new SparseGrid2D((int)(width), (int)(height));

            for(int i = 0; i < 4; i++)//skip the next couple of lines (contain useless metadata)
                land.readLine();
            line = land.readLine();
            curr_tokens = line.split("\\s+");
            int max_pop_density = 0;
            for(int i = 0; i < height; i++)
            {
                if(i != 0)
                {
                    prev_tokens = curr_tokens;
                    curr_tokens = next_tokens;
                }

                if(i != width - 1)
                {
                    line = land.readLine();
                    next_tokens = line.split("\\s+");
                }

                for(int j = 0; j < curr_tokens.length; j++)
                {
                    if(curr_tokens[j].equals(""))
                        continue;
                    int num_people = Integer.parseInt(curr_tokens[j]);
                    if(num_people > 0)
                    {
                        ebolaSim.total_pop += num_people;

                        if(num_people > max_pop_density)
                            max_pop_density = num_people;
                        int scaled_num_people = scale(num_people, Parameters.SCALE);//Scale the number of agents to reduce size of simulation

                        ebolaSim.total_scaled_pop += scaled_num_people;

                        if(num_people > Parameters.MIN_POP_URBAN || nearbyUrban(prev_tokens, curr_tokens, next_tokens, i, j))//determine if location is urban
                        {
                            ebolaSim.urbanAreasGrid.setObjectLocation(new Object(), j, i);
                            ebolaSim.urban_pop += num_people;
                        }


                        while(scaled_num_people > 0)
                        {
                            Household h = new Household();

                            int x_coord, y_coord;
                            //randomly pick a space within the sqare kilometer
                            do
                            {
                                y_coord = (i*Parameters.WORLD_TO_POP_SCALE) + (int)(ebolaSim.random.nextDouble() * Parameters.WORLD_TO_POP_SCALE);
                                x_coord = (j*Parameters.WORLD_TO_POP_SCALE) + (int)(ebolaSim.random.nextDouble() * Parameters.WORLD_TO_POP_SCALE);

                            } while (ebolaSim.householdGrid.getObjectsAtLocation(x_coord, y_coord) != null);

                            h.x = x_coord;
                            h.y = y_coord;
                            ebolaSim.householdGrid.setObjectLocation(h, new Int2D(x_coord, y_coord));
                            //System.out.println("House location: " + x_coord + ", " + y_coord);
                            int household_size  = pickHouseholdSize();//use log distribution to pick correct household size

                            //add members to the household
                            for(int m = 0; m < household_size; m++)
                            {
                                if(num_people == 0)
                                    break;
                                scaled_num_people--;
                                Resident r = new Resident();
                                ebolaSim.schedule.scheduleRepeating(r);
                                r.household = h;
                                double ran_y = ebolaSim.random.nextDouble();//Add x jitter
                                double ran_x = ebolaSim.random.nextDouble();//Add y jitter
                                r.x = x_coord + ran_x;
                                r.y = y_coord + ran_y;
                                r.setPop_density(scaled_num_people);
                                ebolaSim.world.setObjectLocation(r, new Double2D(x_coord + ran_x, y_coord + ran_y));
                                h.addMember(r);//add the member to the houshold
                            }
                        }
                    }
                }
            }

            System.out.println("total scaled pop = " + ebolaSim.total_scaled_pop);
            System.out.println("total pop = " + ebolaSim.total_pop);
            System.out.println("expected scaled pop = " + ebolaSim.total_pop*1.0*Parameters.SCALE);
            System.out.println("max_pop_density = " + max_pop_density);
            System.out.println("urban_pop = " + ebolaSim.urban_pop);
            System.out.println("rural_pop = " + (ebolaSim.total_pop - ebolaSim.urban_pop));
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

    private static boolean nearbyUrban(String[] prev_tokens, String[] curr_tokens, String[] next_tokens, int i, int j)
    {
        int sum = 0;
        int count = 0;
        if(i < ebolaSim.pop_height-1)//not the last row
        {
            if(Integer.parseInt(next_tokens[j]) > 0)
            {
                sum += Integer.parseInt(next_tokens[j]);//add cell underneath
                count++;
            }
            if(j > 0 && Integer.parseInt(next_tokens[j-1]) > 0)
            {
                sum += Integer.parseInt(next_tokens[j-1]);//add the cell diagnol bottom left
                count++;
            }
            if(j < ebolaSim.pop_width - 1 && Integer.parseInt(next_tokens[j+1]) > 0)
            {
                sum += Integer.parseInt(next_tokens[j+1]);//add the cell diagnol bottom right
                count++;
            }
        }
        if(i > 0)//not the first row
        {
            if(Integer.parseInt(prev_tokens[j]) > 0)
            {
                sum += Integer.parseInt(prev_tokens[j]);//add cell above
                count++;
            }
            if(j > 0)
            {
                sum += Integer.parseInt(prev_tokens[j-1]);//add cell the diagnol top left
                count++;
            }
            if(j < ebolaSim.pop_width - 1)
            {
                if(Integer.parseInt(prev_tokens[j+1]) > 0)
                {
                    sum += Integer.parseInt(prev_tokens[j + 1]);//add cell diagnol top right
                    count++;
                }
            }
        }
        if(j < ebolaSim.pop_width - 1)//not last column
        {
            if(Integer.parseInt(curr_tokens[j+1]) > 0)
            {
                count++;
                sum += Integer.parseInt(curr_tokens[j+1]);//add cell to the right
            }
        }
        if(j > 0)//not first column
        {
            if(Integer.parseInt(curr_tokens[j-1]) > 0)
            {
                sum += Integer.parseInt(curr_tokens[j - 1]);//add cell to the left
                count++;
            }
        }
        return sum > (count * Parameters.MIN_POP_SURROUNDING);
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
        return (int)Stats.normalToLognormal(Stats.calcLognormalMu(average, stdv), Stats.calcLognormalSigma(average, stdv),
                ebolaSim.random.nextGaussian());
    }
}
