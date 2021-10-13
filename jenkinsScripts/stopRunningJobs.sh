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
# AND ABORTED THOSE WHO RUNNING FOR MORE THEN 1 HOUR



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

# Stop Jobs than running for more 1 Hour
stopJobs()
{
    jobNumber=$(cat $1 | wc -l)
    if [ $jobNumber -ne 0 ];then
        for var in `seq 1 $jobNumber`;
        do
            jobName=$(cat $1 | head -n $var | tail -1)
            buildId=$(curl -s -u $USER_NAME:$USER_TOKEN "http://$JENKINS_URL/job/$jobName/lastBuild/api/xml" | grep -oP '(?<=id>)[^<]+' | sed -e 's/[[:space:]]*$//')
            buildTimestamp=$(curl -s -u $USER_NAME:$USER_TOKEN "http://$JENKINS_URL/job/$jobName/$buildId/api/json?tree=timestamp" | awk -v FS="\"timestamp\":" 'NF>1{print $2}' | cut -d "}" -f 1)
            buildTimestampTmp=$(($buildTimestamp / 1000))
            buildStartedTime=$(date -d @$buildTimestampTmp +'%H%M%S')
            scriptRunningTime=$(date +'%H%M%S')
            buildRunningTime=$(($scriptRunningTime - $buildStartedTime))
            if [ $buildRunningTime -gt 10000 ];then
                echo "THE BUILD $buildId OF JOB $jobName WILL BE ABORTED BECAUSE IT RUNNING FOR MORE THEN 1 HOUR"
                curl -s -u $USER_NAME:$USER_TOKEN -X POST "http://$JENKINS_URL/job/$jobName/$buildId/stop"
            else
                echo -e "ALL BUILD ARE IN THE ACCEPTED RANGE OF BUILDING TIME.\nPLEASE RE RUN THE SCRIPT AFTER A FEW MOMENT !"
            fi
        done
    fi
}

#############
# Main Script
#############

echo "*** BEGIN PROCESS OF LISTING ALL RUNNING JENKINS JOBS AND STOP THOSES WHO RUNNING FOR MORE THEN 1 HOUR ***"

saveRunningJobsInXmlFile

getRunningJobsName

stopJobs ./runningJobName.txt

echo "*** END PROCESS OF LISTING ALL RUNNING JENKINS JOBS AND STOP THOSES WHO RUNNING FOR MORE THEN 1 HOUR ***"

# Delete useless file
rm -f runningJob.xml

exit 0
