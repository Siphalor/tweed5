pushd tweed5 || exit
../gradlew publish
popd || exit
pushd tweed5-minecraft || exit
./helpers/run-each-mc.sh ../gradlew publish
popd || exit
