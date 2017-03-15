FROM tomcat:8-jre8
MAINTAINER Hongxin Zhang <hongxin@cbio.mskcc.org>, Ersin Ciftci <ersin@jimmy.harvard.edu>, Ino de Bruijn <debruiji@mskcc.org>
LABEL Description="OncoKB, a precision oncology knowledge base"
ENV ONCOKB_HOME="/oncokb"
COPY . $ONCOKB_HOME
RUN apt-get update && apt-get install -y --no-install-recommends \
    && apt-get install -y --no-install-recommends -t jessie-backports maven openjdk-8-jdk && \
    cd $ONCOKB_HOME && mvn -Pbackend -DskipTests clean install && \
    cp $ONCOKB_HOME/web/target/oncokb.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
ENTRYPOINT ["catalina.sh", "run"]
