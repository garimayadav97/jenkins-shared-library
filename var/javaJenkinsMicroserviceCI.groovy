def call() {
    
node {
        stage('Initialize')
        {
            def dockerHome = tool 'dockerTool'
            //env.PATH = env.PATH + ":${dockerHome}"
        }

        try {
                stage("Checkout") {
                      git branch: 'master',
                        url: 'https://github.com/garimayadav97/sample-springboot-project.git'
           
                }
                stage("Maven Build") {
                      bat "mvn clean install"
                }
                stage("Unit Tests") {
                      //bat "mvn verify"
                }
                stage("Extract Info") {
                    bat "mvn -N help:effective-pom -Doutput=target/pom-effective.xml"
                    pom = readMavenPom(file: 'target/pom-effective.xml')
                    projectArtifactId = pom.getArtifactId()
                    projectGroupId = pom.getGroupId()
                    projectVersion = pom.getVersion()
                    projectName = pom.getName()
                }
                stage("Docker Build and Tag") {
                      app = docker.build("${projectGroupId}/${projectArtifactId}:${projectVersion}")
                }
                stage("Docker Push") {
                    echo 'Pushing to docker repository'
                    //Push to specific docker registory
                    //docker.withRegistry('https://registry.hub.docker.com', 'docker-credentials')
                    //app.push("${projectVersion}")
                }
        }
        catch (Exception e) {
          currentBuild.currentResult='FAILURE'
          echo 'Exception occurred: ' + e.toString()
          emailext body: "${currentBuild.currentResult}: Job ${env.JOB_NAME}, build ${env.BUILD_NUMBER} failed\nMore info at: ${env.BUILD_URL}",
                    recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']],
                    subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME}"
    }

}
}