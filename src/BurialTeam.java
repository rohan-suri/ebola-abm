import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by rohansuri on 5/13/16.
 */
public class BurialTeam implements Steppable
{
	public  enum State {FREE, BURYING};

	private State         state;
	private int           hoursBurying;
	private String        teamName;
	private Resident      whoIsBeingBuried;

	public BurialTeam(String _teamName)
	{
		teamName	 = _teamName;
		state		 = State.FREE;
		hoursBurying = 0;
	}

	public void startBurying(Resident resident)
	{
		if (state != State.FREE) throw new IllegalStateException("Burial team cannot start burying since it is not free");

		state            = State.BURYING;
		whoIsBeingBuried = resident;

		System.out.println("Started burying " + whoIsBeingBuried);
	}

	@Override
	public void step(SimState simState)
	{
		if (state == State.BURYING)
		{
			if (hoursBurying == Parameters.HOURS_TO_BURY)
			{
				// we are done burying, tell manager to add discovered contacts of this resident
				state        = State.FREE;
				hoursBurying = 0;

				System.out.println("Finished burying " + whoIsBeingBuried);
			}
			else
				hoursBurying++;

			return;
		}

		System.out.println(this);
	}

	State getState()
	{
		return state;
	}

	public void setState(State _state)
	{
		state = _state;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(teamName);

		if (state == State.FREE)
			sb.append(" is free");
		else if (state == State.BURYING)
			sb.append(" is burying for ").append(hoursBurying).append(" hours");

		return sb.toString();
	}
}
