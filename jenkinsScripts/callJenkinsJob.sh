#!/bin/bash
#
#####################################
# AUTHOR : Sayfeddine AWAYNIA
# DATE : 13/10/2021 
# EMAIL : s.e.awaynia@gmail.com
#####################################
#
#
# THIS SCRIPT USED TO BUILD JENKINS JOBS VIA POST REQUEST
# YOU CAN SCHEDULE THIS SCRIPT WITH AN SYSTEM CRON

export userName=$1
export userPassword=$2
export groupeName=$3
export moduleName=$4
export projectVersion=$4

# Call Jenkins Job

curl -u userName:userPassword -X POST "$JENKINS_URL/job/$JOB_NAME/buildWithParameters?GROUPE_NAME=$groupeName&MODULE_NAME=$moduleName&VERSION=$projectVersion"
