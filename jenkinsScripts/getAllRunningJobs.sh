#!/bin/bash
#
#####################################
# AUTHOR : Sayfeddine AWAYNIA
# DATE : 13/10/2021 
# EMAIL : s.e.awaynia@gmail.com
#####################################
#
#
# THIS SCRIPT USED TO GET ALL RUNNING JENKINS JOBS


# set ENV VARS

export JENKINS_URL=$1
export USER_NAME=$2
export USER_TOKEN=$3


######################
# Function declaration
######################

# get all Jenkins running Jobs and save it in XML file
saveRunningJobsInXmlFile()
{
    curl -s -u $USER_NAME:$USER_TOKEN -g "http://$JENKINS_URL/api/xml?tree=jobs[name,url,color]&xpath=/hudson/job[ends-with(color/text(),%22_anime%22)]&wrapper=jobs" > runningJob.xml
}

# Parse XML file content ti get only Jobs name
getRunningJobsName()
{
    grep -oP '(?<=name>)[^<]+' ./runningJob.xml > runningJobName.txt
}

#############
# Main Script
#############

echo "*** BEGIN PROCESS OF LISTING ALL RUNNING JENKINS JOBS ***"

saveRunningJobsInXmlFile

getRunningJobsName

echo "JOBS NAME :"

cat runningJobName.txt

echo "*** END PROCESS OF LISTING ALL RUNNING JENKINS JOBS ***"

# Delete useless file
rm -f runningJob.xml

exit 0
