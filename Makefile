
release:
	mvn clean deploy -P release -DskipTests  -Dgpg.keyname=xx