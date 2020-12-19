#python3 ex2_script.py examples/ast out1 results
import os
from os import listdir
import subprocess
import sys
if len(sys.argv) < 4: exit()
infolder = sys.argv[1]
outfolder = sys.argv[2]
resultsfolder = sys.argv[3]
# create ll files

if not os.path.exists(outfolder):
    os.makedirs(outfolder)

if not os.path.exists(resultsfolder):
    os.makedirs(resultsfolder)

for file in listdir(infolder):
    if file.endswith('.xml'):
        print(f"Generating LLVM from {file}")
        os.system(f'java -jar mjavac.jar unmarshal compile {infolder}/{file} {outfolder}/{os.path.splitext(os.path.splitext(file)[0])[0]}.ll')
 
# execute ll files
for file in listdir(outfolder):
    if file.endswith('.ll'):
        print(f"Running on {file}")
        os.system(f'lli out1/{file} > {resultsfolder}/{file}.out')
        os.system(f'lli expected/{file} > expected/{resultsfolder}/{file}.out')
