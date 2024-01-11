job("Build and run tests") {
    container(displayName = "Run mvn install", image = "maven:3.3-jdk-17") {
        shellScript {
            content = """
	            mvn clean install
            """
        }
    }
}