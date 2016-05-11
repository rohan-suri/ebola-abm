import sim.util.Int2D;

import java.time.LocalDate;
import java.util.*;

/**
 * Created by rohansuri on 5/8/16.
 */
public class ETC extends Structure
{
	private int                 capacity;
	private ArrayList<Resident> patients;
	private int                 workerCount;
	private LocalDate           whenOpened;
	private String              name;
	private String              country;
	private boolean             opened;

	public ETC(String _country, String _name, int _capacity, Int2D _location, LocalDate _whenOpened)
	{
		super(_location);
		country     = _country;
		name        = _name;
		capacity    = _capacity;
		location    = _location;
		workerCount = capacity * 2; // assume 2 workers per bed
		whenOpened  = _whenOpened;
		opened      = false;

		patients = new ArrayList<Resident>(capacity);
	}

	Boolean requestAdmission(Resident newPatient)
	{
		if (opened && patients.size() < capacity)
		{
			patients.add(newPatient);
			members.add(newPatient);
			newPatient.admittedToETC(this);
			return true;
		}
		else
			return false;
	}

	int clearBeds()
	{
		int countCleared = 0;
		Iterator <Resident> patientIterator = patients.iterator();

		while (patientIterator.hasNext())
		{
			Resident patient = patientIterator.next();

			if (patient.getHealthStatus() == Constants.DEAD || patient.getHealthStatus() == Constants.RECOVERED) {
				patientIterator.remove();
				members.remove(patient);
				countCleared++;
				patient.dischargeFromETC();
			}
		}

		return countCleared;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(64);

		return sb.append("ETC Name: ").append(name).append(opened? " [open], " : " [closed], ").append(patients.size()).append(" patients, ").append(capacity).append(" beds").toString();
	}

	// Getters

	public int getCapacity()
	{
		return capacity;
	}

	public int getWorkerCount()
	{
		return workerCount;
	}

	public ArrayList<Resident> getPatients()
	{
		return patients;
	}

	public LocalDate getWhenOpened()
	{
		return whenOpened;
	}

	public String getName()
	{
		return name;
	}

	public boolean isOpen()
	{
		return opened;
	}

	// Setters

	public void setCapacity(int _capacity)
	{
		capacity = _capacity;
	}

	public void setWorkerCount(int _workerCount)
	{
		workerCount = _workerCount;
	}

	public void setOpened(boolean _opened)
	{
		opened = _opened;
	}
	public boolean hasSpace()
	{
		return patients.size() < capacity;
	}
}
