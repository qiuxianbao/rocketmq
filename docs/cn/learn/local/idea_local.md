
#源码本地启动 
从distribution中找到conf目录，复制 <br/>
broker.conf, logback_broker.xml, logback_namesrv.xml <br/>
到自定义目录C:\Idea\a-learn\rocketmq\local（作为RocketMQ的Home目录）

## NamesrvStartup
NamesrvStartup#main
* 定义环境变量，ROCKETMQ_HOME=C:\Idea\a-learn\rocketmq\local

## BrokerStartup
BrokerStartup#main
* 配置broker.conf
````
添加以下内容：

# 此处得用双斜线，否则会报“文件名、目录名或卷标语法不正确。”错误
storePathRootDir=C:\\Idea\\a-learn\\rocketmq\\local\\store
storePathCommitLog=C:\\Idea\\a-learn\\rocketmq\\local\\store\\commitlog

namesrvAddr=127.0.0.1:9876
brokerIP1=192.168.3.10
brokerIP2=192.168.3.10
autoCreateTopicEnable=true
````

* 定义环境变量，ROCKETMQ_HOME=C:\Idea\a-learn\rocketmq\local
* 指定启动参数，-c C:\Idea\a-learn\rocketmq\local\conf\broker.conf
