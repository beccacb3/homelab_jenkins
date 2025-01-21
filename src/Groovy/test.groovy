@Library('homelab_jenkins@main') _ 

pipeline{
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Building on Kubernetes...'
                sh 'echo "Building the project!"'
            }
        }
    }
}