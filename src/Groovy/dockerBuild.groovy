@Library('homelab_jenkins@main') _ 

pipeline{
    agent {
        kubernetes {
            yamlFile 'src/Operations/Kubernetes/dind.yaml'
            defaultContainer 'shell'
        }
    }
    environment {
        IMAGE_NAME = 'realestate-app'
        BRANCH = 'development'
        TAG = "${env.BRANCH}-${env.BUILD_NUMBER}"
        // KUBECONFIG = credentials('kubeconfig-credential-id') // Store kubeconfig in Jenkins credentials
    }
    stages{
        stage('Checkout') {
            steps {
                git branch: env.BRANCH, 
                    url: 'https://github.com/cherpin00/compass-scraping',
                    credentialsId: '4da91a3b-816d-48c0-8aa0-ce7e11e13243'
            }
        }
        stage('Docker Build and Push') {
            steps {
                script {
                    // Log in to Docker registry using credentials stored in Jenkins
                    withCredentials([usernamePassword(credentialsId: 'docker-credentials-id', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin"
                    }
                    
                    // Build the Docker image
                    def image = docker.build("docker.io/cherpin/$IMAGE_NAME:$TAG", "frontend/")
                    
                    // Push the image to the Docker registry
                    image.push()
                }
            }
        }
        // stage('Docker Push') {
        //     steps {
        //         sh 'docker push docker.io/cherpin/$IMAGE_NAME:$TAG'
        //     }
        // }
        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                kubectl set image deployment/react-app \
                    your-container-name=$IMAGE_NAME:$TAG -n realestate-app-$BRANCH
                kubectl rollout status deployment/react-app -n realestate-app-$BRANCH
                '''
            }
        }
    }
    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}