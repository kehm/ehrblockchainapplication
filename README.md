# HLF Network Application
## Java Application for interacting with the HLF Network Package

Related project: <https://github.com/kehm/ehrblockchain>

This software package is related to the following article and thesis:
- (1) Yang et al., "A blockchain-based architecture for securing electronic health record systems", 2019, Concurrencry and Computation: Practice and Experience, <https://onlinelibrary.wiley.com/doi/10.1002/cpe.5479>
- (2) K. E. Marstein, "Improve auditing and privacy of electronic health records by using blockchain technology", 2019, Master's thesis, <http://bora.uib.no/handle/1956/20519>

Main method is located in /src/main/java/com/example/hlfnetworkapplication/Main.java.

To successfully interact with the HLF Network, the following configuration steps must be followed before starting the application:

1. Copy trust store certs.jks generated by the generate.sh script in the HLF Network package to /src/main/resources. 
   The file must be updated each time the cryptographic material for the HLF Network is re-created.
2. Edit affiliations.txt located in /src/main/resources. To enroll a user with the CA, the selected affiliation must be listed in this file.
   Affiliations must be listed together with their corresponding msp, name, url and registrar. See the sample affiliations.txt file supplied with the package as an example.
3. Add IP address - hostname mappings to the system hosts file. In Linux distributions, the file is located in /etc/hosts. Ignore this step if DNS is used.

When the application is running, enroll as a user to start interacting with the network.
The enrollment dialog is displayed on startup and can also be initiated later from the File menu.

The project is available under the Apache License, Version 2.0 (Apache-2.0).
