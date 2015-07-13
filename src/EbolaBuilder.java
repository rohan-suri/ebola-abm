import sim.field.continuous.Continuous2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Double2D;
import sim.util.Int2D;

import java.io.*;
import java.text.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.csv4j.*;

/**
 * Created by rohansuri on 7/8/15.
 */
public class EbolaBuilder
{
    public static EbolaABM ebolaSim;

    /**
     * Contains keys that are the integer ids for each of the country's provinces/counties.
     * The arraylist is has distributions for the following age groups in the following order by increasing intervals of five years:
     * 0-4, 5-9, 10-14, 15-19, 20-24, 25-29, 30-34, 35-39, ... , 70-74, 75-80, 80+
     * A total of 17 groups, distribution is cumalitive so the last group should be ~ 1.0
     */
    private static HashMap<Integer, ArrayList<Double>> age_dist;

    public static void initializeWorld(EbolaABM sim, String pop_file, String admin_file, String age_dist_file)
    {
        ebolaSim = sim;
        age_dist = new HashMap<Integer, ArrayList<Double>>();
        setUpAgeDist(age_dist_file);
        addHousesAndResidents(pop_file, admin_file);
    }

    private static void setUpAgeDist(String age_dist_file)
    {
        try
        {
            // buffer reader for age distribution data
            CSVReader csvReader = new CSVReader(new FileReader(new File(age_dist_file)));
            csvReader.readLine();//skip the headers
            List<String> line = csvReader.readLine();
            while(!line.isEmpty())
            {
                //read in the county ids
                int county_id = NumberFormat.getNumberInstance(java.util.Locale.US).parse(line.get(0)).intValue();
                //relevant info is from 5 - 21
                ArrayList<Double> list = new ArrayList<Double>();
                //double sum = 0;
                for(int i = 5; i <= 21; i++)
                {
                    list.add(Double.parseDouble(line.get(i)));
                    //sum += Double.parseDouble(line.get(i));
                    //Use cumalitive probability
                    if(i != 5)
                        list.set(i-5, list.get(i-5) + list.get(i-5 - 1));
                    //System.out.print(list.get(i-5));
                }
                //System.out.println("sum = " + sum);
                //System.out.println();
                //now add it to the hashmap
                age_dist.put(county_id, list);
                line = csvReader.readLine();
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch(java.text.ParseException e)
        {
            e.printStackTrace();
        }
    }

    private static void addHousesAndResidents(String pop_file, String admin_file)
    {
        try
        {
            // buffer reader - read ascii file for population data
            BufferedReader pop_reader = new BufferedReader(new FileReader(pop_file));
            String pop_line;

            //buffer reader - read ascii file for admin data
            BufferedReader admin_reader = new BufferedReader(new FileReader(admin_file));
            String admin_line;
            String[] admin_tokens;

            // first read the dimensions - admin and pop should be same height and width
            pop_line = pop_reader.readLine(); // read line for width
            String[] curr_tokens = pop_line.split("\\s+");
            String[] prev_tokens = null;
            String[] next_tokens = null;
            int width = Integer.parseInt(curr_tokens[1]);
            ebolaSim.pop_width = width;

            pop_line = pop_reader.readLine();
            curr_tokens = pop_line.split("\\s+");
            int height = Integer.parseInt(curr_tokens[1]);
            ebolaSim.pop_height = height;

            //instantiate world to hold agents
            ebolaSim.world = new Continuous2D(Parameters.WORLD_DISCRETIZTION, width*Parameters.WORLD_TO_POP_SCALE, height*Parameters.WORLD_TO_POP_SCALE);
            //instantiate grid to hold houses
            ebolaSim.householdGrid = new SparseGrid2D((int)(width*Parameters.WORLD_TO_POP_SCALE), (int)(height*Parameters.WORLD_TO_POP_SCALE));
            ebolaSim.urbanAreasGrid = new SparseGrid2D((int)(width), (int)(height));

            for(int i = 0; i < 4; i++)//skip the next couple of lines (contain useless metadata)
                pop_reader.readLine();

            for(int i = 0; i < 6; i++)//skip useless metadata
                admin_reader.readLine();

            pop_line = pop_reader.readLine();
            pop_line = pop_line.substring(1);
            curr_tokens = pop_line.split("\\s+");
            System.out.println(curr_tokens.length);
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
                        pop_line = pop_reader.readLine();
                        pop_line = pop_line.substring(1);
                        next_tokens = pop_line.split("\\s+");
                    }

                    admin_line = admin_reader.readLine();
                    admin_tokens = admin_line.split("\\s+");
                    for(int j = 0; j < curr_tokens.length; j++)
                    {
                        int num_people = Integer.parseInt(curr_tokens[j]);
                        if(num_people > 0)
                        {
                            ebolaSim.total_pop += num_people;

                            if(num_people > max_pop_density)
                                max_pop_density = num_people;
                            int scaled_num_people = scale(num_people, Parameters.SCALE);//Scale the number of agents to reduce size of simulation

                            ebolaSim.total_scaled_pop += scaled_num_people;

                            //determine current country
                            int country = determineCountry(Integer.parseInt(admin_tokens[j]));
                            //county id
                            int county_id = Integer.parseInt(admin_tokens[j]);
                            if(country == Parameters.GUINEA)
                                ebolaSim.total_guinea_pop += num_people;
                            else if(country == Parameters.LIBERIA)
                                ebolaSim.total_lib_pop += num_people;
                            else
                                ebolaSim.total_sl_pop += num_people;

                            if(num_people > Parameters.MIN_POP_URBAN || nearbyUrban(prev_tokens, curr_tokens, next_tokens, i, j))//determine if location is urban
                            {
                                ebolaSim.urbanAreasGrid.setObjectLocation(new Object(), j, i);
                                ebolaSim.total_urban_pop += num_people;

                                if(country == Parameters.GUINEA)
                                    ebolaSim.guinea_urban_pop += num_people;
                                else if(country == Parameters.LIBERIA)
                                    ebolaSim.lib_urban_pop += num_people;
                                else
                                    ebolaSim.sl_urban_pop += num_people;
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
                                h.setCountry(country);
                                ebolaSim.householdGrid.setObjectLocation(h, new Int2D(x_coord, y_coord));
                                //System.out.println("House location: " + x_coord + ", " + y_coord);
                                int household_size  = pickHouseholdSize(country);//use log distribution to pick correct household size

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
                                    r.setAge(pick_age(age_dist, county_id));
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
            System.out.println("total_urban_pop = " + ebolaSim.total_urban_pop);
            System.out.println("total_rural_pop = " + (ebolaSim.total_pop - ebolaSim.total_urban_pop));
            System.out.println("sierra_leone urban percentage = " + ebolaSim.sl_urban_pop*1.0/ebolaSim.total_sl_pop);
            System.out.println("liberia urban percentage = " + ebolaSim.lib_urban_pop*1.0/ebolaSim.total_lib_pop);
            System.out.println("guinea urban percentage = " + ebolaSim.guinea_urban_pop*1.0/ebolaSim.total_guinea_pop);

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
     * @param county_id Id of county
     * @return country, 0 - Guinea, 1 - Sierra Leone, 2 - Liberia
     */
    private static int determineCountry(int county_id)
    {
        if(county_id >= Parameters.MIN_GUINEA_COUNTY_ID && county_id <= Parameters.MAX_GUINEA_COUNTY_ID)
            return Parameters.GUINEA;
        else if(county_id >= Parameters.MIN_SL_COUNTY_ID && county_id <= Parameters.MAX_SL_COUNTY_ID)
            return Parameters.SL;
        else //if(county_id >= Parameters.MIN_LIB_COUNTY_ID && county_id <= Parameters.MAX_LIB_COUNTY_ID)
            return Parameters.LIBERIA;

        //System.out.println("!!!!!!ERRORRORORORORO!!!!!!! " + county_id);
        //return 10000;
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

    public static int pickHouseholdSize(int country)
    {
        double average;
        if(country == Parameters.GUINEA)
            average = Parameters.GUINEA_AVG_HOUSEHOLD_SIZE;
        else if(country == Parameters.LIBERIA)
            average = Parameters.LIB_AVG_HOUSEHOLD_SIZE;
        else
            average = Parameters.SL_AVG_HOUSEHOLD_SIZE;

        double stdv = Parameters.LIB_HOUSEHOLD_STDEV;
        return (int)Stats.normalToLognormal(Stats.calcLognormalMu(average, stdv), Stats.calcLognormalSigma(average, stdv),
                ebolaSim.random.nextGaussian());
    }

    /**
     * Picks an age based on the the age_dist hashmap.  Pick the highest age within the range.
     * For example if the range is 0-4, it picks 4.
     */
    private static int pick_age(HashMap<Integer, ArrayList<Double>> age_dist, int county_id)
    {
        double rand = ebolaSim.random.nextDouble();
        if(county_id == -9999)
            county_id = Parameters.MIN_LIB_COUNTY_ID;
        ArrayList<Double> dist = age_dist.get(county_id);
        int i;
        for(i = 0; i < dist.size(); i++)
        {
            if(rand < dist.get(i))
                break;
        }
        int age = i*5 + 4;
        System.out.println(age + " years");
        return age;
    }
}
