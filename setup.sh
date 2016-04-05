#!/bin/bash
#basic setup script to organize directories
mkdir ../assip
mv ../ebola-abm/ ../assip/ebola-abm/
cd ../../assip/
cp ebola-abm/scripts/run_no_ui ebola-abm/run_no_ui
cp ebola-abm/scripts/run_no_ui ebola-abm/run
