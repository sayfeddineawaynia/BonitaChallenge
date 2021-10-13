# Bonita Challenge

# Section 1 : deploy Bonita Image on K8s

## In this Section we will see how to deploy Bonita Image on K8s

K8s Cluster was provisionning with Kind

Credentials to connect Bonita App
user : rootUser
pwd : bonita

# Section 2 : Jenkins Scripts

getAllRunningJobs.sh : Script to list all running Jenkins Jobs

stopRunningJobs.sh : Script to stop all Jenking job running more than 1 hour

callJenkinsJob.sh : Script to build ajenkins job via post request on a given hour configured in crontab

vars directory contains jenkins job script groovy loaded via shared library in jenkins
