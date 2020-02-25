# start from docker image with bazel installed
FROM l.gcr.io/google/bazel:0.29.1

ENV ANDROID_HOME /android-sdk
ENV PATH ${ANDROID_HOME}/tools:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools/bin:/bin:$PATH

RUN \
    # install extra utilities needed
    apt-get -q update && \
    apt-get -q -y install maven \
    unzip \
    zip \
    wget && \

    # download and extract sdk
    wget -nv https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip && \
    unzip -q sdk-tools-linux-4333796.zip -d $ANDROID_HOME && \
    yes | sdkmanager --install 'build-tools;28.0.3' 'platforms;android-28' \
    # silence sdkmanager progress bars (consisting of '=' characters)
    grep -v = || true
