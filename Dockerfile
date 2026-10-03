# Entorno de compilación Android dentro de Docker
FROM eclipse-temurin:17-jdk-jammy

ENV ANDROID_SDK_ROOT=/opt/android-sdk \
    ANDROID_HOME=/opt/android-sdk \
    GRADLE_VERSION=8.7 \
    CMDLINE_TOOLS_VERSION=11076708
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:/opt/gradle/gradle-${GRADLE_VERSION}/bin

RUN apt-get update && apt-get install -y --no-install-recommends unzip wget \
    && rm -rf /var/lib/apt/lists/*

# Android SDK command-line tools
RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip -O /tmp/tools.zip \
    && unzip -q /tmp/tools.zip -d $ANDROID_SDK_ROOT/cmdline-tools \
    && mv $ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest \
    && rm /tmp/tools.zip

RUN yes | sdkmanager --licenses > /dev/null \
    && sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Gradle
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt/gradle \
    && rm /tmp/gradle.zip

WORKDIR /app
COPY . .

# Compila el APK y lo copia a /output (montado como volumen)
CMD ["sh", "-c", "gradle assembleDebug --no-daemon && mkdir -p /output && cp app/build/outputs/apk/debug/app-debug.apk /output/HojaDeVida.apk && echo 'APK generado en ./output/HojaDeVida.apk'"]
