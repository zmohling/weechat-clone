# WeeChat Clone
This repository hosts a clone of the popular IRC client WeeChat. The client and server are written in Java, and are executed in X-based terminals. Unlike WeeChat, this software does not use the internet chat relay protocol. Client and server communication utilizes a persistent web socket. 

## Requirements
Support for *nix based systems. Only tested in a Linux environment with X Window System terminals and bash. 

##### Dependencies
- Java
- Ant w/ Ivy

## Execution
To run the client, simple execute the `client` file located in the root project directory. Configure the IP and port address of the server before executing the runnable jar file `/bin/jar/server.jar`.

## Screenshots
<img src="https://i.imgur.com/cKy7nQs.png">
<img src="https://i.imgur.com/GlYgPY3.png">
