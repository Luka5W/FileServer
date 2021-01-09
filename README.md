# FileServer

_A simple fileserver written in Java_

**This project is still work in progress!**

## About

The program uses the http(s) protocol to interact with the client.

This includes:
- File access
- File metadata access
- User management

Commandline interface for administrative purposes:<br />
[Luka5W/FileServerCLI](https://github.com/Luka5W/FileServerCLI)

This project is primary made for a Note-App (Android/Web) I'm working on (See [YourNotes](https://github.com/YourNotes)).

## Documentation

- The [API](https://luka5w.github.io/FileServer/api/FileServer.postman_collection.json) is documented with [Postman](https://www.postman.com)
- [JavaDoc](https://luka5w.github.io/FileServer/javadoc/)

### Installation & Usage

**This program is tested and developed with Java 8 on a GNU/Linux system** (specifically Kubuntu 18.04 LTS). I don't know (and care) if it works properly under Windows.

1. Download the jar from the [releases](/releases/latest)
2. Execute it the first time with `java -jar FileServer_[version].jar --setup` to create the setup file.
3. Create a JKS file with this command: `keytool -genkeypair -keyalg RSA -alias selfsigned -keystore [filename].jks -storepass [password] -validity 360 -keysize 2048`.
4. Execute the server: `java -jar java -jar FileServer_[version].jar`. Add `-c [filename].ini` to specify a custom config file (Step 2 would be unnecessary for that).

To get the version, execute the program with the `-v` flag, for all arguments with the `-h` flag.