#script to average trials and produce stats for a trial
import matplotlib as mpl
mpl.use("Agg")

def DTWDistance(s1, s2, w=None):
        '''
        Calculates dynamic time warping Euclidean distance between two
        sequences. Option to enforce locality constraint for window w.
        '''
        DTW={}

        if w:
            w = max(w, abs(len(s1)-len(s2)))

            for i in range(-1,len(s1)):
                for j in range(-1,len(s2)):
                    DTW[(i, j)] = float('inf')
        else:
            for i in range(len(s1)):
                DTW[(i, -1)] = float('inf')
            for i in range(len(s2)):
                DTW[(-1, i)] = float('inf')

        DTW[(-1, -1)] = 0

        for i in range(len(s1)):
            if w:
                for j in range(max(0, i-w), min(len(s2), i+w)):
                    dist= (s1[i]-s2[j])**2
                    DTW[(i, j)] = dist + min(DTW[(i-1, j)],DTW[(i, j-1)], DTW[(i-1, j-1)])
            else:
                for j in range(len(s2)):
                    dist= (s1[i]-s2[j])**2
                    DTW[(i, j)] = dist + min(DTW[(i-1, j)],DTW[(i, j-1)], DTW[(i-1, j-1)])

        return np.sqrt(DTW[len(s1)-1, len(s2)-1])

import scipy.stats
import scipy as sp
def mean_confidence_interval(data, confidence=0.65):
    a = 1.0*np.array(data)
    n = len(a)
    m, se = np.mean(a), scipy.stats.sem(a)
    h = se * sp.stats.t._ppf((1+confidence)/2., n-1)
    return h

def euclid_dist(t1,t2):
    import numpy as np
    s = 0
    for i in range(len(t1)):
        s += (t1[i] - t2[i])**2
    return np.sqrt(s)

def readEffectiveRate(file):
    import os.path
    if not os.path.isfile(file):
        print "no EFFECTIVE RATE found!"
        return []
    rate_lines = open(file).read().split('\n')
    ts = []
    for line in rate_lines:
        tokens = line.split(',')
        if len(tokens) > 1:
            ts.append((float(tokens[0]), float(tokens[1])))
    return ts

def readInCumCases(file):
    cum_lines = open(file).read().split('\r')
    cases = []
    cases.append(0)
    for line in cum_lines:
        tokens = line.split(',')
        if len(tokens) > 1:
            cases.append(float(tokens[1]))
    final_list = []
    for i in range(len(cases)-1):
        #get slope
        slope = (cases[i+1]-cases[i])/7.0
        for j in range(7):
            final_list.append((slope*j) + cases[i])
    final_list.append(len(cases)-1)
    return final_list

def createImage(file_name, cases, index, color, country):
    final_list = []
    for case in cases:
        if len(case) >= index:
            final_list.append(case[:index])
        else:
            print "Found case length shorter than " + str(index)
            print str(case)

    # make all of them the same length
    # for case in cases:
    #     while len(cases) < max_length:
    #         cases.append(np.nan)

    import seaborn as sns

    #create graph
    ax = sns.tsplot(data=final_list, color=sns.xkcd_rgb[color], ci=[95])#err_style="boot_traces", n_boot=500,
    ax.set_title("Cumulative cases")
    ax.set_xlabel('Days since start')
    ax.set_ylabel('Cumulative Cases')
    ax.set_label(country)
    # import matplotlib.pyplot as plt
    #
    # #fig = ax.figure()
    # #fig.add_subplot(ax)
    # #ax.plot()
    # fig = plt.gcf()
    # fig.savefig(file_name)
    # plt.clf()



import sys
import json

directory = sys.argv[1]
output_directory = sys.argv[2]

import os
import pandas as pd
import numpy as np
from os import walk

input_trials = []
for (dirpath, dirnames, filenames) in walk(directory):
    input_trials.extend(dirnames)
    break

run_admin = {}
#contains jsons of each trial
trials = []

actual_cases = {}
countries = {"liberia":[], "guinea":[], "sierra_leone":[]}
country_colors = {"liberia":"pale red", "guinea":"windows blue", "sierra_leone":"boring green"}
actual_cases_colors = {"guinea":"deep blue", "liberia":"dark red", "sierra_leone":"dark green"}
effective_rates = {"liberia":[], "guinea":[], "sierra_leone":[]}
max_length = 0
CUTOFF = 320 

#iterate through each trial and collect relavent info
max_out = 0
drop_out = 0

# choose_num = 10
# import random
# rand = random.sample(range(len(input_trials)), choose_num)
# temp = []
# print "choice = " + str(rand)
# for i in rand:
#     temp.append(input_trials[i])
# input_trials = temp

for trial_directory in input_trials:
    if "trial" in trial_directory:
        #check if output exists
        files = []
        for (dirpath, dirnames, filenames) in walk(directory + trial_directory + "/output/"):
            files.extend(filenames)
            break
        if len(files) == 0:
            print trial_directory + "\tOUTPUT IS EMPTY!!"
            continue

        #create the json for this directory
        os.system("python3 createJSON.py " + directory + trial_directory + "/output/ " + trial_directory + ".json")

        #now we read in each json
        with open(directory + trial_directory + "/output/" + trial_directory + ".json") as data_file:
            data = json.load(data_file)
            trials.append(data)

        #read in cumalative cases for each country
        total = 0
        length = 0
        for country in countries:
            #read in effective reproduction rates
            effective_rates[country].extend(readEffectiveRate(directory + trial_directory + "/output/" + country + "_reproductive_rate.csv"))

            cum_lines = open(directory + trial_directory + "/output/" + country + "_cumalative.csv").read().split('\n')
            cases = []
            for line in cum_lines:
                tokens = line.split(',')
                if len(tokens) > 1:
                    cases.append(float(tokens[1]))
            countries[country].append(cases)
            if len(cases) > 10:
                total += cases[len(cases)-1] - cases[len(cases)-10]
            length = len(cases)
        #print str(total) + "\t" + str(length)
        if length < 359:
            if total == 0:
                drop_out += 1
                #this is to add last number to the end and extend it
                for country in countries:
                    cases = countries[country][len(countries[country])-1]

                    #check if cases is completely empty and if so remove it
                    # if cases[len(cases)-1] == 0:
                    #     countries[country] = countries[country][:-1]
                    #     print "found trial with zero cases"
                    #     continue
                    val_to_add = 0
                    if len(cases) > 0:
                        val_to_add = cases[len(cases)-1]
                    while len(cases) <= CUTOFF:
                        cases.append(val_to_add)
            else:
                max_out += 1
                #this is to extend the premature end to a max out run
                # for country in countries:
                #     cases = countries[country][len(countries[country])-1]
                #     val_to_add = 0
                #     if len(cases) > 1:
                #         val_to_add = (cases[len(cases)-1]/cases[len(cases)-1])*cases[len(cases)-1]
                #     while len(cases) <= CUTOFF:
                #         val_to_add = (cases[len(cases)-1]/cases[len(cases)-1])*cases[len(cases)-1]
                #         cases.append(val_to_add)

#read in the actual cases
actual_cases["guinea"] = readInCumCases("guinea_actual.csv")
actual_cases["liberia"] = readInCumCases("liberia_actual.csv")
actual_cases["sierra_leone"] = readInCumCases("sierra_leone_actual.csv")

#get average cases cumulative
average_cases = {"liberia":[(0,0)] * 362, "guinea":[(0,0)] * 362, "sierra_leone":[(0,0)] * 362}

# just use top 10 ranking
# for country in countries:
#     dtw_sort = []
#     for i in range(len(countries[country])):
#         cases = countries[country][i]
#         dtw_similarity = DTWDistance(cases[:CUTOFF], actual_cases[country][:CUTOFF])
#         dtw_sort.append((dtw_similarity, i))
#     dtw_sort.sort()
#     dtw_sort = dtw_sort[:10]
#     temp = []
#     for pair in dtw_sort:
#         temp.append(countries[country][pair[1]])
#     countries[country] = temp



for country in countries:
    for cases in countries[country]:
        for i in range(len(cases)):
            average_cases[country][i] = ((average_cases[country][i][0]+cases[i]), (average_cases[country][i][1]+1))
#print average_cases["guinea"][:50]
for country in average_cases:
    for i in range(len(average_cases[country])):
        #print country + "\t" + str(average_cases[country][i])
        if average_cases[country][i][1] != 0 or average_cases[country][i][0] != 0:
            average_cases[country][i] = average_cases[country][i][0] / average_cases[country][i][1]
        else:
            average_cases[country][i] = 0
        #print country + "\t" + str(i) + "\t" + str(average_cases[country][i])

#get average effective rate
for country in ["guinea", 'liberia', 'sierra_leone']:
    #effective_rates[country].sort()
    #print(effective_rates[country])
    avg_list_schools_open = []
    avg_list_schools_closed = []
    for pair in effective_rates[country]:
        if pair[0] < 80:
            avg_list_schools_open.append(pair[1])
        else:
            avg_list_schools_closed.append(pair[1])

    print("average effective rate " + country + " time schools open: " + str(np.average(avg_list_schools_open)))
    print("average effective rate " + country + " time schools closed: " + str(np.average(avg_list_schools_closed)))


print("guinea average effective rate = " + str(np.average(effective_rates["guinea"])))
print("liberia average effective rate = " + str(np.average(effective_rates["liberia"])))
print("sierra_leone average effective rate = " + str(np.average(effective_rates["sierra_leone"])))


print("")
#create the images for each cumulative country
index = CUTOFF
import matplotlib.lines as mlines
while index < CUTOFF+1:
    for country in ['liberia', 'sierra_leone', 'guinea']:
        createImage(country + "_" + str(index) + ".png", countries[country], index, country_colors[country], country)
    for country in ['liberia', 'sierra_leone', 'guinea']:
        if len(actual_cases[country]) > 0:
            createImage("some.png", [actual_cases[country]], index, actual_cases_colors[country], 0)
    import matplotlib.pyplot as plt
    #fig = ax.figure()
    #fig.add_subplot(ax)
    #ax.plot()
    plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)
    fig = plt.gcf()
    fig.savefig(output_directory + "images/all_" + str(index) + ".png")
    plt.clf()

    index += 1

#calculate maximum stdev for guinea
final_values = []
for cases in countries["guinea"]:
    if len(cases) >= CUTOFF:
        final_values.append(cases[CUTOFF-1])
max_stdev = mean_confidence_interval(final_values)

#get averages for each district
lengths = []
num_too_high = 0
num_too_low = 0
for trial in trials:#go through each trial
    #go through each country
    trial_sum = 0;
    end_points = list()
    for country in trial:
        if country not in run_admin:
            run_admin[country] = {}

        for admin_id in trial[country]:

            if admin_id not in run_admin[country]:
                run_admin[country][admin_id] = list()
            if len(trial[country][admin_id]) > 10:
                trial_sum += trial[country][admin_id][len(trial[country][admin_id])-1] - trial[country][admin_id][len(trial[country][admin_id])-10]
            lengths.append(len(trial[country][admin_id]))
            if len(trial[country][admin_id]) < CUTOFF:
                continue
            trial[country][admin_id] = trial[country][admin_id][:CUTOFF]
            for i in range(len(trial[country][admin_id])):

                if i >= len(run_admin[country][admin_id]):
                    run_admin[country][admin_id].append(list())
                val = trial[country][admin_id][i]
                run_admin[country][admin_id][i].append(val)
    #print str(trial_sum) + "\t" + str(lengths[len(lengths)-1])

#create an average for each district
for country in run_admin:
    for admin_id in run_admin[country]:
        for i in range(len(run_admin[country][admin_id])):
            run_admin[country][admin_id][i] = np.average(run_admin[country][admin_id][i])
dist = DTWDistance(average_cases["guinea"][:CUTOFF], actual_cases["guinea"][:CUTOFF])
euclid_dist = euclid_dist(average_cases["guinea"][:CUTOFF], actual_cases["guinea"][:CUTOFF])

#add average cumalative
run_admin["cumulative"] = {}
for country in average_cases:
    run_admin["cumulative"][country] = []
    for x in average_cases[country]:
        item = {}
        item["avg"] = x
        item["stdv"] = -1
        run_admin["cumulative"][country].append(item)

#calc metadata
percent_drop = drop_out*1.0/len(trials)
percent_max = max_out*1.0/len(trials)
percent_finish = (len(trials)-(drop_out+max_out*1.0))/len(trials)

#add in metadata
run_admin["meta_data"] = {}
run_admin["meta_data"]["dtw_dist"] = dist
run_admin["meta_data"]["percent_drop"] = percent_drop
run_admin["meta_data"]["percent_max"] = percent_max
run_admin["meta_data"]["percent_finish"] = (len(trials)-(drop_out+max_out*1.0))/len(trials)
run_admin["meta_data"]["num_trials"] = len(trials)
run_admin["meta_data"]["euclid_dist"] = euclid_dist
run_admin["meta_data"]["max_stdev"] = max_stdev


#write it to file
import json
json_data = json.dumps(run_admin)

f = open(output_directory + "averaged_run.json", 'w')
f.write(json_data)
f.close()

print("average length = " + str(np.average(lengths)))
print("num of trials = " + str(len(trials)))
print("percent drop out = " + str(drop_out*1.0/len(trials)))
print("percent max out = " + str(max_out*1.0/len(trials)))
print("percent finish = " + str((len(trials)-(drop_out+max_out*1.0))/len(trials)))
print("DTW similarity = " + str(dist))
print("Euclid similarity = " + str(euclid_dist))
print("Max standard deviation = " + str(max_stdev))

