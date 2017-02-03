#!/bin/bash
$(dirname $0)/ant -Dbasedir=`pwd`   -f $(dirname $0)/../../pceant/pce.xml $1 $2 $3
