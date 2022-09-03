FROM quay.io/wildfly/wildfly:26.1.1.Final
#ADD docker-wildfly/postgresql-42.4.2.jar /opt/jboss/wildfly/standalone/deployments/
ADD server/target/server-2.0.war /opt/jboss/wildfly/standalone/deployments/
ADD docker-wildfly/chat.xml /opt/jboss/wildfly/standalone/configuration/
RUN /opt/jboss/wildfly/bin/add-user.sh -a -u 'chat' -p 'chat'

#RUN sed 's/jboss.bind.address:127.0.0.1/jboss.bind.address:wildfly/g' -i /opt/jboss/wildfly/standalone/configuration/chat.xml

# Wystawienie wszystkiego na zewnątrz do celów debugowania i usera do konsoli administracyjnej (admin/admin)
# URL http://localhost:9990/console
#RUN sed 's/jboss.bind.address.management:127.0.0.1/jboss.bind.address.management:wildfly/g' -i /opt/jboss/wildfly/standalone/configuration/chat.xml
#RUN sed 's/jboss.bind.address.unsecure:127.0.0.1/jboss.bind.address.unsecure:wildfly/g' -i /opt/jboss/wildfly/standalone/configuration/chat.xml
RUN /opt/jboss/wildfly/bin/add-user.sh -e -u 'admin' -p 'admin'

CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","wildfly","-c","chat.xml"]
