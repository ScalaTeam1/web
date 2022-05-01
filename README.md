# Before developing

1. run sbt package to build the another project [link](https://github.com/ScalaTeam1/flight-prices-prediction-xgboost)
2. add lib to this project's classpath
3. install play! plugin in IDEA
4. add environment variable: http.port=... (in order to differ running port from minio)
5. make sure there is a available model in minio or local file(defined in the default configuration file)
6. run

# Files

```text
./
├── README.md
├── app
│   ├── Module.scala
│   ├── actors
│   ├── config
│   ├── controllers
│   ├── filters
│   ├── models
│   ├── services
│   └── utils
├── build.sbt
├── conf
│   ├── application.conf
│   ├── input.csv
│   ├── logback.xml
│   └── routes
├── project
│   ├── build.properties
│   ├── plugins.sbt
│   ├── project
│   └── target
```
