﻿echo 关闭pcm-core系统开始...
kill -9 `cat /opt/app/pcm/pcm-core/main.pid`
echo 关闭pcm-core系统成功...
echo 拷贝编译jar包到应用目录 ...
rm -f /opt/app/pcm/pcm-core/*.jar
rm -f /opt/app/pcm/pcm-core/lib/*.jar
cp -f /root/.jenkins/jobs/pcm-core/workspace/target/pcm-core-0.0.3-SNAPSHOT.jar /opt/app/pcm/pcm-core/
cp -f /root/.jenkins/jobs/pcm-core/workspace/target/lib/*.jar /opt/app/pcm/pcm-core/lib/
echo 拷贝完成....
echo 开始运行服务...
#后台启动pcm-core进程
BUILD_ID= java -jar /opt/app/pcm/pcm-core/pcm-core-0.0.3-SNAPSHOT.jar  start 8081 8071 >/opt/app/pcm/pcm-core/pcm-core.log 2>&1 &
echo $! > /opt/app/pcm/pcm-core/main.pid
echo 启动完毕.....
