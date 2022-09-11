# Czat1b

Wymagana Java 11, WildFly 26.1.1

## Instalacja "ręczna" pod Wildfly
1. Kompilacja: `mvn package`
2. Uruchomienie PostgreSQL (baza o nazwie `chat` dostępna dla użytkownika user/password `admin/admin`), można wykorzystać komponent Dockera z projektu uruchamiając poleceniem `docker-compose up -d postgresql`.
3. Utworzenie pod Wildfly użytkownika `chat/chat` na przykład poleceniem `add-user -a -u chat -p chat`
4. Skopiowanie pliku `docker-wildfly/chat.xml` do `WILDFLY_HOME/standalone/configurations`
5. Skopiowanie plików `server/target/server-2.0.war` oraz `docker-wildfly/postgresql-42.4.2.jar` do `WILDFLY_HOME/standalone/deployments`
6. Uruchomienie Wildfly przez `WILDFLY_HOME/bin/standalone.sh -c chat.xml` (ewentualnie z dodatkiem `-b IP` jeśli ma działać poza localhost)
7. Uruchomienie klienta `java -jar client/target/client-2.0-jar-with-dependencies.jar [host:port]` (domyślny 127.0.0.1:8080)

## Instalacja serwera w Dockerze (Linux)
Wymagany docker-compose obsługujący pliki konfiguracyjne co najmniej w wersji 3.5 i Docker z implementacją network_mode:host
1. Kompilacja: `mvn package`
2. Uruchomienie: `docker-compose up -d --build`

Jako że w takiej konfiguracji kontenery korzystają z natywnego interfejsu sieciowego komputera, to nie działa mapowanie portów ustawione w dccker-compose.yml, a więc odpowiednie porty (8080, 9990 i 5432) nie mogą być zajęte przez inne usługi (i port 8080 otwarty na firewallu).
