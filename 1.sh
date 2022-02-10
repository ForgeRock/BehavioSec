#!/bin/bash

mvn package -Dmaven.test.skip=true
rm /usr/local/opt/tomcat@9/libexec/webapps/openam/WEB-INF/lib/uber-behaviosecNode-*.jar
cp target/uber-behaviosecNode-*.jar /usr/local/opt/tomcat@9/libexec/webapps/openam/WEB-INF/lib

cp target/uber-behaviosecNode-*.jar /Users/marcofanti/Downloads/ContinuousAccessEvaluation
ls -ltr /usr/local/opt/tomcat@9/libexec/webapps/openam/WEB-INF/lib/uber*


#pushd   /usr/local/opt/tomcat@9/libexec
/usr/local/opt/tomcat@9/libexec/bin/catalina.sh stop

#popd

scp -i "~/.ssh/tomcat-forgerock.pem"  \
/Volumes/ExternalOwc/BehavioSec/Development/forgerock/BehavioSec7.0/behaviosecNode/target/uber-behaviosecNode-*.jar \
ubuntu@ec2-18-117-228-4.us-east-2.compute.amazonaws.com:/opt/tomcat/apache-tomcat-9.0.43/webapps/openam/WEB-INF/lib/
