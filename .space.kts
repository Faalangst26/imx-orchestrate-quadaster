job("Build and run tests") {
    container(displayName = "Run mvn install", image = "maven:latest") {
        shellScript {
            content = """
	            mvn -B package --file pom.xml
            """
        }
    }
}