### 介绍
alpine、fastdfs、nginx、Java环境
### 使用步骤
##### 1、将fastdfs_package、Dockfile放在同一目录
执行docker bulid -t imageName .
##### 2、vim /etc/fdfs/storage.conf
将tracker_server=192.168.172.20:22122更改为tracker_server=你的ip:22122
##### 3、vim /etc/fdfs/mod_fastdfs.conf
将tracker_server=192.168.172.20:22122更改为tracker_server=你的ip:22122
##### 4、vim /usr/local/nginx/conf/nginx.conf
在该文件中作出如下修改:<br />
<!-- ![avatar](https://images2017.cnblogs.com/blog/1107037/201801/1107037-20180106211140565-2088673686.png)<br /> -->
<!-- ![avatar](https://images2017.cnblogs.com/blog/1107037/201712/1107037-20171227211338675-1973886058.png)<br /> -->
其中server_name指定本机ip，location /group1//M00/：group1为nginx 服务FastDFS的分组名称，<br />
M00是FastDFS自动生成编号，对应store_path0=/home/fdfs_storage，如果FastDFS定义store_path1，<br />
这里就是M01
##### 启动tracker_server
/usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf restart
##### 启动storage
/usr/bin/fdfs_storaged /etc/fdfs/storage.conf restart
##### 启动nginx
/usr/local/nginx/sbin/nginx


## 搭建FastDFS分布式文件系统
### 1、单tracker，多storage
##### 创建tracker和storage并存的服务器
###### 创建镜像
将fastdfs_package、Dockfile放在同一目录，执行docker build -t imageName .
###### 创建容器
mkdir /home/tra_sto<br />
docker run -t -i -v /home/tra_sto:/home/fdfs_storage --name ContainerName ImageId /bin/bash<br />

vi /etc/fdfs/storage.conf<br />
将tracker_server=192.168.172.20:22122更改为tracker_server=你的ip:22122<br />

vi /etc/fdfs/mod_fastdfs.conf<br />
将tracker_server=192.168.172.20:22122更改为tracker_server=你的ip:22122<br />

vi /usr/local/nginx/conf/nginx.conf<br />
在该文件中作出如下修改:<br />
 ![avatar](https://images2017.cnblogs.com/blog/1107037/201801/1107037-20180106211140565-2088673686.png)<br />
 ![avatar](https://images2017.cnblogs.com/blog/1107037/201712/1107037-20171227211338675-1973886058.png)<br />
其中server_name指定本机ip，location /group1//M00/：group1为nginx 服务FastDFS的分组名称，<br />
M00是FastDFS自动生成编号，对应store_path0=/home/fdfs_storage，如果FastDFS定义store_path1，<br />
这里就是M01<br />
##### 启动tracker_server
/usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf restart
##### 启动storage
/usr/bin/fdfs_storaged /etc/fdfs/storage.conf restart
##### 启动nginx
/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/nginx.conf

##### 创建单storage
###### 创建镜像
将fastdfs_package、Dockfile放在同一目录，执行docker build -t imageName .
##### 创建容器
mkdir /home/storage<br />
docker run -t -i -v /home/storage:/home/fdfs_storage --name ContainerName ImageId /bin/bash<br />

vi /etc/fdfs/storage.conf<br />
将tracker_server=192.168.172.20:22122更改为tracker_server=tracker的ip:22122<br />
将group_name=group1改为group_name=group2<br />

vi /etc/fdfs/mod_fastdfs.conf<br />
将tracker_server=192.168.172.20:22122更改为tracker_server=tracker的ip:22122<br />
将group_name=group1改为group_name=group2<br />

vi /usr/local/nginx/conf/nginx.conf<br />
在该文件中作出如下修改,将图中的group1改为group2:<br />
<!-- ![avatar](https://images2017.cnblogs.com/blog/1107037/201801/1107037-20180106211140565-2088673686.png)<br /> -->
<!-- ![avatar](https://images2017.cnblogs.com/blog/1107037/201712/1107037-20171227211338675-1973886058.png)<br /> -->
启动步骤如"tracker和storage并存的服务器"。


#### NAT模式、路由模式、桥接模式
NAT就是在局域网内部网络中使用内部地址，而当内部节点要与外部网络进行通讯时，就在网关处，将内部地址替换成公用地址，从而在
外部网络上正常使用，NAT可以使多台计算机共享Internet连接
NAT有三种类型，静态NAT、动态地址NAT、网络地址端口转换NAPT
NAT模式相当于虚拟机的网关是宿主机，通过dhcp来获取IP，而宿主机的网关是连接是连接外网的路由器，也就是说，虚拟机是通过宿主机来上网的
桥接模式，虚拟机和宿主机的网关是路由器，虚拟机和宿主机是一个网段iP，通过路由器来上网


