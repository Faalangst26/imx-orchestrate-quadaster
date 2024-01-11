job("Build and run tests") {
    container(displayName = "Run mvn install", image = "eclipse-temurin:17-jdk-focal") {
        shellScript {
            content = """
	            mvn clean install
            """
        }
    }
}