#!/bin/bash

mvn exec:java -Dexec.mainClass="examples.NonPrivacy" & 
sleep 20
pid=$! &
echo "$pid" 

