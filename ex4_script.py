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
    if file.endswith('.java'):
        print("==================")
        print(f"Running lexical analysis and parsing on {file}")
        command = f'java -jar mjavac.jar parse marshal {infolder}/{file} {outfolder}/{file}.xml'
        print(f"command: {command}")
        print("OUTPUT:")
        os.system(command)

        print("TEST RESULTS:")

        if not os.path.exists(f"{outfolder}/{file}.xml"):
            if "invalid" in file.lower() or "error" in file.lower() or "bad" in file.lower():
                print("SUCCESS")
                pass_count += 1

            else:
                print("FAILED: expected: OK, got: ERROR")
                fail_count += 1

        else:
            if "invalid" in file.lower() or "error" in file.lower() or "bad" in file.lower():
                # print("FAILED: expected: ERROR, got: OK")
                # fail_count += 1
                command = f'java -jar mjavac.jar unmarshal print {outfolder}/{file}.xml {outfolder}/{file}.xml.java'
                os.system(command)

                with open(f"{outfolder}/{file}.xml.java", "rb") as res:
                    with open(f"{infolder}/{file}", "rb") as orig:
                        if res.read() != orig.read():
                            print(f"FAILED: code not equal: {outfolder}/{file}.xml.java  {infolder}/{file}")
                            fail_count +=1
                        else:
                            print("SUCCESS")
                            pass_count += 1

            else:
                command = f'java -jar mjavac.jar unmarshal print {outfolder}/{file}.xml {outfolder}/{file}.xml.java'
                os.system(command)

                with open(f"{outfolder}/{file}.xml.java", "rb") as res:
                    with open(f"{infolder}/{file}", "rb") as orig:
                        if res.read() != orig.read():
                            print(f"FAILED: code not equal: {outfolder}/{file}.xml.java  {infolder}/{file}")
                            fail_count +=1
                        else:
                            print("SUCCESS")
                            pass_count += 1

print("========================")
print(f"Passed: {pass_count} Failed: {fail_count}")