import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.*;

/**
 * Created by rohansuri on 5/10/16.
 */
public class ContactTracerManager implements Steppable
{
	//	Manages teams of contact tracers

	private EbolaABM                      ebolaABM;
	private int                           numberOfTeams;

	public  ArrayList<ContactTracingTeam> allTeams;
	private ArrayList<Resident>           masterFollowUpList;
	private int                           MAX_RESIDENTS_FOLLOWING = numberOfTeams * ContactTracingTeam.getFollowUpCapacity();

	private static final boolean          DEBUG = false;

	public ContactTracerManager(int _numberOfTeams, EbolaABM _ebolaABM)
	{
		ebolaABM      = _ebolaABM;
		numberOfTeams = _numberOfTeams;
		allTeams      = new ArrayList<ContactTracingTeam>(numberOfTeams);

		for (int i=0; i < numberOfTeams; i++)
			allTeams.add(new ContactTracingTeam(this, "Team " + i));

		masterFollowUpList = new ArrayList<Resident>();
	}

	public boolean applyForTracing(Resident resident)
	{
		// find a team to start identifying contacts

		for (ContactTracingTeam team: allTeams)
		{
			if (team.getState() == ContactTracingTeam.State.FREE)
			{
				team.startIdentifying(resident);
				return true;
			}
		}
		return false;
	}

	public void addContactsForFollowUp(Resident resident)
	{
		// get all contacts of this resident
		Set<Resident> contacts = resident.getAllContacts();

		for (Resident contact : contacts)
		{
			if (!contact.hasBeenFollowedUp() && masterFollowUpList.size() < MAX_RESIDENTS_FOLLOWING)  // ignore any contacts that have already been followed up
			{
				double prob = ebolaABM.random.nextDouble();

				if (prob < Parameters.PERCENT_CONTACTS_IDENTIFIED)
					masterFollowUpList.add(contact);
			}
		}
	}

	public void step(SimState simState)
	{
		// add residents from masterFollowUpList to each team

		for (ContactTracingTeam team: allTeams)
		{
			if (team.getState() != ContactTracingTeam.State.IDENTIFYING &&
				team.getWorkLoad() < team.getFollowUpCapacity())
			{
				team.addFollowUps(masterFollowUpList);

				team.startTracing();
			}
		}

		// reassign work to free up teams that don't have a full followup workload
		for (int d=0; d<Parameters.DAYS_BETWEEN_CHECKS; d++)
			reassignFollowUpWork(d);

		// free up any teams that are following up but now have no residents to follow up due to reassignment
		for (ContactTracingTeam team: allTeams)
		{
			if (team.getState() == ContactTracingTeam.State.FOLLOWING_UP && team.getWorkLoad() == 0)
				team.setState(ContactTracingTeam.State.FREE);
		}
	}

	public void reassignFollowUpWork(int day)
	{
		ArrayList<ArrayList<Resident>> allDailyLists = new ArrayList<ArrayList<Resident>>();

		// make a consolidated list of all team's follow up lists for one day,
		// then reassign residents over that list

		for (ContactTracingTeam team: allTeams)
			allDailyLists.add(team.getDailyList(day));

		int nReassigned = 0;

		ListIterator<ArrayList<Resident>> forwardIterator  = allDailyLists.listIterator();
		ListIterator<ArrayList<Resident>> backwardIterator = allDailyLists.listIterator(allDailyLists.size());

		ArrayList<Resident> toList   = forwardIterator.next();
		ArrayList<Resident> fromList = backwardIterator.previous();

		printIndexes("Start", forwardIterator, backwardIterator);

		while (forwardIterator.nextIndex()-1 < backwardIterator.previousIndex()+1)
		{
			// find first list from current forward position that is not full
			while(toList.size() == Parameters.DAILY_FOLLOW_UPS && forwardIterator.hasNext())
			{
				toList = forwardIterator.next();
				printIndexes("Find not full forward list ", forwardIterator, backwardIterator);
			}

			// break if fromList is < toList
			if (forwardIterator.nextIndex()-1 >= backwardIterator.previousIndex()+1)
			{
				printIndexes("Breaking after finding Not-Full TO list", forwardIterator, backwardIterator);
				break;
			}

			// find first list from current backward position that is not empty
			while (fromList.size() == 0 && backwardIterator.hasPrevious())
			{
				fromList = backwardIterator.previous();
				printIndexes("Find not empty backward list", forwardIterator, backwardIterator);
			}

			// break if fromList is < toList
			if (forwardIterator.nextIndex()-1 >= backwardIterator.previousIndex()+1)
			{
				printIndexes("Breaking after finding Not-Empty FROM  list", forwardIterator, backwardIterator);
				break;
			}

			// move as many elements from fromList to toList
			// as long as we have space in toList and elements available in fromList

			int nMoved = 0;

			while(toList.size() < Parameters.DAILY_FOLLOW_UPS && fromList.size() > 0)
			{
				toList.add(fromList.remove(0));
				nMoved++;
			}

			// track total number of residents moved
			if (nMoved > 0)
			{
				if (DEBUG)
					System.out.println("Moved " + nMoved + " residents");

				nReassigned += nMoved;
			}

			// if we stopped moving residents because toList filled up, then move toList to the next list
			if (toList.size() == Parameters.DAILY_FOLLOW_UPS && forwardIterator.hasNext())
			{
				toList = forwardIterator.next();
				printIndexes("Advance toList  ", forwardIterator, backwardIterator);
			}

			// break if fromList is < toList
			if (forwardIterator.nextIndex()-1 >= backwardIterator.previousIndex()+1)
			{
				printIndexes("Breaking after advancing TO list", forwardIterator, backwardIterator);
				break;
			}

			// if we stopped moving residents because fromList emptied, then move fromList to the previous list
			if (fromList.size() == 0 && backwardIterator.hasPrevious())
			{
				fromList = backwardIterator.previous();
				printIndexes("Reverse fromList", forwardIterator, backwardIterator);
			}
		}
		printIndexes("Out of while loop", forwardIterator, backwardIterator);

		System.out.println("Finished reassigning " + nReassigned + " follow-up residents");
		System.out.println("-------------------------------------");
	}

	void printIndexes(String msg, ListIterator f, ListIterator b)
	{
		if (DEBUG)
			System.out.println(msg + " Fwd.index="+ (f.nextIndex()-1) + ", Back.index=" + (b.previousIndex()+1));
	}

	ContactTracingTeam getTeam(int index)
	{
		return allTeams.get(index);
	}

	public static void main(String args[])
	{
		int nResidents = 0;
		int DAY        = 0;

		// Test cases with 11 teams (odd number)
		// int fill[] = {3, 10, 4, 4, 10, 10, 8, 9, 0, 0, 5};
		// int fill[] = {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
		// int fill[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		// int fill[] = {10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0};
		// int fill[] = {0, 0, 0, 0, 0, 10, 10, 10, 10, 10, 10};

		// Test cases with 10 teams (even number)
		int fill[] = {3, 10, 4, 4, 10, 10, 8, 9, 0, 5};
		// int fill[] = {10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
		// int fill[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		// int fill[] = {10, 10, 10, 0, 0, 0, 0, 0, 0, 0};
		// int fill[] = {0, 0, 0, 0, 0, 10, 10, 10, 10, 10};

		int nTeams = fill.length;

		ContactTracerManager manager = new ContactTracerManager(nTeams, null);

		for (int i=0; i<nTeams; i++)
		{
			ArrayList<Resident> dailyList = new ArrayList<Resident>();

			int dailyAmount = fill[i];

			for (int j=0; j<dailyAmount; j++)
			{
				dailyList.add(new Resident(null, null, 0, 0, false));
				nResidents++;
			}
			ContactTracingTeam team = manager.getTeam(i);

			team.setDailyList(dailyList, DAY);
			team.setState(ContactTracingTeam.State.FOLLOWING_UP);
			System.out.println(team);
		}

		System.out.println("Total Residents = " + nResidents);
		System.out.println("-------------------------------------");

		manager.reassignFollowUpWork(DAY);


		int finalResidents = 0;

		for (int i=0; i<nTeams; i++)
		{
			ContactTracingTeam team = manager.getTeam(i);
			ArrayList<Resident> list = team.getDailyList(DAY);

			finalResidents += list.size();
			System.out.println(team);
		}
		System.out.println("Total Residents after reassign = " + finalResidents);
	}
}
