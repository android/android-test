# start from docker image with bazel installed
FROM gcr.io/cloud-builders/bazel

ENV ANDROID_HOME /android-sdk
ENV PATH ${ANDROID_HOME}/tools:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools/bin:/bin:$PATH

RUN \
    # install extra utilities needed
    apt-get update && \
    apt-get -y install maven \
    unzip \
    zip \
    wget && \

    # download and extract sdk
    wget https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip && \
    unzip sdk-tools-linux-4333796.zip -d $ANDROID_HOME && \
    yes | sdkmanager --install 'build-tools;28.0.3' 'platforms;android-28'
