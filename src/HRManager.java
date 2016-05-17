import net.sf.csv4j.CSVReader;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rohan on 5/15/16.
 */
public class HRManager implements Steppable
{
    private EbolaABM ebolaABM;
    private ArrayList<Integer> hr_by_day;

    public HRManager(EbolaABM ebolaABM)
    {
        this.ebolaABM = ebolaABM;
        hr_by_day = new ArrayList<>();
        //read in file
        readData("data/who_people_by_day.csv");
    }

    private void readData(String file)
    {
        try
        {
            CSVReader csvReader = new CSVReader(new FileReader(new File(file)));
            csvReader.readLine();

            List<String> line;

            while ((line = csvReader.readLine()) != null && !line.isEmpty())
            {
                String startDate;
                LocalDate oStartDate;

                try
                {
                    int value = (int)Math.round(Double.parseDouble(line.get(0)));
                    hr_by_day.add(value);
                    // oStartDate = DateTimeFormatter.ISO_DATE.parse(startDate);
                }
                catch (Exception e)
                {
                    System.err.println("Skipped ETC due to invalid data in line: " + line);
                    System.err.println(e.toString());
                    continue;
                }

                // System.out.println("Creating ETC " + name + " with " + beds + " beds at (" + lat + "," + lng + ") opened " + oStartDate);

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
    }

    @Override
    public void step(SimState state) {

        long steps = state.schedule.getSteps();
        if (steps%24 != 0) return;
        int days = (int)(steps/24);
        int people_today = hr_by_day.get(days);
        int etc_hr = (int)Math.round(people_today*Parameters.PERCENT_ETC);
        int safe_burial_hr = (int)Math.round(people_today*Parameters.PERCENT_SAFE_BURIALS);
        int contact_tracing_hr = (int)Math.round(people_today*Parameters.PERCENT_CONTACT_TRACING);
        ebolaABM.myETCmanager.updateCapacity(etc_hr);
        ebolaABM.myBurialTeamManager.updateTeams(safe_burial_hr);
        ebolaABM.myContactTracerManager.updateTeams(contact_tracing_hr);
    }
}
