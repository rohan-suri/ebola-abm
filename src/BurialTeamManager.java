import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.*;

/**
 * Created by rohansuri on 5/13/16.
 */
public class BurialTeamManager
{
	//	Manages burial teams

	private EbolaABM              ebolaABM;
	private int                   numberOfTeams;

	public  ArrayList<BurialTeam> allTeams;
	private static final boolean  DEBUG = false;

	public BurialTeamManager(int _numberOfTeams, EbolaABM _ebolaABM)
	{
		ebolaABM      = _ebolaABM;
		numberOfTeams = _numberOfTeams;
		allTeams      = new ArrayList<BurialTeam>(numberOfTeams);

		for (int i=0; i < numberOfTeams; i++)
			allTeams.add(new BurialTeam("Burial team " + i));
	}

	public boolean applyForBurial(Resident resident)
	{
		// find first free team to start burying

		for (BurialTeam team: allTeams)
		{
			if (team.getState() == BurialTeam.State.FREE)
			{
				team.startBurying(resident);
				return true;
			}
		}
		return false;
	}
}
