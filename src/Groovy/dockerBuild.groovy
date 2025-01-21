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
        string: 'image_name',
        defaultValue: params.image_name ? : '',
        description: ''
    }
    parameters {
        string: 'branch',
        defaultValue: params.branch ? : '',
        description: ''
    }
    parameters {
        string: 'tag',
        defaultValue: params.tag ? : '',
        description: ''
    }
    stages{
        stage('Checkout') {
            steps {
                git branch: params.branch, 
                    url: 'https://github.com/cherpin00/compass-scraping',
                    credentialsId: '4da91a3b-816d-48c0-8aa0-ce7e11e13243'
            }
        }
        stage('Docker Build and Push') {
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
                sh "docker push docker.io/cherpin/${image_names}:${tag}"
            }
        }
    }
}