#!/bin/sh

mkdir -p client-files server-files

INPUT() {
    echo key
    echo init
    echo list
    for i in $(ls test-files/)
    do
        cp "test-files/$i" "server-files/$i"
        echo dwnl "$i"
        echo chalpart "$i"
        echo chalpart "$i"
        echo chal "$i"
        echo stat "$i"
        # sha256sum "server-files/$i" "test-files/$i"
    done
    echo quit
}

for i in test-files/*
do
    sha256sum "$i"
done

INPUT | java -cp proj.jar Client
sha256sum client-files/* server-files/*
# INPUT

# rm server-files/* client-files/*
