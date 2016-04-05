import os
from os import walk

paramsweep = "paramsweep_7"
output = "runs_7"

dirs = []
for (dirpath, dirnames, filenames) in walk(paramsweep):
    dirs.extend(dirnames)
    break

for directory in dirs:
    contact_rate = float(directory[6:12])
    pop_flow = float(directory[13:])
    dir_string = format(pop_flow, '.2f') + "-" + format(contact_rate, '.3f')
    os.system("mkdir " + output + "/" + dir_string)
    os.system("mkdir " + output + "/" + dir_string + "/images")
    print "python analyze_run.py " + paramsweep + "/" + directory + "/ " + output + "/" + dir_string + "/"
    os.system("python analyze_run.py " + paramsweep + "/" + directory + "/ " + output + "/" + dir_string + "/")
