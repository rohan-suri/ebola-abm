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

    public static int WORLD_TO_POP_SCALE = 114;

    public static double POP_BLOCK_METERS = 926.1;//Height and width of one population block. (http://www.esri.com/news/arcuser/0400/wdside.html)
    public static double MIN_POP_URBAN = 575.45;//Minimum population density per 926 meters to be urban
    public static double MIN_POP_SURROUNDING = 287.73;//Minimum surrounding population density per 926 meters.  An urban district must be surrounded by
                                                      //by an total of this minimum density.  Source: http://cber.cba.ua.edu/asdc/urban_rural.html

}
