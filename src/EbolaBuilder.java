import com.sun.org.apache.bcel.internal.generic.ASTORE;
import com.vividsolutions.jts.geom.*;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.*;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.io.geo.*;
import sim.util.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.*;
import java.util.*;

import net.sf.csv4j.*;
import sim.util.geo.MasonGeometry;

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

    public static HashSet<Geometry> removeGeometry = new HashSet<Geometry>();
    public static HashSet<LineString> allLineStrings = new HashSet<LineString>();
    public static void initializeWorld(EbolaABM sim, String pop_file, String admin_file, String age_dist_file)
    {
        ebolaSim = sim;
        age_dist = new HashMap<Integer, ArrayList<Double>>();
        //TODO Hardcoded right now
        ebolaSim.world_height = 9990;
        ebolaSim.world_width = 9390;

        ebolaSim.allRoadNodes = new SparseGrid2D(ebolaSim.world_width, ebolaSim.world_height);
        ebolaSim.roadNetwork = new Network();
        ebolaSim.roadLinks = new GeomVectorField(ebolaSim.world_width, ebolaSim.world_height);
        System.out.println("(" + ebolaSim.world_width + ", " + ebolaSim.world_height + ")");
        GeomVectorField schools_vector = null;
        try
        {
            //read in roads shapefile
            Bag masked = new Bag();
            File file2 = new File(Parameters.ROADS_SHAPE_PATH);
            URL roadLinkUL = file2.toURI().toURL();
            ShapeFileImporter.read(roadLinkUL, ebolaSim.roadLinks, masked);

            //read in schools shapefile
            schools_vector = new GeomVectorField();
            Bag schools_masked = new Bag();
            File schools_file = new File(Parameters.SCHOOLS_PATH);
            URL shapeURI = schools_file.toURI().toURL();
            ShapeFileImporter.read(shapeURI, schools_vector, schools_masked);

            System.out.println("Done getting information, now analyzing.");

            //needed to assure same envelope
            System.out.println("about to read int Ascii grid");
            long t = System.currentTimeMillis();
            GeomGridField gridField = new GeomGridField();//just to align mbr
            InputStream inputStream = new FileInputStream(Parameters.POP_PATH);
            ArcInfoASCGridImporter.read(inputStream, GeomGridField.GridDataType.INTEGER, gridField);

            //align mbr for all vector files read
            System.out.println("Algining");

            Envelope globalMBR = ebolaSim.roadLinks.getMBR();

            globalMBR.expandToInclude(gridField.getMBR());
            globalMBR.expandToInclude(schools_vector.getMBR());

            ebolaSim.roadLinks.setMBR(globalMBR);
            gridField.setMBR(globalMBR);
            schools_vector.setMBR(globalMBR);

            //Read in the road cost file
            readInRoadCost();

            System.out.println("Time " + ((System.currentTimeMillis()-t)/1000/60) + " minutes");

        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        //construct network of roads from roadLinks
        extractFromRoadLinks(ebolaSim.roadLinks, ebolaSim);
        System.out.println("Un trimmed network size = " + ebolaSim.allRoadNodes.size());

        //add schools from vectorfield
        readInSchools(schools_vector);

        //assignNearest Nodes to all facilities except households
        assignNearestNode(ebolaSim.schoolGrid, ebolaSim.schoolNodes);

        //read in csv that gives the distribution of ages for the three countries from landscan data
        setUpAgeDist(age_dist_file);

        //Create the population - note, this call assumes all structures have been read in
        addHousesAndResidents(pop_file, admin_file);

        // set up the locations and nearest node capability
        long time = System.currentTimeMillis();
        System.out.println("Assigning nearestNodes...");
        //assignNearestNode(ebolaSim.householdGrid, ebolaSim.householdNodes);
        System.out.println("time = " + ((System.currentTimeMillis() - time) / 1000 / 60) + " minutes");
    }

    static void readInRoadCost()
    {
        try
        {
            ebolaSim.road_cost = new DoubleGrid2D(ebolaSim.world_width, ebolaSim.world_height);

            FileInputStream fileInputStream = new FileInputStream(new File(Parameters.ROADS_COST_PATH));
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            for(int i = 0; i < ebolaSim.world_width; i++)
                for(int j = 0; j < ebolaSim.world_height; j++)
                    ebolaSim.road_cost.set(i, j, dataInputStream.readDouble());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private static void editAndWriteRaster()
    {
        //TEMP
//            roads_grid = new GeomGridField();
//            InputStream is = new FileInputStream("data/all_roads_trim_raster.asc");
//            ArcInfoASCGridImporter.read(is, GeomGridField.GridDataType.INTEGER, roads_grid);
        //TEMP AF
//        IntGrid2D grid = (IntGrid2D)roads_grid.getGrid();
//        for(int i = 0; i < grid.getWidth(); i++)
//            for(int j = 0; j < grid.getHeight(); j++)
//                if(grid.get(i,j) != -9999)
//                    grid.set(i,j,0);
//        roads_grid.setGrid(grid);
//        //now write it
//        try {
//            BufferedWriter writer = new BufferedWriter( new FileWriter("roads_trim_zero.asc") );
//            ArcInfoASCGridExporter.write(roads_grid, writer);
//            writer.close();
//        } catch (IOException ex) {
//        /* handle exception */
//            ex.printStackTrace();
//        }
    }

    private static void remove()
    {
        //            int sum = 0;
//            int max = 0;
//            int[] frequency = new int[100];
//            for(int i = 0; i < allNetworks.size(); i++)
//            {
//                HashSet<LineString> set = allNetworks.get(i);
//                int total_nodes = 0;
//                for(LineString lineString: set)
//                    total_nodes += lineString.getNumPoints();
//
//                sum += total_nodes;
//                if(total_nodes > max)
//                    max = total_nodes;
//
//                if(total_nodes < 500)
//                    frequency[total_nodes/5]++;
//                else
//                    frequency[99]++;
//
//                if(total_nodes > 100)
//                {
//                    for(LineString lineString: set)
//                    {
//                        //removeGeometry.add((Geometry)lineString);
//                        MasonGeometry mg = new MasonGeometry();
//                        mg.geometry = lineString;
//                        ebolaSim.roadLinks.addGeometry(mg);
//                    }
//                }
//                else
//                {
//                    for(LineString lineString: set)
//                    {
//                        removeGeometry.add((Geometry)lineString);
//                    }
//                }
//
//            }
//            System.out.println("Max allRoadNodes = " + max);
//            //System.out.println("Average allRoadNodes = " + sum*1.0/allNetworks.size());
//
//            String[] s = new String[frequency.length];
//            for(int i = 0; i < frequency.length; i++)
//            {
//                s[i] = (i+1)*5 + "";
//            }
//
//            for(int i = 0; i < frequency.length; i++)
//            {
//                System.out.print(frequency[i] + " \t\t\t");
//                ebolaSim.roadNetworkDistribution.addValue(frequency[i],"Number of allRoadNodes",s[i]);
//            }
//            System.out.println();
//            for(int i = 0; i < frequency.length; i++)
//            {
//                System.out.print((i+1)*5 + " \t\t\t\t");
//            }
//            System.out.println("\nExporting...");
//            GeoToolsImporter.removeAndExport(removeGeometry);
    }

    /**
     * Reads in the schools and add them to the grid
     * @param school_file path to shapefile of schools
     */
    static void readInSchools(GeomVectorField schools_vector)
    {
        ebolaSim.schoolGrid = new SparseGrid2D(ebolaSim.world_width, ebolaSim.world_height);
        Bag school_geom = schools_vector.getGeometries();

        Envelope e = schools_vector.getMBR();
        double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
        int xcols = ebolaSim.world_width - 1, ycols = ebolaSim.world_height - 1;
        //System.out.println("Number of schools = " + school_geom.size());
        for(Object o: school_geom)
        {
            MasonGeometry school = (MasonGeometry)o;
            Point point = schools_vector.getGeometryLocation(school);
            double x = point.getX(), y = point.getY();
            int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)), yint = (int) (ycols - Math.floor(ycols * (y - ymin) / (ymax - ymin))); // REMEMBER TO FLIP THE Y VALUE
            School s = new School(new Int2D(xint, yint));
            ebolaSim.schools.add(s);
            //System.out.println("(" + xint + ", " + yint + ")");
            ebolaSim.schoolGrid.setObjectLocation(s, xint, yint);
        }
    }

    /**
     * Function will assign each structure in the SparseGrid a nearest node on the road network found in allRoadNodes at sim state.
     * @param grid a sparsegrid that contains strctures.
     */
    static void assignNearestNode(SparseGrid2D grid, Map<Node, Structure> nodeStructureMap)
    {
        double max_distance = 0;
        int count = 0;
        double sum = 0;
        Bag objects  = grid.getAllObjects();
        for(Object o: objects)
        {
            Structure structure = (Structure)o;
            Node node = getNearestNode(structure.getLocation().getX(), structure.getLocation().getY());
            if(node != null)
            {
                structure.setNearestNode(node);
                nodeStructureMap.put(node, structure);

                //create a node on the road network that connects this structure to the road network
                Node newNode = new Node(structure.location);
                Edge e = new Edge(newNode, node, (int)newNode.location.distance(node.location));
                newNode.links.add(e);
                node.links.add(e);
                structure.setNearestNode(newNode);
                nodeStructureMap.remove(node);
                nodeStructureMap.put(newNode, structure);

                double distance = structure.getLocation().distance(node.location);
                distance *= (Parameters.POP_BLOCK_METERS/Parameters.WORLD_TO_POP_SCALE)/1000.0;
                if(distance > max_distance)
                    max_distance = distance;
                sum += distance;
                count++;
            }
        }
        for(int i = 0; i < frequency.length; i++)
        {
            System.out.print(frequency[i] + "\t\t");
        }
        System.out.println("\nAverage distance = " + sum/count + " km");
        System.out.println("Max distance household to node = " + max_distance + " kilometers");
    }
    private static int[] frequency = new int[300];
    /**
     *
     * @param x source x coordinate
     * @param y source y coordinate
     * @return Road node nearest to the x, y coordinates
     */
    static Node getNearestNode(int x, int y)
    {
        int cX = x;
        int cY = y;

        while(ebolaSim.road_cost.get(cX, cY) != 0)
        {
            DoubleBag val = new DoubleBag();
            IntBag xBag = new IntBag();
            IntBag yBag = new IntBag();

            ebolaSim.road_cost.getRadialNeighbors(cX, cY, 1, Grid2D.BOUNDED, true, val, xBag, yBag);
            double min = Double.MAX_VALUE;
            int index = 0;
            for (int i = 0; i < val.size(); i++)
                if (val.get(i) < min)
                {
                    min = val.get(i);
                    index = i;
                }
            cY = yBag.get(index);
            cX = xBag.get(index);
        }

        Bag nodes = ebolaSim.allRoadNodes.getObjectsAtLocation(cX, cY);
        Bag val = new Bag();
        IntBag xBag = new IntBag();
        IntBag yBag = new IntBag();

        if(nodes == null || nodes.isEmpty())
        {
            for(int i = 1; i < 300; i++)
            {
                ebolaSim.allRoadNodes.getRadialNeighbors(cX, cY, i, Grid2D.BOUNDED, true, val, xBag, yBag);
                if(val != null && !val.isEmpty())
                {
                    frequency[i]++;
                    //System.out.println("Radial neihghbor found!! at " + i);
                    return (Node)val.get(0);
                }
            }

            System.out.println("NO NODE NEARBY!!!!!!!!!!!!!");
            frequency[9]++;
            return new Node(new Int2D(cX, cY));
        }
        else
        {
            frequency[0]++;
            //System.out.println("On a NODE!!");
        }

        return (Node)nodes.get(0);

    }

    static void extractFromRoadLinks(GeomVectorField roadLinks, EbolaABM ebolaSim)
    {
        Bag geoms = roadLinks.getGeometries();
        Envelope e = roadLinks.getMBR();
        double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
        int xcols = ebolaSim.world_width - 1, ycols = ebolaSim.world_height - 1;
        int count = 0;

        //allNetworks = new LinkedList<HashSet<LineString>>();

        // extract each edge
        for (Object o : geoms)
        {
            MasonGeometry gm = (MasonGeometry) o;
            if (gm.getGeometry() instanceof LineString)
            {
                count++;
                readLineString((LineString) gm.getGeometry(), xcols, ycols, xmin, ymin, xmax, ymax, ebolaSim);

            } else if (gm.getGeometry() instanceof MultiLineString)
            {
                MultiLineString mls = (MultiLineString) gm.getGeometry();
                for (int i = 0; i < mls.getNumGeometries(); i++)
                {
                    count++;
                    readLineString((LineString) mls.getGeometryN(i), xcols, ycols, xmin, ymin, xmax, ymax, ebolaSim);
                }
            }
            if(count%10000 == 0)
                System.out.println("# of linestrings = " + count);

        }

    }

//    /**
//     * Converts an individual linestring into a series of links and nodes in the
//     * network
//     * int width, int height, Dadaab dadaab
//     * @param geometry
//     * @param xcols - number of columns in the field
//     * @param ycols - number of rows in the field
//     * @param xmin - minimum x value in shapefile
//     * @param ymin - minimum y value in shapefile
//     * @param xmax - maximum x value in shapefile
//     * @param ymax - maximum y value in shapefile
//     */
//    static void readLineString(LineString geometry, int xcols, int ycols, double xmin,
//                               double ymin, double xmax, double ymax, EbolaABM ebolaSim)
//    {
//
//        CoordinateSequence cs = geometry.getCoordinateSequence();
//
//        // iterate over each pair of coordinates and establish a link between
//        // them
//        Node oldNode = null; // used to keep track of the last node referenced
//        for (int i = 0; i < cs.size(); i++) {
//
//            // calculate the location of the node in question
//            double x = cs.getX(i), y = cs.getY(i);
//            int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)), yint = (int) (ycols - Math.floor(ycols * (y - ymin) / (ymax - ymin))); // REMEMBER TO FLIP THE Y VALUE
//
//            if (xint >= ebolaSim.world_width) {
//                continue;
//            } else if (yint >= ebolaSim.world_height) {
//                continue;
//            }
//
//            // find that node or establish it if it doesn't yet exist
//            Bag ns = ebolaSim.allRoadNodes.getObjectsAtLocation(xint, yint);
//            Node n;
//            if (ns == null) {
//                n = new Node(new Location(xint, yint));
//                ebolaSim.allRoadNodes.setObjectLocation(n, xint, yint);
//            } else {
//                n = (Node) ns.get(0);
//            }
//
//            if (oldNode == n) // don't link a node to itself
//            {
//                continue;
//            }
//
//            // attach the node to the previous node in the chain (or continue if
//            // this is the first node in the chain of links)
//
//            if (i == 0) { // can't connect previous link to anything
//                oldNode = n; // save this node for reference in the next link
//                continue;
//            }
//
//            int weight = (int) n.location.distanceTo(oldNode.location); // weight is just
//            // distance
//
//            // create the new link and save it
//            Edge e = new Edge(oldNode, n, weight);
//            ebolaSim.roadNetwork.addEdge(e);
//            oldNode.links.add(e);
//            n.links.add(e);
//
//            oldNode = n; // save this node for reference in the next link
//        }
//    }

    /**
     * Converts an individual linestring into a series of links and allRoadNodes in the
     * network
     * int width, int height, Dadaab dadaab
     * @param geometry
     * @param xcols - number of columns in the field
     * @param ycols - number of rows in the field
     * @param xmin - minimum x value in shapefile
     * @param ymin - minimum y value in shapefile
     * @param xmax - maximum x value in shapefile
     * @param ymax - maximum y value in shapefile
     */
    static void readLineString(LineString geometry, int xcols, int ycols, double xmin,
                               double ymin, double xmax, double ymax, EbolaABM ebolaSim) {

        CoordinateSequence cs = geometry.getCoordinateSequence();
        // iterate over each pair of coordinates and establish a link between
        // them

        if(!allLineStrings.add(geometry)) //Uncomment for linestring trimming
            return;

        //linestring trimming: HashSet<LineString> curSet = new HashSet<LineString>();
        //curSet.add(geometry);
        //allNetworks.addFirst(curSet);
//        ListIterator<HashSet<LineString>> listIterator = allNetworks.listIterator();
//        listIterator.next();
//        int removeIndex = 0;
        Node oldNode = null; // used to keep track of the last node referenced
        Node oldNodeTrimmed = null; //used to keep track of last trimmed node referenced
        int trimmed_distance = 0;
        for (int i = 0; i < cs.size(); i++)
        {
            // calculate the location of the node in question
            double x = cs.getX(i), y = cs.getY(i);
            int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)), yint = (int) (ycols - Math.floor(ycols * (y - ymin) / (ymax - ymin))); // REMEMBER TO FLIP THE Y VALUE

            if (xint >= ebolaSim.world_width)
                continue;
            else if (yint >= ebolaSim.world_height)
                continue;

            // find that node or establish it if it doesn't yet exist
            Bag ns = ebolaSim.allRoadNodes.getObjectsAtLocation(xint, yint);
            Node n;
            if (ns == null)
            {
                n = new Node(new Int2D(xint, yint));
                n.lineStrings.add(geometry);
                n.index = i;
                ebolaSim.allRoadNodes.setObjectLocation(n, xint, yint);
            }
            else //this means that we are connected to another linestring or this linestring
            {
                n = (Node) ns.get(0);
                //USE FOR NETWORK COLLAPSE
//                LineString searchFor = n.lineString;
//                ListIterator<HashSet<LineString>> nextIterator = allNetworks.listIterator();
//                //search for the other linestring
//                int temp = -1;
//                while(nextIterator.hasNext())
//                {
//                    HashSet<LineString> next = nextIterator.next();
//                    temp++;
//                    if(next.contains(searchFor))
//                    {
//                        if(next != curSet)
//                        {
//                            //add all from the previous hashset to this one
//                            next.addAll(curSet);
//                            curSet = next;
//
//                            //remove the earlier position
//                            //listIterator.remove();
//                            if(removeIndex != 0) {
//                                int john = 1;
//                                john++;
//                            }
//                            allNetworks.remove(removeIndex);
//                            if(removeIndex < temp)
//                                temp--;
//                            removeIndex = temp;
//                            //now reset the position of the iterator and change locations
//                            //removeIndex = nextIterator.nextIndex();
//
//                            if(removeIndex < 0 || !allNetworks.get(removeIndex).contains(geometry))
//                                System.out.println("ERROR ERROR ERROR ERROR!!!!!!!!!!!!!!!");
//                        }
//                        break;
//                    }
//                }
            }

            // attach the node to the previous node in the chain (or continue if
            // this is the first node in the chain of links)

            if (i == 0) { // can't connect previous link to anything
                oldNode = n; // save this node for reference in the next link
                continue;
            }

            int weight = (int) n.location.distance(oldNode.location); // weight is just
            // distance
            //add it to the thinned network if it is the first or last in the cs.

            if (oldNode == n) // don't link a node to itself
            {
                continue;
            }

            // create the new link and save it
            Edge e = new Edge(oldNode, n, weight);
            ebolaSim.roadNetwork.addEdge(e);

            oldNode.links.add(e);
            n.links.add(e);
            n.weightOnLineString = trimmed_distance;
            oldNode = n; // save this node for reference in the next link
        }

        //if we haven't found any links the network should be null

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
            ebolaSim.world_width = width*Parameters.WORLD_TO_POP_SCALE;

            pop_line = pop_reader.readLine();
            curr_tokens = pop_line.split("\\s+");
            int height = Integer.parseInt(curr_tokens[1]);
            ebolaSim.pop_height = height;
            ebolaSim.world_height = height*Parameters.WORLD_TO_POP_SCALE;

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
            for(int i = 0; i < height; i++)
            {
                if(i != 0)//store three lines at a time so you can check surrounding cells
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
                //iterate over the row
                for(int j = 0; j < curr_tokens.length; j++)
                {
                    //number of people within this row
                    int num_people = Integer.parseInt(curr_tokens[j]);
                    if(num_people > 0)
                    {
                        ebolaSim.total_pop += num_people;

                        int scaled_num_people = scale(num_people, Parameters.SCALE);//Scale the number of agents to reduce size of simulation

                        ebolaSim.total_scaled_pop += scaled_num_people;

                        //determine current country
                        int country = determineCountry(Integer.parseInt(admin_tokens[j]));
                        //county id, counties/provinces within each country
                        int county_id = Integer.parseInt(admin_tokens[j]);

                        //add up total pop stats for later
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

                        //iterate over each house
                        while(scaled_num_people > 0)
                        {
                            int x_coord, y_coord;
                            //randomly pick a space within the square kilometer
                            do
                            {
                                y_coord = (i*Parameters.WORLD_TO_POP_SCALE) + (int)(ebolaSim.random.nextDouble() * Parameters.WORLD_TO_POP_SCALE);
                                x_coord = (j*Parameters.WORLD_TO_POP_SCALE) + (int)(ebolaSim.random.nextDouble() * Parameters.WORLD_TO_POP_SCALE);

                            } while (false);//ebolaSim.householdGrid.getObjectsAtLocation(x_coord, y_coord) != null);
                            Household h = new Household(new Int2D(x_coord, y_coord));
                            h.setCountry(country);
                            h.setNearestNode(getNearestNode(h.getLocation().getX(), h.getLocation().getY()));//give it a nearest node

                            //addNearestNode to the network
                            Node newNode = new Node(h.location);
                            Edge e = new Edge(newNode, h.getNearestNode(), (int)newNode.location.distance(h.getNearestNode().location));
                            newNode.links.add(e);
                            h.getNearestNode().links.add(e);
                            h.setNearestNode(newNode);

                            ebolaSim.householdGrid.setObjectLocation(h, new Int2D(x_coord, y_coord));

                            int household_size  = pickHouseholdSize(country);//use log distribution to pick correct household size

                            //get nearest school
                            School nearest_school = (School)getNearestStructureByRoute(h.getNearestNode(), ebolaSim.schoolNodes);//getNearestSchool(h.getLocation().getX(), h.getLocation().getY());

                            //add members to the household
                            for(int m = 0; m < household_size; m++)
                            {
                                if(num_people == 0)
                                    break;
                                scaled_num_people--;
                                Resident r = new Resident(new Int2D(x_coord, y_coord));
                                ebolaSim.schedule.scheduleRepeating(r);
                                r.setHousehold(h);
                                r.setPop_density(scaled_num_people);
                                r.setAge(pick_age(age_dist, county_id));
                                if(nearest_school != null)
                                    r.setNearestSchool(nearest_school);
                                else
                                    System.out.println("SCHOOL IS NULL!!");
                                if(r.getAge() >= 5 && r.getAge() <= 14 && nearest_school != null)
                                {
                                    nearest_school.addMember(r);
                                }
                                ebolaSim.world.setObjectLocation(r, new Double2D(x_coord, y_coord));
                                h.addMember(r);//add the member to the houshold
                            }
                        }
                    }
                }
            }

            System.out.println("total scaled pop = " + ebolaSim.total_scaled_pop);
            System.out.println("total pop = " + ebolaSim.total_pop);
            System.out.println("expected scaled pop = " + ebolaSim.total_pop*1.0*Parameters.SCALE);
            System.out.println("total_urban_pop = " + ebolaSim.total_urban_pop);
            System.out.println("total_rural_pop = " + (ebolaSim.total_pop - ebolaSim.total_urban_pop));
            System.out.println("sierra_leone urban percentage = " + ebolaSim.sl_urban_pop*1.0/ebolaSim.total_sl_pop);
            System.out.println("liberia urban percentage = " + ebolaSim.lib_urban_pop*1.0/ebolaSim.total_lib_pop);
            System.out.println("guinea urban percentage = " + ebolaSim.guinea_urban_pop*1.0/ebolaSim.total_guinea_pop);
            System.out.println("no school count = " + ebolaSim.no_school_count);
            System.out.println("average distance = " + ebolaSim.distance_sum/ebolaSim.distance_count);
            System.out.println("max distance = " + ebolaSim.max_distance);
            int sum = 0;
            int count = 0;
            int max_size = 0;
            for(Object o: ebolaSim.schools)
            {
                School school = (School)o;
                sum += school.getMembers().size();
                count++;
                if(school.getMembers().size() > max_size)
                    max_size = school.getMembers().size();
            }
            System.out.println("average school population = " + sum*1.0/count);
            System.out.println("max school pop = " + max_size);
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

    private static Structure getNearestStructureByRoute(Node start, Map<Node, Structure> endNodes)
    {
        return endNodes.get(AStar.getNearestNode(start, endNodes));
    }

    private static School getNearestSchool(int x, int y)
    {
        //find nearest school
        Bag schools = new Bag();

        for(int i = 3; i <= 54; i += 3)//increment the radius to lookup i in kilometers
        {
            int radius = (int)Math.round(i*1000/Parameters.POP_BLOCK_METERS*Parameters.WORLD_TO_POP_SCALE);//convert to grid units
            ebolaSim.schoolGrid.getRadialNeighborsAndLocations(x, y, radius, SparseGrid2D.BOUNDED, false, schools, null, null);
            if(schools.size() != 0)
                break;
        }

        School nearest_school = null;
        double min_distance = Double.MAX_VALUE;
        for(Object o: schools)
        {
            School school = (School)o;
            double distance = school.getLocation().distance(new Double2D(x, y));
            if(distance < min_distance)
            {
                min_distance = distance;
                nearest_school = school;
            }
        }
        min_distance *= (Parameters.POP_BLOCK_METERS/Parameters.WORLD_TO_POP_SCALE)/1000.0;//convert to kilometers
        if(nearest_school == null)
            ebolaSim.no_school_count++;
        else
        {
            ebolaSim.distance_count++;
            ebolaSim.distance_sum += min_distance;
            if(min_distance > ebolaSim.max_distance)
                ebolaSim.max_distance = min_distance;
        }
        return nearest_school;
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

    /**
     *
     * @param prev_tokens previous row
     * @param curr_tokens current row
     * @param next_tokens next row
     * @param i column index
     * @param j row index
     * @return whether the nearby cells have a total population density greater than 500 people per square mile
     */
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
     * Within each fivebucket range it picks an age randomly
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
        int age = i*5 + ebolaSim.random.nextInt(5);
        //System.out.println(age + " years");
        return age;
    }

    public static class Node
    {
        public Int2D location;

        ArrayList<Edge> links;
        double weightOnLineString;//measures the weight on the line string from 0
        public HashSet<LineString> lineStrings = new HashSet<LineString>();
        public int index;
        public Node(Int2D l)
        {
            location = l;
            links = new ArrayList<Edge>();
        }

        public ArrayList<Edge> getLinks() {
            return links;
        }
        @Override
        public String toString()
        {
            return "(" + location.getX() + ", " + location.getY() + ")";
        }
    }

}
