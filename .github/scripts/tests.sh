set -e
SCRIPT_DIR=$(cd $(dirname $0) && pwd -P)
PROJECT_DIR=$SCRIPT_DIR/../../

cd $PROJECT_DIR
pwd

# DEFAULT CONFIGURATIONS
echo "[Kudos] Testing with default configurations"
./gradlew :kudos-compiler:test -PKOTLIN_COMPILER=K1

# Gson
gson_versions=('2.4' '2.5' '2.6' '2.7' '2.8.0' '2.9.0' '2.10')
for gson_version in ${gson_versions[@]}
do
  echo "[Kudos] Testing with Gson=$gson_version"
  ./gradlew :kudos-compiler:test -PGSON_VERSION=$gson_version -PVARIANT=gson -PKOTLIN_COMPILER=K2
done

# Jackson
jackson_versions=('2.12.0' '2.13.0' '2.14.0' '2.15.0')
for jackson_version in ${jackson_versions[@]}
do
  echo "[Kudos] Testing with Jackson=$jackson_version"
  ./gradlew :kudos-compiler:test -PJACKSON_VERSION=$jackson_version -PVARIANT=jackson -PKOTLIN_COMPILER=K2
done

# Android JsonReader
echo "[Kudos] Testing with Android JsonReader"
./gradlew :kudos-compiler:test -PVARIANT=jsonReader -PKOTLIN_COMPILER=K2

cd -
