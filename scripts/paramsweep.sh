#!/bin/bash
#conducts param sweep
 
PARAM1=SUSCEPTIBLE_TO_EXPOSED
PARAM1_START=0.0110
PARAM1_INTERVAL=0.0010
PARAM1_COUNT=7
PARAM1_VALUE=0.0100
 
PARAM2=POPULATION_FLOW_SCALE
PARAM2_START=0.30
PARAM2_INTERVAL=0.1
PARAM2_COUNT=7
PARAM2_VALUE=0.30
 
TRIALS=20
#num of trials to complete before going to the next one
TRIAL_STEPS=5
 
#echo "s/$PARAM1 = 0.0000/$PARAM1 = $PARAM1_START/g"
#set param file to initial vaules
sed -i "s/$PARAM1 = 0.0000/$PARAM1 = $PARAM1_START/g" ~/assip/ebola-abm/src/Parameters.java
sed -i "s/$PARAM2 = 0.00/$PARAM2 = $PARAM2_START/g" ~/assip/ebola-abm/src/Parameters.java
 
 
for m in $(seq 1 1 $TRIALS)
do
        COUNTER1=0
        #check if the progress file is here
        if [ ! -f ~/assip/paramsweeps/paramsweep1/progress_contact ]; then
                echo 0 > ~/assip/paramsweeps/paramsweep1/progress_contact
        fi
        COUNTER1=$(cat ~/assip/paramsweeps/paramsweep1/progress_contact)
 
        while [ $COUNTER1 -lt $PARAM1_COUNT ]; do
 
                #update to new param
                NEW_VALUE1=$(python -c "print \"%.4f\" % ($PARAM1_START - ($PARAM1_INTERVAL*$COUNTER1)) ")
                sed -i "s/$PARAM1 = $PARAM1_VALUE/$PARAM1 = $NEW_VALUE1/g" ~/assip/ebola-abm/src/Parameters.java
                PARAM1_VALUE=$NEW_VALUE1
                echo "$PARAM1 = $PARAM1_VALUE"
 
                COUNTER2=0
                #check if the progress file is here
                if [ ! -f ~/assip/paramsweeps/paramsweep1/progress_pop ]; then
                        echo 0 > ~/assip/paramsweeps/paramsweep1/progress_pop
                fi
                COUNTER2=$(cat ~/assip/paramsweeps/paramsweep1/progress_pop)
 
                while [ $COUNTER2 -lt $PARAM2_COUNT ]; do
                         #update to new param
                        NEW_VALUE2=$(python -c "print \"%.2f\" % ($PARAM2_START + ($PARAM2_INTERVAL*$COUNTER2)) ")
                        sed -i "s/$PARAM2 = $PARAM2_VALUE/$PARAM2 = $NEW_VALUE2/g" ~/assip/ebola-abm/src/Parameters.java
                        PARAM2_VALUE=$NEW_VALUE2
                        echo "$PARAM2 = $PARAM2_VALUE"
                        mkdir ~/assip/paramsweeps/paramsweep1/run${COUNTER1}${COUNTER2}-${PARAM1_VALUE}-${PARAM2_VALUE}
 
                        #check if the progress file is here
                        if [ ! -f ~/assip/paramsweeps/paramsweep1/run${COUNTER1}${COUNTER2}-${PARAM1_VALUE}-${PARAM2_VALUE}/progress ]; then
                                echo 0 > ~/assip/paramsweeps/paramsweep1/run${COUNTER1}${COUNTER2}-${PARAM1_VALUE}-${PARAM2_VALUE}/progress
                        fi
                        PROGRESS=$(cat ~/assip/paramsweeps/paramsweep1/run${COUNTER1}${COUNTER2}-${PARAM1_VALUE}-${PARAM2_VALUE}/progress)
                        let PROGRESS=PROGRESS+1
                        #do each trial here
                        TEMP=0
                        let TEMP=PROGRESS+TRIAL_STEPS
                        for i in $(seq $PROGRESS 1 $TEMP)
                        do
                                echo "Trial #${i}"
                                #run each in a seperate terminal
                                mkdir ~/assip/paramsweeps/paramsweep1/run${COUNTER1}${COUNTER2}-${PARAM1_VALUE}-${PARAM2_VALUE}/trial${i}
                                cd ~/assip/ebola-abm/
                                ./run_no_ui &> ~/assip/paramsweeps/paramsweep1/run${COUNTER1}${COUNTER2}-${PARAM1_VALUE}-${PARAM2_VALUE}/trial${i}/log.txt &
                                cd ~/
                                ./run_recorder.sh ~/assip/paramsweeps/paramsweep1/run${COUNTER1}${COUNTER2}-${PARAM1_VALUE}-${PARAM2_VALUE}/trial${i}/ trial_ "$PARAM1 = $PARAM1_VALUE\n$PARAM2 = $PARAM2_VALUE"
                                #save progress in file
                                echo ${i} > ~/assip/paramsweeps/paramsweep1/run${COUNTER1}${COUNTER2}-${PARAM1_VALUE}-${PARAM2_VALUE}/progress
                        done
                        let COUNTER2=COUNTER2+1
                        echo $COUNTER2 > ~/assip/paramsweeps/paramsweep1/progress_pop
                done
                let COUNTER1=COUNTER1+1
                echo $COUNTER1 > ~/assip/paramsweeps/paramsweep1/progress_contact
 
                COUNTER2=0
                echo $COUNTER2 > ~/assip/paramsweeps/paramsweep1/progress_pop
        done
        rm -rf ~/assip/paramsweeps/paramsweep1/progress_contact
        rm -rf ~/assip/paramsweeps/paramsweep1/progress_pop
done
