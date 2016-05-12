import com.vividsolutions.jts.geom.Envelope;
import net.sf.csv4j.CSVReader;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Int2D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohansuri on 5/8/16.
 */
public class ETCmanager implements Steppable
{
	static EbolaABM ebolaABM = null;
	ArrayList<ETC>  allETCs  = new ArrayList<>();
	static LocalDate startDate = LocalDate.of(2014, 1, 1);

	public ETCmanager(EbolaABM _ebolaABM)
	{
		ebolaABM = _ebolaABM;

		readData(Parameters.ETC_GUINEA, "Guinea", 4, 9, 10, 13, 14);
		readData(Parameters.ETC_LIBERIA, "Liberia", 3, 7, 8, 11, 12);
		readData(Parameters.ETC_SIERRA_LEONE, "Sierra Leonne", 4, 9, 10, 13, 14);
	}

	private void readData(String file, String country, int posName, int posBeds, int posDate, int posLat, int posLong)
	{
		try
		{
			CSVReader csvReader = new CSVReader(new FileReader(new File(file)));
			csvReader.readLine();

			List<String> line;

			while ((line = csvReader.readLine()) != null && !line.isEmpty())
			{
				String           name;
				int              beds;
				double           lat, lng;
				String           startDate;
				LocalDate        oStartDate;

				try
				{
					name = line.get(posName);
					beds = Integer.parseInt(line.get(posBeds));

					// skip any ETC marked with zero beds
					if (beds == 0)
						continue;

					lat = Double.parseDouble(line.get(posLat));
					lng = Double.parseDouble(line.get(posLong));
					startDate = line.get(posDate);

					int pos = startDate.indexOf('T');

					startDate = startDate.substring(0, pos);

					oStartDate = LocalDate.parse(startDate);

					// oStartDate = DateTimeFormatter.ISO_DATE.parse(startDate);
				}
				catch (Exception e)
				{
					System.err.println("Skipped ETC due to invalid data in line: " + line);
					System.err.println(e.toString());
					continue;
				}

				// System.out.println("Creating ETC " + name + " with " + beds + " beds at (" + lat + "," + lng + ") opened " + oStartDate);

				ETC oETC = new ETC(country, name, beds, convertLatLogToInt2D(lat, lng), oStartDate);
				allETCs.add(oETC);
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
	public void step(SimState simState)
	{
		long stepsIntoRun = ebolaABM.schedule.getSteps();

		if (stepsIntoRun % 24 != 0) return;

		long daysIntoRun = stepsIntoRun / 24;

		// starting date is 2014-01-01
		// calculate if startingDate + currentDay > an ETC's start date
		// if so, "open" the ETC

		LocalDate todayDate = startDate.plusDays(daysIntoRun);

		System.out.println("Today's date: " + todayDate);

		// open ETCs by iterating through all not opened ETCs and setting open flag ifs opened date is >= today's date
		// for an ETC that's already opened free up beds that have a dead or recovered patient

		int countCleared = 0;

		for (ETC etc: allETCs)
		{
			if (!etc.isOpen())
			{
				LocalDate etcOpenDate = etc.getWhenOpened();

				if (etcOpenDate.isBefore(todayDate) || etcOpenDate.isEqual(todayDate))
				{
					etc.setOpened(true);
					System.out.println("Opened ETC: " + etc);
				}
				else
				{
					//System.out.println("Not opening ETC: " + etc.getName() + " since it's opening date: " + etcOpenDate + " is later than today: " + todayDate);
				}
			}
			else
				countCleared += etc.clearBeds();
		}

		if (countCleared > 0)
			System.out.println("Removed " + countCleared + " patients from ETCs");
	}

	public static Int2D convertLatLogToInt2D(double latitude, double longitude)
	{
		if (ebolaABM != null)
		{
			Envelope e = ebolaABM.roadLinks.getMBR();

			double xmin = e.getMinX();
			double ymin = e.getMinY();
			double xmax = e.getMaxX();
			double ymax = e.getMaxY();

			int xcols = ebolaABM.world_width - 1;
			int ycols = ebolaABM.world_height - 1;

			int xint = (int) Math.floor(xcols * (longitude - xmin) / (xmax - xmin));
			int yint = (int) (ycols - Math.floor(ycols * (latitude - ymin) / (ymax - ymin))); // REMEMBER TO FLIP THE Y VALUE

			return new Int2D(xint, yint);
		}
		else
			return new Int2D((int)longitude*1000, (int)latitude*1000);
	}

	public void listAll()
	{
		for (ETC etc: allETCs)
			System.out.println(etc.toString());
	}

	public static void main(String args[])
	{
		ETCmanager etcManager = new ETCmanager(null);

		etcManager.listAll();
	}

	/**
	 * @param resident
	 * @return nearest open and non filled ETC @null if none available
	 */
	public ETC getNearestETC(Resident resident)
	{
		double min_distance = Double.MAX_VALUE;
		int min_index = -1;
		for(int i = 0; i < allETCs.size(); i++)
		{
			if (allETCs.get(i).isOpen() && allETCs.get(i).hasSpace())
			{
				double dist = squareDistance(resident.getHousehold().getLocation(), allETCs.get(i).getLocation());
				if(dist < min_distance)
				{
					min_distance = dist;
					min_index = i;
				}
			}
		}
		if(min_index == -1)//no open ETCs with extra space
			return null;
		return allETCs.get(min_index);
	}

	private static double squareDistance(Int2D a, Int2D b)
	{
		return ((a.getX() - b.getX()) * (a.getX() - b.getX())) +((a.getY() - b.getY()) * (a.getY() - b.getY()));
	}
}
