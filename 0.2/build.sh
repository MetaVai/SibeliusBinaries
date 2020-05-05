#!/bin/bash

javac -cp Sibelius.jar akillesai/TestSibelius.java
cat libSibeliusAPI_* > libSibeliusAPI.so
chmod a+x *.so
