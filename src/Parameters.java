/**
 * Created by rohansuri on 7/7/15
 */
public class Parameters
{
    public static double SCALE = 0.01; //percentage of total population that agents will be created.  Maximimum of 1
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


    //Parameters for inactive vs labour force. Taken from: http://www.ilo.org/wcmsp5/groups/public/---dgreports/---stat/documents/presentation/wcms_156366.pdf
    //ages index 0 = ages 5-9,  1 = ages 10-14, 2 = ages 15-20, ... 75+
    //Urban Male
    public static double[] URBAN_MALE_LF_BY_AGE = {.054, .097, .144, .356, .574, .821, .826, .899, .878, .835, .760, .610, .733, .578, .363, .421, .576, .578};
    //Urban Female
    public static double[] URBAN_FEMALE_LF_BY_AGE = {.040, .078, .142, .390, .598, .701, .755, .798, .763, .736, .605, .517, 471, .429, .156, .384, .526, .533};
    //Rural Male
    public static double[] RURAL_MALE_LF_BY_AGE = {.220, .391, .434, .612, .750, .880, .889, .892, .944, .896, .829, .807, .843, .643, .484, .591, .749, .758};
    //Rural Female
    public static double[] RURAL_FEMALE_LF_BY_AGE = {.201, .307, .418, .570, .754, .770, .796, .800, .820, .747, .774, .664, .491, .512, .294, .553, .678, .695};

    //Parameters for reason of inactivity, either school or household work
    //  0      1       2       3       4
    //  5-14   15-24   25-34   35-54   65+
    //Urban Male
    public static double[] URBAN_MALE_INACTIVE_SCHOOL = {0.788, 0.785, 0.548, 0.17, 0.196, 0.077};
    //Urban Female
    public static double[] URBAN_FEMALE_INACTIVE_SCHOOL = {0.795, 0.67, 0.313, 0.232, 0.182, 0.171};
    //Rural Male
    public static double[] RURAL_MALE_INACTIVE_SCHOOL = {0.655, 0.699, 0.372, 0.302, 0.233, 0.124};
    //Rural Female
    public static double[] RURAL_FEMALE_INACTIVE_SCHOOL = {0.652, 0.544, 0.249, 0.223, 0.161, 0.108};

    //Parameters for unemployment of labour force
    //  0      1       2       3       4
    //  5-14   15-24   25-34   35-54   65+
    //Urban Male
    public static double[] URBAN_MALE_UNEMPLOYMENT = {0.068, 0.056, 0.038, 0.023, 0.029, 0.046};
    //Urban Female
    public static double[] URBAN_FEMALE_UNEMPLOYMENT = {0.146, 0.061, 0.04, 0.008, 0.002, 0.063};
    //Rural Male
    public static double[] RURAL_MALE_UNEMPLOYMENT = {0.021, 0.034, 0.021, 0.01, 0.023, 0.024};
    //Rural Female
    public static double[] RURAL_FEMALE_UNEMPLOYMENT = {0.032, 0.021, 0.021, 0.013, 0.006, 0.022};

    //Parameters for distribution of economic sector based on Urban/rural and male/female
    //Urban Male
    public static double[] URBAN_MALE_SECTORS = {0.242, 0.017, 0.128, 0.006, 0.003, 0.075, 0.161, 0.063, 0.023, 0.008, 0.015, 0, 0.008, 0.035, 0.011, 0.116, 0.049, 0.01, 0.025, 0.004, 0.002};
    //Urban Female
    public static double[] URBAN_FEMALE_SECTORS = {0.225, 0.006, 0.034, 0, 0, 0.022, 0.532, 0.004, 0.057, 0.002, 0.004, 0, 0, 0.005, 0.006, 0.056, 0.016, 0.007, 0.012, 0.012, 0};
    //Rural Male
    public static double[] RURAL_MALE_SECTORS = {0.647, 0.012, 0.149, 0, 0, 0.01, 0.079, 0.017, 0.013, 0, 0, 0, 0.003, 0.013, 0.004, 0.037, 0.006, 0, 0.009, 0.001, 0};
    //Rural Female
    public static double[] RURAL_FEMALE_SECTORS = {0.561, 0.01, 0.056, 0, 0, 0, 0.32, 0.003, 0.024, 0, 0.002, 0, 0, 0.005, 0, 0.007, 0.009, 0, 0.002, 0.003, 0};

    public static double convertToKilometers(double val)
    {
        return val * (Parameters.POP_BLOCK_METERS/Parameters.WORLD_TO_POP_SCALE)/1000.0;
    }
}
