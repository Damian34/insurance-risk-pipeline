# insurance-risk-pipeline
in progress..


## Stack
- Scala 3.8.4 (JDK 25)
- RabbitMQ
- Akka HTTP/Streams

## How to run

### Clean if found problem with compiling .sbt
```bash
taskkill /F /IM java.exe
```

### Build jars
Install SBT globally from https://www.scala-sbt.org/download.html 
or run via IntelliJ (right panel -> sbt -> assembly)
```bash
sbt assembly
```

### Startup on Docker after build
```bash
docker-compose up -d
```
