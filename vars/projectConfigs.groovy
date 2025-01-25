def call(String pipelineName) {
    def config = [:]
    
    // Define your configurations (these could be in JSON or YAML files in your repo)
    def pipelineConfigs = [
        "realestate": [
            github_repo: "https://github.com/cherpin00/compass-scraping",
            image_name: "realestate-app",
            docker_credentials: "a453e044-6a68-4edb-a82e-b26ffe9054af",
            github_credentials: "4da91a3b-816d-48c0-8aa0-ce7e11e13243",
            dockerfile_path: "frontend/Dockerfile",
            docker_repo: "docker.io/cherpin",
            project_name: "react-app",
            app_name: "realestate-app"
        ]
    ]
    
    // Load the configuration based on the pipeline name
    if (pipelineConfigs.containsKey(pipelineName)) {
        config = pipelineConfigs[pipelineName]
        print("Found config ${config}")
    } else {
        error "Pipeline name '${pipelineName}' is not defined in configuration."
    }
    
    return config
}
