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

1. 应用启动 读取配置文件中的model路径 model不存在则从s3中下载

2. online predict 读取文件 or s3路径 下载 提交预测 更新至mongodb中

3. streaming 读取文件 or s3路径 下载 提交预测 更新至mongodb中
