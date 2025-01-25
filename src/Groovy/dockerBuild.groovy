@Library('homelab_jenkins@main') _ 

def docker_credentials = ''
def github_credentials = ''

pipeline{
    agent {
        kubernetes {
            yamlFile 'src/Operations/Kubernetes/dind.yaml'
            defaultContainer 'shell'
        }
    }
    parameters {
        string(
            name: 'github_repo',
            defaultValue: params.github_repo ?: '',
            description: 'Github repository url hosting dockerfile'
        )
        string(
            name: 'branch',
            defaultValue: params.branch ?: '',
            description: 'Branch to build the dockerfile from'
        )
        string(
            name: 'image_name',
            defaultValue: params.image_name ?: '',
            description: 'Name of image to be created'
        )
        string(
            name: 'tag',
            defaultValue: params.tag ?: '',
            description: 'Tag for the docker image'
        )
        string(
            name: 'docker_credentials',
            defaultValue: params.credentials ?: '',
            description: 'Credentials for pushing to dockerhub'
        )
        string(
            name: 'github_credentials',
            defaultValue: params.credentials ?: '',
            description: 'Credentials for pushing to dockerhub'
        )
        string(
            name: 'dockerfile_path',
            defaultValue: params.dockerfile_path ?: '',
            description: 'Path to dockerfile in the github repository'
        )
        string(
            name: 'docker_repo',
            defaultValue: params.docker_repo ?: '',
            description: 'Repository to upload docker image to'
        )
    }
    stages{
        stage('Check known credentials'){
            steps{
                script{
                    print("Check Docker Repo Credentials")
                    credentials = params.docker_credentials
                    if(params.docker_repo.contains("docker.io/cherpin")){
                        docker_credentials = "4da91a3b-816d-48c0-8aa0-ce7e11e13243"
                    }
                    print("Docker Credentials set to: ${docker_credentials}")
                    
                    print("Check Github Repo Credentials")
                    credentials = params.docker_credentials
                    if(params.docker_repo.contains("cherpin")){
                        github_credentials = "a453e044-6a68-4edb-a82e-b26ffe9054af"
                    }
                    print("Docker Credentials set to: ${github_credentials}")
                }
            }
        }
        stage('Checkout') {
            steps {
                script {
                git branch: params.branch, 
                    url: params.github_repo,
                    credentialsId: github_credentials
                }
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    sh "ls"
                    def tag = "${params.branch}-${env.BUILD_NUMBER}"
                    withCredentials([usernamePassword(credentialsId: docker_credentials, usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin"
                    }
                    sh """
                        cd \$(dirname ${params.dockerfile_path}) && docker build -f \$(basename ${params.dockerfile_path}) . -t ${params.docker_repo}/${params.image_name}:${params.tag}
                    """
                }
            }
        }
        stage('Docker Push') {
            steps {
                sh "docker push ${params.docker_repo}/${image_name}:${params.tag}"
            }
        }
    }
}