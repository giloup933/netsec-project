%.class: %.java
	javac $< # -o $@

all: proj.jar client-files server-files
# make && java -cp proj.jar Server
# (echo key; echo init; echo dwnl README.md; echo quit) | java -cp proj.jar Client

client-files:
	mkdir client-files
server-files:
	mkdir server-files

Counter.class: Utility.class
Crypto.class: Counter.class
Client.class: Crypto.class Counter.class
ServerThread.class: Crypto.class
Server.class: Crypto.class ServerThread.class

proj.jar: Client.class Server.class
	jar cvf proj.jar *.class

clean:
	rm *.class proj.jar
