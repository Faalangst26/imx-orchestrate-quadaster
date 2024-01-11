job("Build and run tests") {
    container(displayName = "Run mvn install", image = "openjdk:17-jdk-slim") {
        shellScript {
            content = """
	            mvn clean install
            """
        }
    }
}