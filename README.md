## 项目介绍
   本项目采用FastDFS分布式文件系统进行文件存储，使用FineUploader实现文件的分片
   上传功能，对于小于40M的文件，采用nginx实现文件的下载，对于大于40M的文件，使用
   HttpServletResponse下载到浏览器。

## 启动
- 修改FastDFS的tracker服务器
````
fastdfs.connect_timeout_in_seconds = 5
fastdfs.network_timeout_in_seconds = 30
fastdfs.charset = UTF-8
fastdfs.http_anti_steal_token = false
fastdfs.http_secret_key = edu521310
fastdfs.http_tracker_http_port = 80
fastdfs.tracker_servers = 172.17.0.2:22122

````
- 修改下载文件所在的服务器位置
````
fastdfs.uploadSmallFileUri = http://172.17.0.2/
fastdfs.uploadBigFileUri = http://172.17.0.2:8888/fastdfs/applicationToFile?id=
````

- 更改数据库的位置
````
spring.datasource.url=jdbc:mysql://210.28.188.105:3306/fastdfs?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&useSSL=false
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
````

- 更改应用服务器的文件目录
````
breakpoint.upload.dir = /home/one/
breakpoint.upload.dir.tmp = /home/one/tmp/
breakpoint.upload.dir.fin = /home/one/fin/
````

## 联系方式
QQ邮箱 1605611836@qq.com
google邮箱 sunow521310@gmail.com