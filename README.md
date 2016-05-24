# Bahmni Reports

Hosts the reports web application for the [Bahmni project](http://www.bahmni.org/)

# Installing the application

1. Clone or download this repository.

2. Run `mvn clean install -DskipDump -DskipTests` to build it

3. Deploy the application found under target/bahmnireports.war

# For developers

1. Run `scripts/create_configuration.sh` script to create necessary config files to run the integration tests. Modify the config file at $HOME/.bahmni-reports/bahmni-reports-test.properties to suit your needs.

2. Run `mvn clean install` to build your changes.
