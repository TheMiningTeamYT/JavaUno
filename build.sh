#!/bin/bash
wine32 C:\\jdk1.1.8\\bin\\javac.exe -classpath C:\\jdk1.1.8\\lib\\classes.zip\;C:\\jdk1.1.8\\lib\\swingall.jar -d bin/ $(winepath -w $(find src/ -type f -iname "*.java"))
