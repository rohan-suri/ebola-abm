import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

import javax.swing.*;
import java.awt.*;

/**
 * Created by rohansuri on 7/8/15.
 */
public class EbolaWithUI extends GUIState
{
    Display2D display; //displaying the model
    JFrame displayFrame; //frame containing all the displays


    public EbolaWithUI(EbolaABM sim)
    {
        super(sim);
    }

    @Override
    public void init(Controller c)
    {
        super.init(c);
        display = new Display2D(625, 625, this); //creates the display
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);

        JFreeChart roadNetworkChart;
        roadNetworkChart = ChartFactory.createBarChart("Road Network Distribution", "Number of Nodes",
                "Total Number", ((EbolaABM) this.state).roadNetworkDistribution, PlotOrientation.VERTICAL, false, false,
                false);
        roadNetworkChart.setBackgroundPaint(Color.WHITE);
        roadNetworkChart.getTitle().setPaint(Color.BLACK);

        CategoryPlot p4 = roadNetworkChart.getCategoryPlot();
        p4.setBackgroundPaint(Color.WHITE);
        p4.setRangeGridlinePaint(Color.blue);

        // set the range axis to display integers only...
        NumberAxis rangeAxis4 = (NumberAxis) p4.getRangeAxis();
        rangeAxis4.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        //rangeAxis4.setRange(0, ((EbolaABM) this.state).allRoadNodes.getAllObjects().size());

        ChartFrame frame4 = new ChartFrame("Road Network Distribution", roadNetworkChart);
        frame4.setVisible(false);
        frame4.setSize(700, 350);

        frame4.pack();
        c.registerFrame(frame4);
    }

    @Override
    public void start()
    {
        super.start();
        setupPortrayals();
    }

    public void setupPortrayals()
    {
        ContinuousPortrayal2D residentPortrayal = new ContinuousPortrayal2D();

        residentPortrayal.setField(((EbolaABM)this.state).world);
        residentPortrayal.setPortrayalForAll(new OvalPortrayal2D()
        {
            public void draw (Object object, Graphics2D graphics, DrawInfo2D info)
            {
                paint = new Color(20, 4, 255);
                super.scale = 1.0;
                super.draw(object, graphics, info);
            }
        });
        display.attach(residentPortrayal, "Residents");

        FieldPortrayal2D householdortrayal = new SparseGridPortrayal2D();
        householdortrayal.setField(((EbolaABM)state).householdGrid);
        householdortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(0, 128, 255), 1.0, true)
        {
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
            {
                Household house = (Household)object;

                if(house.getCountry() == Parameters.GUINEA)
                    paint = new Color(216, 10, 255);
                else if(house.getCountry() == Parameters.LIBERIA)
                    paint = new Color(52, 222, 29);
                else if(house.getCountry() == Parameters.SL)
                    paint = new Color(255, 248, 98);
                else
                    paint = new Color(8, 20, 255);
                super.draw(object, graphics, info);
            }
        });
        display.attach(householdortrayal, "Household");

//        FieldPortrayal2D urbanPortrayal = new SparseGridPortrayal2D();
//        urbanPortrayal.setField(((EbolaABM)state).urbanAreasGrid);
//        urbanPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(255, 21, 19), 1.0, false));
//        display.attach(urbanPortrayal, "Urban Area");

        FieldPortrayal2D roadPortrayal = new SparseGridPortrayal2D();
        roadPortrayal.setField(((EbolaABM)state).allRoadNodes);
        roadPortrayal.setPortrayalForAll(new OvalPortrayal2D(new Color(255, 64, 240), 1.0, true));
        display.attach(roadPortrayal, "Road Node Trimmed");

        FieldPortrayal2D schoolPortrayal = new SparseGridPortrayal2D();
        schoolPortrayal.setField(((EbolaABM)state).schoolGrid);
        schoolPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(255, 0, 22), 1.0, true));
        display.attach(schoolPortrayal, "Schools");

        //---------------------Adding the road portrayal------------------------------
        GeomVectorFieldPortrayal roadLinkPortrayal = new GeomVectorFieldPortrayal();
        roadLinkPortrayal.setField(((EbolaABM)state).roadLinks);
        roadLinkPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, 2.0, true));
        display.attach(roadLinkPortrayal, "Roads");

        NetworkPortrayal2D roadNetworkPortrayal = new NetworkPortrayal2D();
        roadNetworkPortrayal.setField(new SpatialNetwork2D(((EbolaABM)state).allRoadNodes, ((EbolaABM)state).roadNetwork));
        roadNetworkPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());
        display.attach(roadNetworkPortrayal, "Road Network");

    }

    @Override
    public void quit()
    {
        super.quit();

        if (displayFrame != null)
            displayFrame.dispose();
        displayFrame = null;
        display = null;

    }

    public static void main(String[] args)
    {
        EbolaWithUI ebUI = new EbolaWithUI(new EbolaABM(System.currentTimeMillis()));
        Console c = new Console(ebUI);
        c.setVisible(true);
    }
}
