#python3 ex2_script.py examples/ast out1 results
import os
from os import listdir
import subprocess
import sys
if len(sys.argv) < 3: exit()
infolder = sys.argv[1]
outfolder = sys.argv[2]

if not os.path.exists(outfolder):
    os.makedirs(outfolder)

for file in listdir(infolder):
    if file.endswith('.xml'):
        print("==================")
        print(f"Running semantic analysis on {file}")
        command = f'java -jar mjavac.jar unmarshal semantic {infolder}/{file} {outfolder}/{os.path.basename(file)}.txt'
        print(f"command: {command}")
        print("OUTPUT:")
        os.system(command)

        if not os.path.exists(f"{outfolder}/{os.path.basename(file)}.txt"):
            print(f"An error occurred. No output file was created.")
            continue

        with open(f"{outfolder}/{os.path.basename(file)}.txt", "r") as f:
            content = f.read()

            print("-------------")
            print("TEST RESULTS:")

            if "OK" in content:
                if "Invalid" in file:
                    print("FAILED: expected: ERROR, got: OK")

                else:
                    print("SUCCESS")
            else:
                if "Invalid" not in file:
                    print("FAILED: expected: OK, got: ERROR")

                else:
                    print("SUCCESS")

