/**
 * Created by rohansuri on 7/7/15.
 */
public class Parameters
{
    public static double SCALE = 0.001; //percentage of total population that agents will be created.  Maximimum of 1

    public static String POP_PATH = "data/merged_pop.asc"; //Path to liberia population data

    public static double LIB_AVG_HOUSEHOLD_SIZE = 5.1; //Liberia's average household size (2008, http://lisgis.net/pg_img/Population%20size%20210512.pdf)

    public static double LIB_HOUSEHOLD_STDEV =  1.61;//TODO random, taken from TB model

    public static double WORLD_DISCRETIZTION = 0.1;//discretization or buckets for world granularity

    public static int WORLD_TO_POP_SCALE = 10;
}
