SCRIPT_DIR=$(cd $(dirname $0) && pwd -P)
PROJECT_DIR=$SCRIPT_DIR/../../
echo $PROJECT_DIR

cd $PROJECT_DIR

getProp(){
   grep "${1}" gradle.properties | cut -d'=' -f2 | sed 's/\r//'
}
publishVersion=$(getProp VERSION_NAME)
snapshotSuffix='SNAPSHOT'

./gradlew publishAllPublicationsToMavenCentral -PGSON_VERSION=2.4 -PJACKSON_VERSION=2.12.0
if [[ "$publishVersion" != *"$snapshotSuffix"* ]]; then
  echo "auto release artifacts of ${publishVersion}"
  ./gradlew closeAndReleaseRepository
fi

cd -
