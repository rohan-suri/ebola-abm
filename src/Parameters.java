/**
 * Created by rohansuri on 7/7/15
 */
public class Parameters
{
    public static double SCALE = 0.0001; //percentage of total population that agents will be created.  Maximimum of 1
    public static int WORLD_TO_POP_SCALE = 10; //scale up from the population data for each household
    public static double WORLD_DISCRETIZTION = 0.1;//discretization or buckets for world granularity
    public static double POP_BLOCK_METERS = 926.1;//Height and width of one population block. (http://www.esri.com/news/arcuser/0400/wdside.html)
    public static double WORLD_LENGTH = WORLD_TO_POP_SCALE * POP_BLOCK_METERS;//The size of one grid cell in meters

    //-------File paths-------//
    public static String POP_PATH = "data/merged_pop.asc"; //Path to liberia population data (LandScan 2013)
    public static String ADMIN_PATH = "data/merged_admin.asc";//Path to file that has administration and county boundaries for all three countries (LandScan 2013)
    public static String AGE_DIST_PATH = "data/All_Age_Distribution.csv";//Path to file that has age distribution for each of the counties and provinces (LandScan 2013)
    public static String ROADS_SHAPE_PATH = "data/roads_shapefile/all_roads_trim7.shp";//Path to vector data for all roads
    public static String ROADS_COST_PATH = "data/road_cost.dat";//Path to cost distance data for all allRoadNodes in the network
    public static String SCHOOLS_PATH = "data/schools_shapefile/all_schools.shp";//Path to shapefile that has location of all primary schools

    public static double MIN_POP_URBAN = 575.45;//Minimum population density per 926 meters or 1000 people per square mile to be urban. Source: http://cber.cba.ua.edu/asdc/urban_rural.html
    public static double MIN_POP_SURROUNDING = 287.73;//Minimum surrounding population density per 926 meters.  An urban district must be surrounded by
                                                      //by an total of this minimum density.  Source: http://cber.cba.ua.edu/asdc/urban_rural.html

    //Contains id ranges (inclusive) for each county in Sierra Leone, Guinea, and Liberia - used to identify country and country specific statistics
    public static int MIN_LIB_COUNTY_ID = 1508;
    public static int MAX_LIB_COUNTY_ID = 1522;
    public static int MIN_SL_COUNTY_ID = 2379;
    public static int MAX_SL_COUNTY_ID = 2382;
    public static int MIN_GUINEA_COUNTY_ID = 1086;
    public static int MAX_GUINEA_COUNTY_ID = 1090;

    //-------Liberia---------//
    public static int LIBERIA = 2;//ID for Liberia
    public static double LIB_AVG_HOUSEHOLD_SIZE = 4.97; //Liberia's average household size (2008, http://www.euromonitor.com/medialibrary/PDF/Book_WEF_2014.pdf)
    public static double LIB_HOUSEHOLD_STDEV =  1.61;//TODO random, taken from TB model

    //-------Sierra Leone---------//
    public static int SL = 1;//ID for Sierra Leone
    public static double SL_AVG_HOUSEHOLD_SIZE = 5.56; //Sierra Leone's average household size (2008, http://www.euromonitor.com/medialibrary/PDF/Book_WEF_2014.pdf)


    //-------Guinea---------//
    public static int GUINEA = 0;//ID for Guinea
    public static double GUINEA_AVG_HOUSEHOLD_SIZE = 8.75; //Sierra Leone's average household size (2014, http://www.euromonitor.com/medialibrary/PDF/Book_WEF_2014.pdf)

    public static double convertToKilometers(double val)
    {
        return val * (Parameters.POP_BLOCK_METERS/Parameters.WORLD_TO_POP_SCALE)/1000.0;
    }
}
