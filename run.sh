#!/bin/sh
file=$1
shift
params="$@"

groovy blueprintVizualizer.groovy $params > $file.dot

dot -Tpng -o $file.png $file.dot

rm $file.dot