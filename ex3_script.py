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

pass_count = 0
fail_count = 0


for file in listdir(infolder):
    if file.endswith('.xml'):
        print("==================")
        print(f"Running semantic analysis on {file}")
        command = f'java -jar mjavac.jar unmarshal semantic {infolder}/{file} {outfolder}/{file}.txt'
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
                if "invalid" in file.lower() or "error" in file.lower() or "bad" in file.lower():
                    print("FAILED: expected: ERROR, got: OK")
                    fail_count += 1

                else:
                    print("SUCCESS")
                    pass_count += 1
            else:
                if "invalid" not in file.lower() and "error" not in file.lower() and "bad" not in file.lower():
                    print("FAILED: expected: OK, got: ERROR")
                    fail_count += 1

                else:
                    print("SUCCESS")
                    pass_count += 1

print("========================")
print(f"Passed: {pass_count} Failed: {fail_count}")