# RoboRally
This document outlines the implemented features in this project and provides instructions on:
- Setting up the project
- Playing the game locally
- Setting up online play

## Features

### Field Actions / Spaces
- Checkpoints (Fully Functional)
- Conveyor Belts (Fully Functional)
- Pit (Visual Display Only)
- Start Gears

### Deck and Cards
- Interactive Programming Cards (Fully Functional)

### Saving and Loading with Serialization
- Reading from and writing to JSON files
- Serialization of Boards to JSON
- Deserialization of JSON to Boards
- Saving Board State to files (Fully Included)
- Loading Boards from files (Fully Included)

### Online Multiplayer
- Built on RESTful Architecture
- GET and PUT GameState to and from Server
- Supports external server hosting (IP configuration in code)
- Ability to host multiple games
- Display list of active games
- Joining games functionality
- Separate thread HTTP Requests to Server

### Boards
- Board1
- Board2

## Setting Up the Project
Simply extract the zip file and open the resulting folder as an IntelliJ Project.

## How to Play (Locally)

### Running the Game Application
Locate the Game Application at: `Game/src/main/java/dk/dtu/compute/se/pisd/roborally/StartRoboRally.java`
Run the main method.

### Starting a Game
After launching the application, click on File.
Choose "New Game" to start a new game or "Load Game" to load a saved game.
"New Game" lets you choose the number of players and a default board to start a new game.
"Load Game" allows you to resume a saved game.

### Saving a Game
During a game, click on File in the top menu-bar and select "Save Game".
Follow the onscreen instructions to name your save file.
The game is saved and can now be loaded when starting a game.

### Exiting a Game
During a game, under File, click on "Stop Game" to stop the current game.

### Exiting the Application
Click the X in the corner or go to File -> Exit to exit the application.

## Online Play Instructions

### Launching a Server
Run the HttpApplication found at: `HTTP/src/main/java/dtu/compute.http/HttpApplication`.
This application runs in the background on the server machine.
By default, applications running on the same machine can connect via the localhost. You can change the IP address in the source code to allow computers on the same subnet to connect.

### Hosting an Online Game
Launch the RoboRally application.
On the top menu bar, go to "Joint-play" and select "Launch game".
Configure the number of players and boards as you wish.

### Joining an Online Game
Launch a separate RoboRally application.
Go to "Multiplayer" on the top menu bar and click "Connect with server".
Select the game you want to join from the list and click "Join".
The game starts when all players have joined.

### Running Multiple Applications Simultaneously in IntelliJ
Open the application configurations window (Edit configurations).
In the "Build and run" menu, select "Modify options".
Enable "Allow Multiple Instances".
Apply and Exit.
