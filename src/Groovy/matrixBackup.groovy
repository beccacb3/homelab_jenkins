@Library('homelab_jenkins@main') _ 

pipeline {
    agent {
        kubernetes {
            label 'build-and-deploy'
            defaultContainer 'build-and-deploy'
        }
    }
    parameters {
        string(
            name: 'project_name',
            defaultValue: params.project_name ?: '',
            description: 'Name of the application you want to back up.'
        )
    }
    stages {
        stage('Backup') {
            steps {
                script {
                	sh """
                		volumeMount=\$(kubectl --namespace matrix get deployment matrix -o json | jq -r '.spec.template.spec.volumes[] | select(has("persistentVolumeClaim")).name') 
                		podName=\$(kubectl --namespace matrix get pods | grep matrix | awk '{print \$1}')
                		dataPath=\$(kubectl --namespace matrix get deployment matrix -o json | jq -r ".spec.template.spec.containers[].volumeMounts[] | select(.name == \"${volumeMount}\").mountPath")
                		kubectl --namespace matrix cp ${podName}:${dataPath} data.bak
                	"""
                }
            }
        }
    }
    post {
        success {
            echo 'Backup successful!'
            mail to: 'caleb.herpin@gmail.com,beccacb3@gmail.com',
                 subject: "Jenkins Pipeline: Success - ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
                 body: "The Jenkins pipeline for ${env.JOB_NAME} build #${env.BUILD_NUMBER} succeeded.\n\nCheck it here: ${env.BUILD_URL}"
        }
        failure {
            echo 'Backup failed!'
            mail to: 'caleb.herpin@gmail.com,beccacb3@gmail.com',
                 subject: "Jenkins Pipeline: Failure - ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
                 body: "The Jenkins pipeline for ${env.JOB_NAME} build #${env.BUILD_NUMBER} failed.\n\nCheck it here: ${env.BUILD_URL}"
        }
    }
}
