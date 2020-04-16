#!/bin/bash

javac -cp Sibelius.jar TestSibelius.java
cat split_libSibeliusAPI_so_* > libSibeliusAPI.so
chmod a+x *.so
