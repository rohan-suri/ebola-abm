/**
 * Created by rohansuri on 7/7/15.
 */
public class Parameters
{
    public static double SCALE = 0.01; //percentage of total population that agents will be created.  Maximimum of 1

    public static String LIB_POP_PATH = "data/liberia/liberia_pop.asc"; //Path to liberia population data

    public static double LIB_TFR = 4.81; //Liberia's total fertility rate according to CIA World Factbook (2014)

    public static double LIB_TFR_STDEV =  1.61;//RANDOM, taken from TB model

    public static double WORLD_DISCRETIZTION = 0.1;//discretization or buckets for world granularity

    public static int WORLD_TO_POP_SCALE = 10;
}
