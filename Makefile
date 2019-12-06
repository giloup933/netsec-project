%.class: %.java
	javac $< # -o $@

all: last.jar

last/Crypto.class: last/Counter.class
last/Client.class: last/Crypto.class last/Counter.class
last/ServerThread.class: last/Crypto.class
last/Server.class: last/Crypto.class last/ServerThread.class

last.jar: last/Client.class last/Server.class
	jar cvf last.jar last

clean:
	rm last/*.class last.jar
