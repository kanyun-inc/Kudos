set -e
SCRIPT_DIR=$(cd $(dirname $0) && pwd -P)
PROJECT_DIR=$SCRIPT_DIR/../../

cd $PROJECT_DIR
pwd

# MAVEN
cd $PROJECT_DIR/kudos-sample/kudos-maven-sample
mvn test -Dkudos.version=0.0.0-SNAPSHOT
cd -

# GRADLE
cd $PROJECT_DIR/kudos-sample/kudos-gradle-sample
./gradlew test -PKUDOS_VERSION=0.0.0-SNAPSHOT
cd -

cd -
