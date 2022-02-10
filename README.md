# Izanami

HTTP Live Streaming (HLS) Server Application

## Prerequisites

- Linux PC (Ubuntu 20.04)
- NVIDIA GPU (GeForce 10 series) and its driver
- FFmpeg 4.4
- Java 17
- [Izanagi](https://github.com/hirooka/izanagi) (If you want to play stream of MPEG2-Ts tuner)

## Getting Started

The easiest way to run application is to use Docker with `nvidia/cuda`. In this case you need install Docker on Ubuntu 20.04 ([Install Docker Engine on Ubuntu](https://docs.docker.com/engine/install/ubuntu/)).

### Build application Docker image

```
git clone https://github.com/hirooka/izanami
cd izanami
./gradlew build
docker build . -t $USER/izanami:1.0.0-SNAPSHOT
```

### Run Application (with MongoDB and Docker network)

```
docker network create nihon
docker pull mongo:bionic
docker run \
  --name izanami-mongo \
  --net nihon \
  -d \
  mongo:bionic
docker build . -t $USER/izanami:1.0.0-SNAPSHOT
docker run \
  --name izanami \
  --net nihon \
  --privileged \
  --volume /dev/:/dev/ \
  --volume /opt/izanami/video:/opt/izanami/video \
  --volume /etc/localtime:/etc/localtime:ro \
  -p 8080:8080 \
  -d \
  -it $USER/izanami:1.0.0-SNAPSHOT
```

You can start playback via

```
echo '{
  "streamingType": "TUNER",
  "playlistType": "LIVE",
  "transcodingSettings": "HD_HIGH",
  "canEncrypt": false,
  "channelRemoteControl": 1,
  "fileName": "",
  "videoResolution": null,
  "videoBitrate": "0",
  "audioBitrate": 0,
  "username": "izanami"
}' | curl -X POST --data-binary @- \
-H 'Content-Type: application/json' \
-H 'User-Agent: Chrome/0' \
http://izanami:izanami@localhost:8080/api/v1/izanami/start
```

Then you can get m3u8 playlist URI as response

```
{
  "uri" : "/stream/11d17784-7c28-49a7-9427-99c10f34a8ff/0/1280x720-4500-160/live/izanamih264.m3u8"
}
```

You can stop playback via

```
curl http://izanami:izanami@localhost:8080/api/v1/izanami/stop
```
