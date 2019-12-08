%.class: %.java
	javac $< # -o $@

all: proj.jar

Crypto.class: Counter.class
Client.class: Crypto.class Counter.class
ServerThread.class: Crypto.class
Server.class: Crypto.class ServerThread.class

proj.jar: Client.class Server.class
	jar cvf proj.jar *.class

clean:
	rm *.class proj.jar
