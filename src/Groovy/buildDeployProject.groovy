@Library('homelab_jenkins@main') _ 

def github_repo = ""
def branch = ""
def image_name = ""
def dockerfile_path = ""
def docker_repo = ""
def project_name = ""
def app_name = ""
def github_credentials = ""
def docker_credentials = ""
def tag = ""

pipeline {
    agent {
        kubernetes {
            label 'build-and-deploy'
            defaultContainer 'build-and-deploy'
        }
    }
    parameters {
        string(
            name: 'branch',
            defaultValue: params.github_repo ?: '',
            description: 'Branch if it differs from development or master'
        )
    }
    stages {
        stage('Load Config') {
            steps {
                script {
                    print(env.JOB_NAME)
                    def split_JOB_NAME = env.JOB_NAME.split('_')
                    // Load the configuration dynamically based on the pipeline name
                    def config = projectConfigs(split_JOB_NAME[0])
                    print("config in build ${config}")

                    // Use the configuration in your pipeline
                    github_repo = config.github_repo
                    image_name = config.image_name
                    dockerfile_path = config.dockerfile_path
                    docker_repo = config.docker_repo
                    project_name = config.project_name
                    app_name = config.app_name
                    github_credentials = config.github_credentials
                    docker_credentials = config.docker_credentials

                    if(params.branch != ''){
                        branch = params.branch
                    }
                    else if(split_JOB_NAME[2] == "dev"){
                        app_name = "${app_name}-dev"
                        image_name = "${image_name}-dev"
                        branch = "development"
                    }
                    else {
                        branch = "master"
                    }

                    tag = "${env.BRANCH}-${env.BUILD_NUMBER}"

                    echo "Building Docker image ${image_name}:${tag} from ${github_repo} on branch ${branch}"
                }
            }
        }
        stage('Call Docker Build/Upload Pipeline') {
            steps {
                script{
                    def triggerResult = build job: 'docker_image_build', // Name of the downstream pipeline (Pipeline B)
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
                    
                    if (triggerResult.result == 'FAILURE') {
                        error "Downstream pipeline failed! Marking this stage as failure."
                    } else {
                        echo "Downstream pipeline succeeded!"
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
		        sh """
                kubectl set image deployment/${project_name} \
                    ${project_name}=cherpin/${image_name}:${tag} -n ${app_name}
                kubectl rollout status deployment/${project_name} -n ${app_name}
                """
                }
            }
        }
    }
    post {
        success {
            echo 'Deployment successful!'
            mail to: 'caleb.herpin@gmail.com,beccacb3@gmail.com,david@herpin.net',
                 subject: "Jenkins Pipeline: Success - ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
                 body: "The Jenkins pipeline for ${env.JOB_NAME} build #${env.BUILD_NUMBER} succeeded.\n\nCheck it here: ${env.BUILD_URL}"
        }
        failure {
            echo 'Deployment failed!'
            mail to: 'caleb.herpin@gmail.com,beccacb3@gmail.com,david@herpin.net',
                 subject: "Jenkins Pipeline: Failure - ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
                 body: "The Jenkins pipeline for ${env.JOB_NAME} build #${env.BUILD_NUMBER} failed.\n\nCheck it here: ${env.BUILD_URL}"
        }
    }
}
