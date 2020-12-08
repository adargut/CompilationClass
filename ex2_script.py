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
for file in listdir(infolder):
    if file.endswith('.xml'):
        os.system(f'java -jar mjavac.jar unmarshal compile {infolder}/{file} {outfolder}/{file}.ll')
 
# execute ll files
for file in listdir(outfolder):
    if file.endswith('.ll'):
        os.system(f'lli out1/{file} > {resultsfolder}/{file}.out')
