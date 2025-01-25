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
        stage('Call Docker Image Pipeline') {
            steps {
                script{
                    build job: 'docker_image_build', // Name of the downstream pipeline (Pipeline B)
                        parameters: [
                            string(name: 'github_repo', value: "https://github.com/cherpin00/compass-scraping"),
                            string(name: 'branch', value: "development"),
                            string(name: 'image_name', value: "realestate-app"),
                            string(name: 'tag', value: "test"),
                            string(name: 'docker_credentials', value: ""),
                            string(name: 'github_credentials', value: ""),
                            string(name: 'dockerfile_path', value: "frontend/Dockerfile"),
                            string(name: 'docker_repo', value: "docker.io/cherpin"),
                        ]
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                kubectl set image deployment/react-app \
                    react-app=cherpin/$IMAGE_NAME:$TAG -n realestate-app-dev
                kubectl rollout status deployment/react-app -n realestate-app-dev
                '''
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
