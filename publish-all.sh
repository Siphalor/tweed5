#!/usr/bin/env sh

cd tweed5 || exit
../gradlew publish
cd .. || exit
cd tweed5-minecraft || exit
./helpers/run-each-mc.sh ../gradlew publish
cd .. || exit
