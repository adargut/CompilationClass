#!/bin/bash

CASEDIR=cases
SNAPDIR=snapshots

MJAVAC=../mjavac.jar

if [[ $# -ne 2 ]]; then
    echo "Usage: $0 <dirname> <prefix>"
    exit 1
fi

for AST in $1/*.xml; do
    CASENAME="$2_$(basename -- $AST .xml)"
    java -jar $MJAVAC unmarshal print $AST $CASEDIR/$CASENAME.java
    xmlstarlet ed -d //lineNumber $AST > $SNAPDIR/$CASENAME.xml # normalize
    sed -i "/xml-model/d" $SNAPDIR/$CASENAME.xml # remove schema decl
    echo $CASENAME
done
