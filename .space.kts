job("Build and run tests") {
    container(displayName = "Run mvn install", image = "maven:3.9.6-eclipse-temurin-17-focal") {
        shellScript {
            content = """
	            mvn clean install
            """
        }
    }
}