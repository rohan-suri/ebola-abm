import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by rohansuri on 5/10/16.
 */
public class ContactTracerManager
{
	//	Manages teams of contact tracers

	private EbolaABM                      ebolaABM;
	private int                           numberOfTeams;

	private ArrayList<ContactTracingTeam> allTeams;
	private ArrayList<Resident>           masterFollowUpList;
	private int                           MAX_RESIDENTS_FOLLOWING = numberOfTeams * ContactTracingTeam.getFollowUpCapacity();

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

		for (Resident contact: contacts)
		{
			if (!contact.hasBeenFollowedUp() && masterFollowUpList.size() < MAX_RESIDENTS_FOLLOWING)  // ignore any contacts that have already been followed up
			{
				double prob = ebolaABM.random.nextDouble();

				if (prob < Parameters.PERCENT_CONTACTS_IDENTIFIED)
					masterFollowUpList.add(contact);
			}
		}

		// add residents to be followed up to each team

		for (ContactTracingTeam team: allTeams)
		{
			if (team.getState() != ContactTracingTeam.State.IDENTIFYING &&
				team.getWorkLoad() < team.getFollowUpCapacity())
			{
				team.addFollowUps(masterFollowUpList);

				team.startTracing();
			}
		}
	}
}
