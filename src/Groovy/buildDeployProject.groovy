@Library('homelab_jenkins@main') _ 

// def github_repo = "https://github.com/cherpin00/compass-scraping"
// def branch = "development"
// def image_name = "realestate-app-dev"
// def tag = "test"
// def dockerfile_path = "frontend/Dockerfile"
// def docker_repo = "docker.io/cherpin"
// def project_name = "react-app"
// def app_name = "realestate-app-dev"

def docker_credentials = ""
def github_credentials = ""
def github_repo = ""
def branch = ""
def image_name = ""
def tag = ""
def dockerfile_path = ""
def docker_repo = ""
def project_name = ""
def app_name = ""

pipeline {
    agent {
        kubernetes {
            label 'build-and-deploy'
            defaultContainer 'build-and-deploy'
        }
    }
    stages {
        stage('Load Config') {
            steps {
                script {
                    print(env.JOB_NAME)
                    // Load the configuration dynamically based on the pipeline name
                    def config = projectConfigs(env.JOB_NAME.split('_')[0])
                    print("config in build ${config}")

                    // Use the configuration in your pipeline
                    github_repo = config.github_repo
                    branch = config.branch
                    image_name = config.image_name
                    tag = config.tag
                    dockerfile_path = config.dockerfile_path
                    docker_repo = config.docker_repo
                    project_name = config.project_name
                    app_name = config.app_name

                    //Docker information
                    echo "Building Docker image ${image_name}:${tag} from ${github_repo} on branch ${branch}"
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
                    ${project_name}=cherpin/${image_name}:${tag} -n ${app_name}
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
