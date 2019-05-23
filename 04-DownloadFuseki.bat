powershell -command "Start-BitsTransfer -Source http://ftp.fau.de/apache/jena/binaries/apache-jena-fuseki-3.11.0.zip -Destination apache-jena-fuseki-3.11.0.zip"
powershell -command "Expand-Archive apache-jena-fuseki-3.11.0.zip ."
PAUSE
