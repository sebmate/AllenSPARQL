powershell -command "Start-BitsTransfer -Source https://artfiles.org/apache.org/jena/binaries/apache-jena-fuseki-3.17.0.zip -Destination apache-jena-fuseki-3.17.0.zip"
powershell -command "Expand-Archive apache-jena-fuseki-3.17.0.zip ."
PAUSE
