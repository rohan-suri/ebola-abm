import matplotlib as mpl
mpl.use("Agg")

import seaborn as sns
from os import walk
import pandas as pd
import json

run_dir = "runs/"
input_folders = []
for (dirpath, dirnames, filenames) in walk(run_dir):
    input_folders.extend(dirnames)
    break
df = pd.DataFrame(columns=('contact_rate', 'pop_flow', 'dtw'))
df = df[df.columns].astype(float)

index_counter = 0

for folder in input_folders:
    contact_rate = float(folder[4:])
    pop_flow = float(folder[:-6])
    with open(run_dir + folder + "/averaged_run.json") as data_file:
        dat = json.load(data_file)
        dtw = dat["meta_data"]["dtw_dist"]
        df.loc[index_counter] = [contact_rate, pop_flow, dtw]
        index_counter += 1
    #print pop_flow

#delete the last when contact rate = 0.011 and pop_flow=0.9
#df = df[df.contact_rate != 0.011]
#df = df[df.pop_flow != 0.9]

df = df.pivot("contact_rate", "pop_flow", "dtw")

print df
ax = sns.heatmap(data=df, annot=True, fmt=".1f", cmap="YlGnBu")

import matplotlib.pyplot as plt
plt.show()
fig = plt.gcf()
fig.savefig("heatmap.pdf", format="pdf", dpi=1000)
plt.clf()

#flights = sns.load_dataset("flights")
#print flights

