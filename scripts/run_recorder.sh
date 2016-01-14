#!/bin/bash
#takes a screenshot every 10 minutes and copies over necesary information
#on a particular run
#arg 1 is the path to the directory to create the run and arg two is teh name of the run
 
COUNTER=0
MAX=200
function create_video {
        echo ""
        echo "Creating video..."
        let COUNTER=COUNTER-1
        #convert -delay 40 $(for i in $(seq 0 1 $COUNTER); do echo ${BASH_ARGV[1]}${BASH_ARGV[0]}${i}.jpg; done) ${BASH_ARGV[1]}animation.gif
        echo "Created video"
}
 
#get the comments file into the directory
echo "Enter comments for this run:"
comments=$3
echo "$comments" > ${1}comments.txt
 
#copy over the params file for that run
echo "Copy param file"
cp ~/assip/ebola-abm/src/Parameters.java $1params.java
 
#echo "Grabbing screens..."
#to give some time to move screens sleep for 10 seconds
sleep 1s
while [ $COUNTER -gt -1 ] && [ $COUNTER  -lt $MAX ]; do
        #gnome-screenshot -f "$1$2$COUNTER.jpg"
        #echo Saved $COUNTER picture
        let COUNTER=COUNTER+1
        sleep 30s
        ps aux | grep EbolaABM > ~/temp
        LINES=$(wc -l temp | cut -f1 -d' ')
        if [ $LINES -lt 2 ]
        then
                echo "run finished"
                #copy over run results
                mkdir $1/output/
                cp ~/assip/ebola-abm/output/* $1/output/
                MAX=0
        fi
        rm -rf ~/temp
done
killall java
if [ $MAX -gt 1 ]
then
        echo "ERROR ERROR ERROR Reached max timeout!"
fi
