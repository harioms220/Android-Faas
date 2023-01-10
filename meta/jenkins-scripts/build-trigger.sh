echo "Building FaaS library"
echo "Android sdk is at ${ANDROID_SDK_ROOT}"

#delete old build
rm -f app/build/outputs/apk/qa/*.apk

#increase version code by 1
sudo /var/lib/jenkins/scripts/increse_version.sh /var/lib/jenkins/rizzleFaasKeys/beta/version.properties
sudo cp /var/lib/jenkins/rizzleFaasKeys/beta/version.properties feed-demo/
sudo chown jenkins:jenkins feed-demo/version.properties

#build the apk
./gradlew clean
./gradlew assembleQa


