CS6349 term project: secure delivery protocol of files over the network using SHA-256 only.

Dakota Fisher, Gil Popilski


Build the project:
-Run 'make' in the directory.

Test the project:
-Make a directory 'test-files' in the directory.
-Populate the directory with select files.
-Start the server with the command 'java -cp proj.jar Server'
-Run the script './full-test.sh'

Client commands:

-key: Obtain the public RSA key of the server.
-init: Initialize session, create session key and send to the server, encrypted with its public RSA key. Should only be done after key.
-list: Print the list of files available at the server.
-upld <filename>: Upload a file to the server. Encrypted automatically. The session should be initialized first.
-dwnl <filename>: Download a file from the server. Encrypted automatically. The session should be initialized first.
-chal <filename>: Integritiy check of <filename> from the server, handled separately from the file delivery. Based on the whole file. Can be compared with the file the user obtains.
-chalpart <filename>: The same as chal, but is based on a random part of the specified file.
-stat <filename>: Receive statistics about the file: its length and hash.