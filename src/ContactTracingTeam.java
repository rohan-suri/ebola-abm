import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by rohansuri on 5/10/16.
 */
public class ContactTracingTeam implements Steppable
{
	private ArrayList<ArrayList<Resident>> followUpLists;

	private static final int DAILY_FOLLOW_UPS     = 10;
	private static final int DAYS_BETWEEN_CHECKS  =  2;

	private static final int DAYS_TO_IDENTIFY     =  4;
	private static final int FOLLOW_UP_DAYS       = 21;

	private static final int FOLLOW_UP_CAPACITY   = DAILY_FOLLOW_UPS * DAYS_BETWEEN_CHECKS;

	public  enum State {FREE, IDENTIFYING, FOLLOWING_UP};

	private State                state;
	private int                  daysIdentifying   = 0;
	private int                  totalFollowUpDays = 0;
	private Resident             whoseContactsBeingIdentified;
	private ContactTracerManager myManager;
	private String               teamName;

	public ContactTracingTeam(ContactTracerManager _myManager, String _teamName)
	{
		myManager = _myManager;
		teamName  = _teamName;

		followUpLists = new ArrayList<ArrayList<Resident>>(DAYS_BETWEEN_CHECKS);

		for (int i=0; i < DAYS_BETWEEN_CHECKS; i++)
			followUpLists.add(new ArrayList<Resident>());

		state = State.FREE;
	}

	public void startIdentifying(Resident resident)
	{
		if (state != State.FREE) throw new IllegalStateException("Contact Tracing team cannot start identifying since it is not free");

		state = State.IDENTIFYING;
		whoseContactsBeingIdentified = resident;
	}

	public void startTracing()
	{
		if (state == State.IDENTIFYING) throw new IllegalStateException("Contact Tracing team cannot start tracing since it is identifying contacts");

		state = State.FOLLOWING_UP;
	}

	@Override
	public void step(SimState simState)
	{
		long stepsIntoRun = ((EbolaABM)simState).schedule.getSteps();

		if (stepsIntoRun % 24 != 0) return;

		if (state == State.IDENTIFYING)
		{
			if (daysIdentifying == DAYS_TO_IDENTIFY)
			{
				// we are done identifying, tell manager to add discovered contacts of this resident
				myManager.addContactsForFollowUp(whoseContactsBeingIdentified);
				state = State.FREE;
				daysIdentifying = 0;
			}
			else
				daysIdentifying++;

			return;
		}

		if (state == State.FOLLOWING_UP)
		{
			int todaysListIndex = totalFollowUpDays % DAYS_BETWEEN_CHECKS;
			ArrayList<Resident> todaysList = followUpLists.get(todaysListIndex);

			for (Resident resident: todaysList)
			{
				if (resident.getFollowedUpDays() == FOLLOW_UP_DAYS)
				{
					// no need to check this resident further, remove this resident from our day's list
					todaysList.remove(resident);
				}
				else // check resident's health status
				if (resident.getHealthStatus() == Constants.INFECTIOUS)
				{
					// resident is sick, stop following up and immediately send him to nearest ETC
					// TODO: Add code to send resident to nearest ETC

					todaysList.remove(resident);
				}
				else // increment resident's follow up day count
					resident.incrementFollowUpDays();
			}

			totalFollowUpDays++;
		}

		System.out.println(this);
	}

	public int addFollowUps(ArrayList<Resident> masterFollowUpList)
	{
		// moves as many members from masterFollowUpList to this team as capacity will allow
		// adds a resident to the daily list with the least size

		int residentsAdded = 0;

		ListIterator<Resident> iterator = masterFollowUpList.listIterator();

		while (iterator.hasNext())
		{
			Resident resident = iterator.next();

			ArrayList<Resident> smallestDailyList = getSmallestDailyList();

			if (smallestDailyList != null)
			{
				smallestDailyList.add(resident);
				iterator.remove();
				residentsAdded++;
			}
		}

		return residentsAdded;
	}

	public ArrayList<Resident> getSmallestDailyList()
	{
		int                 minSize = DAILY_FOLLOW_UPS+1;
		ArrayList<Resident> minList = null;

		for (ArrayList<Resident> dailyList : followUpLists)
		{
			int size = dailyList.size();

			if (size == DAILY_FOLLOW_UPS) continue; // ignore a full list

			if (size < minSize)
			{
				minSize = size;
				minList = dailyList;
			}
		}

		return minList; // possible to return null if all lists are full
	}

	public int getWorkLoad()
	{
		// gets total residents in all follow up lists

		int totalLoad = -1;

		for (ArrayList<Resident> followUpList: followUpLists)
			totalLoad += followUpList.size();

		return totalLoad;
	}

	State getState()
	{
		return state;
	}

	public static int getFollowUpCapacity()
	{
		return FOLLOW_UP_CAPACITY;
	}

	public String toString()
	{
		String stateString = "Free";

		if (state == State.IDENTIFYING) stateString = "Identifying for " + daysIdentifying + " days";
		if (state == State.FOLLOWING_UP) stateString = "Following up " + getWorkLoad() + " contacts";

		StringBuffer sb = new StringBuffer(teamName);

		if (state == State.FREE)
			sb.append(" Free");
		else if (state == State.IDENTIFYING)
			sb.append(" Identifying for ").append(daysIdentifying).append(" days");
		else if (state == State.FOLLOWING_UP)
			sb.append(" Following up ").append(getWorkLoad()).append(" contacts");

		return sb.toString();
	}
}
