@Library('homelab_jenkins@main') _ 

pipeline{
    agent {
        kubernetes {
            yamlFile 'src/Operations/Kubernetes/dind.yaml'
            defaultContainer 'shell'
        }
    }
    // environment {
    //     IMAGE_NAME = 'realestate-app'
    //     BRANCH = 'development'
    //     TAG = "${env.BRANCH}-${env.BUILD_NUMBER}"
    // }
    parameters {
        string(
            name: 'image_name',
            defaultValue: params.image_name ?: '',
            description: ''
        )
        string(
            name: 'branch',
            defaultValue: params.branch ?: '',
            description: ''
        )
        string(
            name: 'tag',
            defaultValue: params.tag ?: '',
            description: ''
        )
        string(
            name: 'repo_url',
            defaultValue: params.repo_url ?: '',
            description: ''
        )
        string(
            name: 'credentials',
            defaultValue: params.credentials ?: '',
            description: ''
        )
    }
    stages{
        stage('Checkout') {
            steps {
                git branch: params.branch, 
                    url: params.repo_url,
                    credentialsId: params.credentials
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    def tag = "${params.branch}-${env.BUILD_NUMBER}"
                    withCredentials([usernamePassword(credentialsId: 'a453e044-6a68-4edb-a82e-b26ffe9054af', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin"
                    }
                    sh "cd frontend; docker build . -t docker.io/cherpin/${params.image_name}:$tag"
                }
            }
        }
        stage('Docker Push') {
            steps {
                sh "docker push docker.io/cherpin/${image_name}:${tag}"
            }
        }
    }
}