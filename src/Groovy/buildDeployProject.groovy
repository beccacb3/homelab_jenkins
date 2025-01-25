def github_repo = "https://github.com/cherpin00/compass-scraping"
def branch = "development"
def image_name = "realestate-app"
def tag = "test"
def docker_credentials = ""
def github_credentials = ""
def dockerfile_path = "frontend/Dockerfile"
def docker_repo = "docker.io/cherpin"
def project_name = "react-app"
def app_name = "realestate-app-dev"

pipeline {
    agent {
        kubernetes {
            label 'build-and-deploy'
            defaultContainer 'build-and-deploy'
        }
    }
    environment {
        IMAGE_NAME = 'realestate-app'
        BRANCH = 'development'
        TAG = "${env.BRANCH}-${env.BUILD_NUMBER}"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: env.BRANCH, 
                    url: 'https://github.com/cherpin00/compass-scraping',
                    credentialsId: '4da91a3b-816d-48c0-8aa0-ce7e11e13243'
            }
        }
        // stage('Docker Login') {
        //     steps {
        //         withCredentials([usernamePassword(credentialsId: 'a453e044-6a68-4edb-a82e-b26ffe9054af', 
        //                                           usernameVariable: 'DOCKER_USERNAME', 
        //                                           passwordVariable: 'DOCKER_PASSWORD')]) {
        //             sh 'podman login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD docker.io'
        //         }
        //     }
        // }
        // stage('Docker Build') {
        //     steps {
        //         sh 'cd frontend; podman build . -t docker.io/cherpin/$IMAGE_NAME:$TAG'
        //     }
        // }
        // stage('Docker Push') {
        //     steps {
        //         sh 'podman push docker.io/cherpin/$IMAGE_NAME:$TAG'
        //     }
        // }
        stage('Configure Project Parameters') {
            steps {
                script {
                    github_repo 
                }
            }
        }
        stage('Call Docker Build/Upload Pipeline') {
            steps {
                script{
                    build job: 'docker_image_build', // Name of the downstream pipeline (Pipeline B)
                        parameters: [
                            string(name: 'github_repo', value: github_repo),
                            string(name: 'branch', value: branch),
                            string(name: 'image_name', value: image_name),
                            string(name: 'tag', value: tag),
                            string(name: 'docker_credentials', value: ""),
                            string(name: 'github_credentials', value: ""),
                            string(name: 'dockerfile_path', value: dockerfile_path),
                            string(name: 'docker_repo', value: docker_repo),
                        ]
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
		        sh """
                    kubectl set image deployment/${project_name} \
                    ${app_name}=${docker_repo}/${image_name}:${tag} -n ${app_name}
                    kubectl rollout status deployment/${project_name} -n ${app_name}
                """
                }
            }
        }
    }
    // post {
    //     success {
    //         echo 'Deployment successful!'
    //         mail to: 'caleb.herpin@gmail.com,beccacb3@gmail.com,david@herpin.net',
    //              subject: "Jenkins Pipeline: Success - ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
    //              body: "The Jenkins pipeline for ${env.JOB_NAME} build #${env.BUILD_NUMBER} succeeded.\n\nCheck it here: ${env.BUILD_URL}"
    //     }
    //     failure {
    //         echo 'Deployment failed!'
    //         mail to: 'caleb.herpin@gmail.com,beccacb3@gmail.com,david@herpin.net',
    //              subject: "Jenkins Pipeline: Failure - ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
    //              body: "The Jenkins pipeline for ${env.JOB_NAME} build #${env.BUILD_NUMBER} failed.\n\nCheck it here: ${env.BUILD_URL}"
    //     }
    // }
}
