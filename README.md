# Before developing

1. run sbt packge to build the another project
2. add lib to this project's classpath
3. install play! plugin in IDEA
4. add environment variable: http.port=...
5. run

# Files

├── README.md ├── app │ ├── Module.scala Guice Configuration Class │ ├── controllers API Controller │ ├── filters API
Filter │ ├── pojos │ ├── services │ ├── utils ├── build.sbt ├── conf │ ├── application.conf │ ├── logback.xml logback
config file │ └── routes API Routes ├── project │ ├── build.properties │ ├── plugins.sbt └── web

## TODO

## TODO

- online predict
- streaming
- mongodb curd

