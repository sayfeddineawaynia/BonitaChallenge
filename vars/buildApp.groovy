import hudson.model.*
import com.cloudbees.groovy.cps.NonCPS


def getDescription(){
    def description = '''
    <h2> BuildAndDeployClientApp </h2>
    <p>--------------------------------------------------------------------------</p> 
    <h3>Stages Definition : </h3>
    </p>
    <p><b>Stage Init Config : </b></p>
    <ul>
        <li>Clean du workspace</li>
        <li>Initiate Job Config</li>
    </ul>
    <p><b>checkParams :</b></p>
    <ul>
        <li>Check Param if they null or empty</li>
    </ul>
    <p><b>Pull Project Sources : </b></p>
    <ul>
        <li>Get Sources Projet</li>
    </ul>
    <p><b>Build App : </b></p>
    <ul>
        <li>Build App whitout running tests</li>
    </ul>
    <p><b>Run Tests : </b></p>
    <ul>
        <li>Run Application Tests</li>
    </ul>
    <p><b>Deploy and Run App : </b></p>
    <ul>
        <li>Deploy App in Docker container</li>
    </ul>
    '''
    return description
}

def call(){

    node('built-in'){

        stage('init Config'){
            println "Configuration globale du Job est commencée !"
			
			deleteDir() //Purge workspace of Jenkins Job
            def activateScheduler = (params.ACTIVATE_SCHEDULER != null && !params.ACTIVATE_SCHEDULER) ? false : true

			properties([
				parameters([
					string(name: 'GROUPE_NAME', defaultValue: '', description: 'Groupe Name'),
					string(name: 'MODULE_NAME', defaultValue: '', description: 'Module Name'),
					string(name: 'VERSION',  defaultValue: '', description: 'Module Tag Or Branch Name'),
                    booleanParam(name: 'ACTIVATE_SCHEDULER', defaultValue: activateScheduler, description: 'Param used to activate or not the scheduler for this Job')
				]),
				buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')),
                activateScheduler?
				pipelineTriggers([
                    cron('H H(0-5) * * 1-5')
                ]):pipelineTriggers([])
			])

            //Set la description du job dans l'IHM Jenkins
            def jobName = "${env.JOB_NAME}"
            Jenkins.instance.getItem(jobName.split('/')[0]).description = getDescription()
        }

        def groupName = params.GROUPE_NAME
        def moduleName = params.MODULE_NAME
        def projectVersion = params.VERSION
	def ret

        stage('checkParams'){
            if(!groupName?.trim() || !moduleName?.trim() || !projectVersion?.trim()){
                error "PLEASE VERIFY JOB PARAMS THAT ARE NOT EMPTY !"
            }
        }

        stage('Pull Project Sources'){

            // there is two way to checkout Projet sources
            // with Jenkins SCL tool Or with basic git commande

            // checkout changelog: false, poll: false, 
            // scm: [$class: 'GitSCM', 
            // branches: 
            //     [[name: "*/${projectVersion}"]], 
            //     extensions: [], 
            // userRemoteConfigs: 
            //     [[credentialsId: 'gitlub-user', 
            //     url: "https://github.com/${groupName}/${moduleName}.git"]]]

            // if project is public
            // checkout([$class: 'GitSCM', 
            // branches: 
            //     [[name: '*/master']], extensions: [], 
            // userRemoteConfigs: [[url: 'https://github.com/${groupName}/${moduleName}.git']]])

            sh"git clone -b ${projectVersion} --single-branch https://github.com/${groupName}/${moduleName}.git"

            // note : we can also variabilize from which registry we need to pull sources
            // new param where the user will chose one of given choices "gitlab", "github" ...
            // and the commande will be
            // sh"git clone -b ${projectVersion} --single-branch https://${remoteResgitry}.com/${groupName}/${moduleName}.git"


        }

        dir("$WORKSPACE/$moduleName"){
            
            withMaven(jdk: 'jdk1.8', maven: 'maven354') {

                stage('Build App'){
                    sh"mvn -B -DskipTests clean package"
                }

                stage('Run Tests'){
                    ret = sh returnStatus: true, script: "mvn test"
                    if(ret != null && ret == 0){
                        junit '**/target/surefire-reports/TEST-*.xml'
                        archiveArtifacts 'target/*.jar'
                    }
                }

                stage('Deploy and Run App'){
		    if(ret != null && ret == 0){
                    	sh """ 
			#!/bin/bash +x
                    	jarPath=\$(find ./target -type f -name "*.jar")
                    	jarName=\$(basename \$jarPath)
                    	cp ./target/\$jarName ./target/my-app.jar
                    	mv ./target/my-app.jar ./my-app.jar
			
			cat <<EOF > Dockerfile
			FROM openjdk:18-jdk-alpine3.13
			WORKDIR /
			ADD my-app.jar my-app.jar
			CMD ["java", "-jar", "my-app.jar"]
EOF
			docker build -t demoapp .
			docker run --name rundemoapp demoapp
			
			#Purge container and image docker for the next build
			docker rm rundemoapp
			docker rmi demoapp
                    	"""
			// THIS IS NOT A DEPLOY STEP, IT'S JUST AN EXECUTION OF JAR   
			// THAT'S WHY WE HAD CREATE A DOCKER FILE TO SIMULATE A DOPLOYMENT STEP
                    	//sh"java -jar my-app.jar"
			
		    } else { 
		    	println "Application will not be deployed because tests are on fail"
		    }
                }
            }
        }

    }
}
