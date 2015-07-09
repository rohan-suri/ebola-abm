import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
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
                super.scale = 1;
                super.draw(object, graphics, info);
            }
        });
        display.attach(residentPortrayal, "Residents");

        FieldPortrayal2D businessPortrayal = new SparseGridPortrayal2D();
        businessPortrayal.setField(((EbolaABM)state).householdGrid);
        businessPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(0, 128, 255), 1.0, false));
        display.attach(businessPortrayal, "Businesses");
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
