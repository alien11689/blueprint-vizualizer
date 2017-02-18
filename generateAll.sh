#!/bin/sh

mkdir -p target

./run.sh target/fullBlueprint example/full/fullBlueprint.xml

./run.sh target/beanWithCallbackMethods example/firstCut/beanWithCallbackMethods.xml
./run.sh target/beanWithConfigurationProperties example/firstCut/beanWithConfigurationProperties.xml
./run.sh target/otherStuff example/firstCut/otherStuff.xml

./run.sh target/joinFirstCut example/firstCut/otherStuff.xml example/firstCut/beanWithConfigurationProperties.xml example/firstCut/beanWithCallbackMethods.xml

./run.sh target/joinFirstCutGrouped example/firstCut/otherStuff.xml example/firstCut/beanWithConfigurationProperties.xml example/firstCut/beanWithCallbackMethods.xml --no-group-by-file

./run.sh target/otherStuffWithoutMy example/firstCut/otherStuff.xml -e my1

