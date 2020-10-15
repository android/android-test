# start from docker image with bazel installed
FROM l.gcr.io/google/bazel:3.5.0

ENV ANDROID_HOME /android-sdk
ENV PATH ${ANDROID_HOME}/tools:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools/bin:/bin:$PATH

RUN \
    # TODO: investigate why /usr/local/lib/libc++.so not compatible with aapt
    rm -f /usr/local/lib/libc++.so && \
    # install extra utilities needed
    apt-get -q update && \
    apt-get -q -y install maven \
    unzip \
    zip \
    wget && \

    # download and extract sdk while suppressing the progress bar output
    wget -nv https://dl.google.com/android/repository/commandlinetools-linux-6609375_latest.zip && \
    unzip -q commandlinetools-linux-6609375_latest.zip -d $ANDROID_HOME && \
    yes | sdkmanager --install 'build-tools;30.0.2' 'platforms;android-30' --sdk_root=$ANDROID_HOME | grep -v = || true
