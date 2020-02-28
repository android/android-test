# start from docker image with bazel installed
FROM l.gcr.io/google/bazel:0.29.1

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
    wget -nv https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip && \
    unzip -q sdk-tools-linux-4333796.zip -d $ANDROID_HOME && \
    yes | sdkmanager --install 'build-tools;29.0.3' 'platforms;android-29' | grep -v = || true
