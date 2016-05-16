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

	public  ArrayList<BurialTeam> allTeams;
	private static final boolean  DEBUG = false;

	public BurialTeamManager(EbolaABM _ebolaABM)
	{
		ebolaABM      = _ebolaABM;
		allTeams      = new ArrayList<BurialTeam>();
	}

	private void addBurialTeam()
	{
		BurialTeam bt = new BurialTeam("Burial Team " + allTeams.size());
		allTeams.add(bt);
		ebolaABM.schedule.scheduleRepeating(bt);
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

	public void updateTeams(int hr) {
		int num_add = (int)Math.round((hr/Parameters.HR_PER_BURIAL_TEAM)) - allTeams.size();
		for(int i = 0; i < num_add; i++) {
			this.addBurialTeam();
		}
	}
}
