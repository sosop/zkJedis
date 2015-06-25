#!/bin/bash

CONFDIR=$1
START=$2
NUM=$3

if [ "$NUM" == "" ]
then
    echo "以后完成..."
else
    killall redis-server 2>/dev/null
    sleep 2
    for i in `seq $NUM`
    do
        NODEDIR=$(( $START + $i - 1 ))
        FULLDIR=$CONFDIR"/"$NODEDIR"/redis.conf"
        if [ -f $FULLDIR ]
        then
            cd $CONFDIR/$NODEDIR
            rm nodes.conF 2>/dev/null
            redis-server redis.conf
        else
            echo $NODEDIR" not exsit!"
        fi
    done
fi


