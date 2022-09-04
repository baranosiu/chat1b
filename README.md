# Czat1b

Wymagana Java 11.

## Instalacja w Dockerze
1. Kompilacja `mvn package`
2. Uruchomienie serwera `docker-compose up -d --build`
3. Uruchomienie klienta `java -jar client/target/client-2.0-jar-with-dependencies.jar [host:port]` (domyślny 127.0.0.1:8080)

## Instalacja "ręczna" pod Wildfly

1. Kompilacja: `mvn package`
2. Ustawienie rozwiązywania nazwy `postgresql` na hosta, na którym jest zainstalowany PostgreSQL (na przykład dodanie wpisu pod Linuxem do `/etc/hosts` lub pod Windowsem do `C:\Windows\System32\drivers\etc\hosts`)
3. Uruchomienie PostgreSQL (baza o nazwie `chat` dostępna dla użytkownika user/password `admin/admin`), można wykorzystać komponent Dockera z projektu uruchamiając poleceniem `docker-compose up -d postgresql`.
4. Utworzenie pod Wildfly użytkownika `chat/chat` na przykład poleceniem `add-user -a -u chat -p chat`
5. Skopiowanie pliku `docker-wildfly/chat.xml` do `WILDFLY_HOME/standalone/configurations`
6. Skopiowanie pliku `server/target/server-2.0.war` do `WILDFLY_HOME/standalone/deployments`
7. Uruchomienie Wildfly przez `WILDFLY_HOME/bin/standalone.sh -c chat.xml` (`standalone.bat` dla Windowsa)
8. Uruchomienie klienta `java -jar client/target/client-2.0-jar-with-dependencies.jar [host:port]` (domyślny 127.0.0.1:8080)

## Instalacja sieciowa w Dockerze
Jeśli serwer i klient są na różnych maszynach a serwer jest uruchaiany w Dockerze, to trzeba dokonać kilku zmian konfiguracyjnych,
gdyż obrazy Dockera są domyślnie ukryte za NAT a JMS do prawidłowego działania potrzebuje znać swoje zewnętrzne IP i być do niego zbindowany.

Należy w tym celu wykonać następujące zmiany:
1. W `docker-wildfly/Dockerfile` należy w ostatnim wierszu nazwę hosta `wildfly` (argument opcji -b) zamienić na adres IP interfejsu sieciowego komputera, na którym serwer będzie widoczny z zewnątrz.
2. W `docker-compose.yml` dla obydwu serwisów dodać `network_mode: "host"` (odkomentować)
3. W pliku `persistence.xml` zmienić adres serwera z `postgresql` na `127.0.0.1` (zakomentować wiersz 10 i odkomentować 11)
4. Ponownie skompilować projekt przez `mvn clean package` a następnie ponownie zbudować obrazy dla dockera przez `docker-compose build`

Gotowa przykładowa konfiguracja (dla adresu 192.168.1.128) odłożona w branchu `konfiguracja-sieciowa`.

Jako że w takiej konfiguracji kontenery korzystają z natywnego interfejsu sieciowego komputera, to nie działa mapowanie portów ustawione w dccker-compose.yml, a więc odpowiednie porty (8080, 9990 i 5432) nie mogą być zajęte przez inne usługi (i port 8080 otwarty na firewallu).

## Do przedyskutowania z Łukaszem na spotkaniu 11. września
### JPA i klasy `@Entity`
W chwili obecnej klasy `@Entity` dodane ręcznie do `persistence.xml` i działa, ale automatycznie wykrywać ich nie chce.

### Jak korzystać z JPA i zdefiniowanego w Wildfly datasource
Utorzone w Wildfly "datasource" nie chce się automatycznie wstrzykiwać czy to poprzez `@PersistenceContext` czy `@PersistenceUnit`. Zdecydowałem się na "ręczne" tworzenie `EntityManagerFactory`, bo do tego serwerowego nie potrafiłem się dobrać.

### Aplikacja klienta i warningi ActivMQ
Obecnie w `log4j.properties` ustawione logowanie tylko dla ERROR, więc tego nie widać, ale przy pełnym logowaniu sypie co jakiś czas warningiem o wielu sesjach (choć wszystko działa).

### JMS i "twarde" zerwanie połączenia przez klienta
W jaki sposób wykrywać, że klient JMS się "twardo" odłączył od Topic (czyli na przykład zatrzymał aplikację przez Ctrl+C lub utracił połączenie z internetem, bez wysyłania jakiegokolwiek komunikatu). Można to wykrywać implementując puszczanie co jakiś czas "pinga", ale pewnie jest na to lepszy sposób.
