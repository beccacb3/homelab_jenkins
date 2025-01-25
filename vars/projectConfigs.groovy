def call(String pipelineName) {
    def config = [:]
    
    // Define your configurations (these could be in JSON or YAML files in your repo)
    def pipelineConfigs = [
        "realestate": [
            github_repo: "https://github.com/cherpin00/compass-scraping",
            branch: "development",
            image_name: "realestate-app-dev",
            tag: "test",
            docker_credentials: "",
            github_credentials: "",
            dockerfile_path: "frontend/Dockerfile",
            docker_repo: "docker.io/cherpin",
            project_name: "react-app",
            app_name: "realestate-app-dev"
        ]
    ]
    
    // Load the configuration based on the pipeline name
    if (pipelineConfigs.containsKey(pipelineName)) {
        config = pipelineConfigs[pipelineName]
    } else {
        error "Pipeline name '${pipelineName}' is not defined in configuration."
    }
    
    return config
}
