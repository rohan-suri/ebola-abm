import sys

#first arg is input directory
directory = sys.argv[1]

#second arg is output file
file_name = sys.argv[2]

import os
from os import walk

input_files = []
for (dirpath, dirnames, filenames) in walk(directory):
    for filename in filenames:
        if "GIN" in filename or "SLE" in filename or "LBR" in filename:
            input_files.append(filename)
    break

data = {}
data["GIN"] = {}
data["LBR"] = {}
data["SLE"] = {}

for file in input_files:
    country = file[:3]
    admin_id = file[:-4][4:]
    data[country][admin_id] = []

    #no read in the file
    lines = open(directory + file).read().split("\n")
    for line in lines:
        tokens = line.split(",")
        if len(tokens) >= 2:
            data[country][admin_id].append(int(tokens[1]))
import json
json_data = json.dumps(data)

import os.path #remove file if necessary
if os.path.isfile(directory + file_name):
    os.system("rm -rf " + directory + file_name)

f = open(directory + file_name, 'w')
f.write(json_data)
f.close()

