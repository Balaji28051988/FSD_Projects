node {
    
    stage('initalize') {
        def dockerHome = tool 'docker'
        env.PATH="${dockerHome}:${env.PATH}"
    }
    
    stage 'checkout'
     git 'https://github.com/RaghuDevaraj/springBootAssignment.git'
    
    stage 'unit testing and packaging'
        sh 'mvn clean package'
    
    stage('Sonar Analysis'){
        withSonarQubeEnv('jenkinssonarqube'){
          sh "mvn sonar:sonar"
        }
    }
    
    stage ('archival') {
        step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/*.xml'])
        archiveArtifacts 'target/springBoot-0.0.1-SNAPSHOT.war'
    }
    
    stage('docker build') {
        sh "docker -H=172.18.2.50:2375 image build -t 326452/library-management ."
        sh "docker -H=172.18.2.50:2375 login -u 326452 -p docker"
        sh "docker -H=172.18.2.50:2375 push 326452/library-management"
    }
    
    stage('deploy the war') {
        sh "docker -H=172.18.2.50:2375 container run --publish 8060:8080 -d 326452/library-management"
    }
    
    stage ("mail notificaton") {
        emailext (
            from:"Jenkins",
            to:"raghu.devaraj@cognizant.com",
       subject: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
       body: """<p>SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
         <p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>""",
       recipientProviders: [[$class: 'RequesterRecipientProvider']],
       attachLog: true
     )
    }
}
