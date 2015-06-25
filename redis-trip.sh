#! /bin/bash

#########################################################################################################################################
# write by sosop
# date: 2015.1.20
# function: create redis3 cluster quickly
# desc functions
# create: ./redis-trip.sh create (should have cluster.conf file, you should modify the file content, but don't mmodify the file name)
# check: ./redis-trip.sh check ip:port (check this server is running, return this server's info in cluster)
# addNodes: ./redis-trip.sh addNodes ip1:port1 ip2:port2 ... (make the servers into the cluster as master)
# addSlaves: ./redis-trip.sh addSlaves slave1Ip:slaves1Port slave2Ip:slaves2Port [...] masterIp:masterPort
# delSlots: ./redis-trip.sh delSlots [-r | -s] ip:port slot1 slot2 ....
# addSlots: ./redis-trip.sh addSlots [-r | -s] ip:port slot1 slot2 ....
#########################################################################################################################################

PATH=$PATH

if [ $# == 0 ]
then
echo "ERROR: NO USAGE PARAMS"
exit 1
fi

DEFAULT_CONF="cluster.conf"

# whereis redis-cli
CLIENT=`which redis-cli`

if [ -z "$CLIENT" ]
then
    echo "make sure you have installed redis, catn't find redis-cli command"
    exit 1
fi

function isExistConfigFile() {
    if [ ! -f ./$DEFAULT_CONF ]
    then
        echo "ERROR: CAN NOT FIND FILE $DEFAULT_CONF";
        exit 1;
    fi
}

function create()
{
    isExistConfigFile;
    # join the cluster
    gawk 'BEGIN{curIP = "";curPort = "";CLIENT="'$CLIENT'"}
    { 
        split($1, arr, ":");
        if(curIP == "") 
        {
            curIP = arr[1];
            curPort = arr[2];
        }
        else
        {
            cmd = CLIENT" -h "curIP" -p "curPort" cluster meet "arr[1]" "arr[2]" > /dev/null";
            system(cmd);
        }

        for(i = 3; i <= NF; i++)
        {
            split($i, a, ":");
            system(CLIENT" -h "curIP" -p "curPort" cluster meet "a[1]" "a[2]" > /dev/null");
        }

    }
    END{print "加入集群完成..."}' $DEFAULT_CONF

    # allocation slots
    gawk 'BEGIN{CLIENT="'$CLIENT'";print "正在分配slots, 可能需要几秒......"}
    { 
        split($1, arr1, ":");
        split($2, arr2, "-");
        slots = "";
        print "清空"$1" data...";
        system(CLIENT" -h "arr1[1]" -p "arr1[2]" FLUSHALL > /dev/null");

        print "清空"$1" slots..."
        system(CLIENT" -h "arr1[1]" -p "arr1[2]" CLUSTER FLUSHSLOTS > /dev/null");

        for(i = arr2[1]; i <= arr2[2]; i++) {
            slots = slots" "i
        }
        if (slots != "") {
            print "正在服务器: "$1" 分配slots："$2
            cmd = CLIENT" -h "arr1[1]" -p "arr1[2]" cluster addslots "slots" > /dev/null";
            system(cmd);
        }

    }
    END{print "分配完成..."}' $DEFAULT_CONF

    echo '稍等几秒，正在主从配置中...'
    sleep 3;
    # set slave
    gawk 'BEGIN{CLIENT="'$CLIENT'"}
    { 
        if(NF >= 3)
        {
            split($1, arr, ":");
            CLIENT" -h "arr[1]" -p "arr[2]" cluster nodes | gawk '\''$3 ~ /.*myself.*/{ print $1 }'\''" | getline id;
            for(i = 3; i <= NF; i++)
            {
                split($i, a, ":");
                system(CLIENT" -h "a[1]" -p "a[2]" cluster replicate "id" > /dev/null");
            }
        }
    }
    END{print "主从配置完成..."}' $DEFAULT_CONF
}


function check()
{
    ip=`echo $1 | cut -d ':' -f1`;
    port=`echo $1 | cut -d ':' -f2;`
    result=`$CLIENT -h "$ip" -p "$port" CLUSTER NODES | grep myself`;
    echo $result;
}

function addNodes() 
{
    isExistConfigFile;
    num=$#;
    for(( i=2; i <= $num; i++))
    do
        shift;
        ip=`echo $1 | cut -d ':' -f1`;
        port=`echo $1 | cut -d ':' -f2`;
        lines=`wc -l $DEFAULT_CONF | cut -d ' ' -f1`
        part=$(( 32767 / $lines + 1 ));
        rand=$(( $RANDOM / $part + 1 ));
        hap=`sed -n $rand'p' $DEFAULT_CONF | cut -d ' ' -f1`;
        rip=`echo $hap | cut -d ':' -f1`;
        rport=`echo $hap | cut -d ':' -f2`;
        $CLIENT -h $rip -p $rport CLUSTER MEET $ip $port
    done
}

function addSlaves()
{
    addNodes $@;
    sleep 2;
    num=$#;
    mip=`echo ${!#} | cut -d ':' -f1`;
    mport=`echo ${!#} | cut -d ':' -f2`;
    id=`$CLIENT -h $mip -p $mport CLUSTER NODES | grep myself | gawk '{print $1}'`;
    for (( i = 2; i < $num; i++ ))
    do
        shift;
        sip=`echo $1 | cut -d ':' -f1`;
        sport=`echo $1 | cut -d ':' -f2`;
        $CLIENT -h $sip -p $sport CLUSTER REPLICATE $id;
    done

}

function addSlots() 
{
    ip=`echo $3 | cut -d ':' -f1`;
    port=`echo $3 | cut -d ':' -f2`;
    slots="";
    num=$#;
    if [ $2 == '-r' ]
    then
        for (( i = $4; i <= $5; i++ ))
        do
            slots=$slots" "$i;
        done
    elif [ $2 == '-s' ]
    then
        for (( i = 4; i <= $num; i++ ))
        do
            shift;
            slots=$slots" "$3;
        done
    else
        echo "Usage $0 $1 {-r|-s}";
        exit 1;
    fi
    if [ -n "$slots" ]
    then 
        $CLIENT -h $ip -p $port CLUSTER ADDSLOTS $slots;
    fi
}

function delSlots()
{
    ip=`echo $3 | cut -d ':' -f1`;
    port=`echo $3 | cut -d ':' -f2`;
    slots="";
    num=$#;
    if [ $2 == '-r' ]
    then
        for (( i = $4; i <= $5; i++ ))
        do
            slots=$slots" "$i;
        done
    elif [ $2 == '-s' ]
    then
        for (( i = 4; i <= $num; i++ ))
        do
            shift;
            slots=$slots" "$3;
        done
    else
        echo "Usage $0 $1 {-r|-s}";
        exit 1;
    fi
    if [ -n "$slots" ]
    then 
        $CLIENT -h $ip -p $port CLUSTER DELSLOTS $slots;
    fi
    
}


case $1 in
"create")
    create;;
"check")
    check $2;;
"addNodes")
    addNodes $@;;
"addSlaves")
    addSlaves $@;;
"addSlots")
    addSlots $@;;
"delSlots")
    delSlots $@;;
*)
echo "Usage $0 {create|check|addNodes|addSlaves|addSlots|delSlots}";;
esac

exit 0;
