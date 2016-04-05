# An Agent Based Model for Ebola
This is the first spatially expilicit, 1:1, agent based model for the spread of Ebola in Guinea, Sierra Leone, and Liberia. For questions email rohan[at]surilabs.com

##System Requirements
- At least 16 GB of RAM
- At least 1.5 GB of space for additional files
- Java 1.8+ installed
- Bash

##Getting Started
1. To run the model start by cloning this repo.</br>
	```
        git clone https://github.com/rohan-suri/ebola-abm
	```
2. Run the 'setup.sh' script. (Note script is in bash)</br>
	```
	./setup.sh
	```
3. Start the model with or without UI</br>
	```
	./run or ./run_no_ui
	```

##Checking Output
After a run is finished, model output will be written in CSV files located in the output/ directory
###3 Types of Output
- Cumulative cases for each of the three countries (liberia_cumulative.csv)
- Cumulative cases for each district.  Files will be [IPUMS_ID].csv
- Effective contact rates for each country ()

##Parameters
- All paraemters are located in the Parameters.java file
- Parameters derived from a number of sources including previous literature and Labour Market Surveys
- Fitted parameters include:
	- Contact Rate: the probablity of infecting another agent in the same structure at each time step 
	- Populuation Flow Scale: Scales the movement of agents between districts
