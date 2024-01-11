job("Build and run tests") {
    container(displayName = "Run mvn install", image = "maven:latest") {
          steps {
            script {
                name = "Set up JDK 17"
                scriptContent = "java { version = \"17\" }"
            }

            script {
                name = "Build with Maven"
                scriptContent = "mvn -B package --file pom.xml"
            }
    }
}