@Library('homelab_jenkins@main') _ 

pipeline{
    agent {
        kubernetes {
            yamlFile 'src/Operations/Kubernetes/dind.yaml'
            defaultContainer: 'shell'
        }
    }
    stages{
        stage('Test'){
            steps{
                script{
                    //TODO plugin github
                    print("test")
                }
            }
        }
    }
}