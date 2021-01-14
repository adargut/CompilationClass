#!/bin/bash

# # Basic Usage
#
# To run the tests, run this script from within its parent directory (`ex4_tests`).
# The script will compare the output of the parser on every java file in the test
# case directory with the expected outputs found in the snapshot directory
# (ignoring trivia such as whitespace and line number tags); any mismatches will
# be reported, with their accompanying diffs being placed in `fail/`.
#
#
# # Controlling Test Runs
#
# The script accepts an optional glob pattern as its first argument. If it is
# provided, only tests whose name matches the pattern will be run. For exampe,
#
# ./tester.sh *sysout
#
# will run only the tests whose names end with `sysout`.
#
#
# # Updating Test Cases
#
# Running the script with the environment variable `UPDATE` set will cause it to
# update any mismatched snapshots instead of reporting them as failures. For instance,
#
# UPDATE=1 ./tester.sh parse_stmt_sysout
#
# will cause the snapshots for the `parse_stmt_sysout` test to be updated to
# whatever the parser currently outputs.
#
# This can also be used to add new test cases:
# 1. Add a new java file under `cases/`
# 2. Run the script once (the new test should fail)
# 3. Check the failure diffs; if they appear correct, re-run the script with
#    `UPDATE=1` to add the new snapshots.

INDIR=cases

OUTDIR=temp
SNAPDIR=snapshots
FAILDIR=fail

MJAVAC=../mjavac.jar

function process {
    if [[ $1 != $TESTCASE_PAT ]]; then
        return
    fi

    INNAME=$1.java
    ASTNAME=$1.xml
    ERRNAME=$1.stderr

    INFILE=$INDIR/$INNAME

    OUTAST=$OUTDIR/$ASTNAME
    OUTERR=$OUTDIR/$ERRNAME

    SNAPAST=$SNAPDIR/$ASTNAME
    SNAPERR=$SNAPDIR/$ERRNAME

    java -jar $MJAVAC parse marshal $INFILE $OUTAST 2> $OUTERR

    touch $OUTAST
    xmlstarlet ed -L -d //lineNumber $OUTAST &> /dev/null # normalize

    touch $SNAPAST
    touch $SNAPERR

    ASTDIFF=$(diff -u $SNAPAST $OUTAST)
    ASTSTATUS=$?

    ERRDIFF=$(diff -u $SNAPERR $OUTERR)
    ERRSTATUS=$?

    printf "$1: "

    if [[ "$ASTSTATUS" -eq 0 ]] && [[ "$ERRSTATUS" -eq 0 ]]; then
        printf "\x1b[0;32mSuccess\x1b[0m\n"
    else
        if [ -v UPDATE ]; then
            cp $OUTAST $SNAPAST
            cp $OUTERR $SNAPERR
            printf "\x1b[0;34mUpdated\x1b[0m\n"
        else
            [[ ASTSTATUS -ne 0 ]] && echo -n "$ASTDIFF" > $FAILDIR/$ASTNAME.diff
            [[ ERRSTATUS -ne 0 ]] && echo -n "$ERRDIFF" > $FAILDIR/$ERRNAME.diff
            printf "\x1b[0;31mFailed\x1b[0m\n"
        fi
    fi
}

if [[ $# -eq 0 ]]; then
    TESTCASE_PAT="*"
else
    TESTCASE_PAT="$1"
fi

mkdir -p $OUTDIR
mkdir -p $SNAPDIR

rm -rf $FAILDIR
mkdir -p $FAILDIR

for INFILE in $INDIR/*.java; do
    CASE="$(basename -- "$INFILE" .java)"
    process $CASE
done
