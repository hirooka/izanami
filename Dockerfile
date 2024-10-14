FROM nvidia/cuda:12.6.2-devel-ubuntu22.04

MAINTAINER dev

ENV NVIDIA_VISIBLE_DEVICES all
ENV NVIDIA_DRIVER_CAPABILITIES all

# Package
ENV DEBIAN_FRONTEND noninteractive
RUN sed -i 's@archive.ubuntu.com@www.ftp.ne.jp/Linux/packages/ubuntu/archive@g' /etc/apt/sources.list
RUN apt-get update && \
  apt-get install -y --no-install-recommends tzdata
ENV TZ=Asia/Tokyo

RUN apt-get install -y --no-install-recommends \
  git curl wget build-essential \
  # locale
  locales \
  # FFmpeg
  pkg-config nasm yasm \
  # Java
  openjdk-17-jre-headless && \
  apt-get -y clean && \
  rm -rf /var/lib/apt/lists/*

# Web camera (audio)
RUN mkdir /etc/modprobe.d
RUN touch /etc/modprobe.d/alsa-base.conf

# FFmpeg
RUN cd /tmp && \
    git clone https://git.videolan.org/git/ffmpeg/nv-codec-headers && \
    cd nv-codec-headers && \
    make -j$(nproc) && \
    make install

RUN cd /tmp && \
    wget https://ffmpeg.org/releases/ffmpeg-5.0.1.tar.gz && \
    tar zxvf ffmpeg-5.0.1.tar.gz && \
    cd ffmpeg-5.0.1 && \
    ./configure --enable-nonfree --enable-cuda-nvcc --enable-libnpp --extra-cflags=-I/usr/local/cuda/include --extra-ldflags=-L/usr/local/cuda/lib64 --nvccflags="-gencode arch=compute_52,code=sm_52 -O2" && \
    make -j$(nproc) && \
    make install

# clean
RUN rm -rf /tmp/*

# Java
ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64

# izanami
RUN mkdir -p /opt/izanami/video
RUN cd /tmp
COPY . .
RUN ./gradlew build
RUN mv ./build/libs/izanami-1.0.0-SNAPSHOT.jar /izanami.jar

# clean
RUN rm -rf /tmp/*

# locale
RUN locale-gen ja_JP.UTF-8
ENV LANG ja_JP.UTF-8
ENV LANGUAGE ja_JP:ja
ENV LC_ALL ja_JP.UTF-8

# run app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=izanami-docker", "-Dserver.port=8080", "-jar", "/izanami.jar"]
#ENTRYPOINT ["sh", "-c", "java ... -jar /app.jar"]
