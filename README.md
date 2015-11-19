# Bahmni Reports

Hosts the reports web application for the [Bahmni project](http://www.bahmni.org/)

# Installing the application

1. Clone or download this repository.

2. Run `mvn clean install -DskipDump -DskipTests` to build it

4. Run `scripts/create_configuration.sh` file to create the necessary configuration to run the reports application. Modify the configuration files to suit your needs.

3. Deploy the application found under target/bahmnireports.war

# For developers

1. Setup proper properties in the configuration files under $HOME/.bahmni-reports directory. Modify bahmni-reports-test.properties to suit the needs for testing.

2. Run `mvn clean install` to build your changes.
