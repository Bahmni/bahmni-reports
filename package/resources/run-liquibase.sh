#!/bin/sh
set -e -x



CHANGE_LOG_TABLE="-Dliquibase.databaseChangeLogTableName=liquibasechangelog -Dliquibase.databaseChangeLogLockTableName=liquibasechangeloglock -DschemaName=$3"
DRIVER="com.mysql.cj.jdbc.Driver"

find_one_jar() {
    pattern="$1"
    label="$2"
    match=""
    count=0
    old_ifs="$IFS"
    IFS=
    for candidate in $pattern; do
        if [ -e "$candidate" ]; then
            count=$((count + 1))
            match="$candidate"
        fi
    done
    IFS="$old_ifs"
    if [ "$count" -ne 1 ]; then
        echo "expected exactly one $label jar matching $pattern, found $count"
        exit 1
    fi
    echo "$match"
}

LIQUIBASE_JAR=$(find_one_jar "${WAR_DIRECTORY}/WEB-INF/lib/liquibase-core-*.jar" "liquibase")
MYSQL_CONNECTER_JAR=$(find_one_jar "${WAR_DIRECTORY}/WEB-INF/lib/mysql-connector-java-*.jar" "mysql connector")

# liquibase-core 4.32.0, unlike 4.8.0, does not shade its runtime dependencies (commons-io,
# commons-lang3, snakeyaml, opencsv, ...), so the rest of WEB-INF/lib is added via Java's own
# trailing-/* classpath wildcard rather than naming each transitive jar individually -- naming
# them one by one silently breaks again on the next dependency bump.
LIQUIBASE_LIB_DIR="${WAR_DIRECTORY}/WEB-INF/lib"
[ -d "$LIQUIBASE_LIB_DIR" ] || { echo "lib directory not found: $LIQUIBASE_LIB_DIR"; exit 1; }

CLASSPATH="$LIQUIBASE_JAR:$MYSQL_CONNECTER_JAR:${LIQUIBASE_LIB_DIR}/*"

CHANGE_LOG_FILE="$1"

(cd ${WAR_DIRECTORY}/WEB-INF/classes/ && java $CHANGE_LOG_TABLE -cp $CLASSPATH liquibase.integration.commandline.Main --driver=$DRIVER --url=jdbc:mysql://$2:3306/$3 --username=$4 --password=$5 --changeLogFile=$CHANGE_LOG_FILE update)
