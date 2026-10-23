#!/bin/bash
rm -rf build/bin
mkdir -p build/bin/com/loganius/Uno
cp -r src/com/loganius/Uno/Assets build/bin/com/loganius/Uno/
cp bin/java.policy.applet build/bin
wine32 C:\\jdk1.1.8\\bin\\javac.exe -classpath C:\\jdk1.1.8\\lib\\classes.zip\;C:\\jdk1.1.8\\lib\\swingall.jar -d build/bin/ $(winepath -w $(find src/ -type f -iname "*.java"))
jar cfe Uno.jar com.loganius.Uno.UnoGame -C build/bin .
