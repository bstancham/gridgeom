#!/bin/bash

GTJAR=build/libs/gridgeom.jar
# TESTGUIJAR=build/libs/gridgeom-testgui.jar
JARS=$GTJAR:$TESTGUIJAR

TESTGUI_CLASSES=build/classes/java/testgui

# java -cp $JARS info.bschambers.gridgeom.testgui.App
#java -cp $GTJAR:$TESTGUI_CLASSES info.bschambers.gridgeom.testgui.App
#jdb -classpath $GTJAR:$TESTGUI_CLASSES -sourcepath src/main/java:src/testgui/java info.bschambers.gridgeom.testgui.App
jdb -classpath build/libs/gridgeom.jar:build/classes/java/testgui -sourcepath src/main/java:src/testgui/java info.bschambers.gridgeom.testgui.App
