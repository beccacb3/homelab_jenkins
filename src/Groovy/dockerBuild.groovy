@Library('homelab_jenkins@main') _ 

def credentials = ''

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
            name: 'credentials',
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
        stage('Check known docker credentials'){
            steps{
                script{
                    credentials = params.credentials
                    if(params.docker_repo.contains("docker.io/cherpin")){
                        credentials = "a453e044-6a68-4edb-a82e-b26ffe9054af"
                    }
                }
            }
        }
        stage('Checkout') {
            steps {
                git branch: params.branch, 
                    url: params.github_repo,
                    credentialsId: params.credentials
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    sh "ls"
                    def tag = "${params.branch}-${env.BUILD_NUMBER}"
                    withCredentials([usernamePassword(credentialsId: ${credentials}, usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
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