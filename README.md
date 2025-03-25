# NEW USER PLEASE READ
If you're planning to use the mod, and you didn't use it before, you may not be able to do so, if the Spotify app hits the 30 daily user limit, the mod may crash when trying to load data from your account, for news about this you can go [see this issue on GitHub](https://github.com/LeonimusTTV/SpotiCraft/issues/2) \
\
A workaround for this is to make your own Spotify app and build the mod yourself, you'll need to update it every new release and build it again, see the [How to build the mod](#how-to-build-the-mod)

# SpotiCraft - A new way of listening to music

These are the **official** links to download the mod on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/spoticraft-2) and [Modrinth](https://modrinth.com/mod/spoticraft-2) \
Another mod also called SpotiCraft exist, it's not maintained anymore, but you can always go check it -> https://github.com/IMB11/Spoticraft/tree/master (fabric only)

## Table of Content
[Description](#description)\
[TODO List](#todo-list)\
[FAQ](#faq)\
[Version List](#version-list)\
[API](#api)\
[How to build the mod](#how-to-build-the-mod)\
[Changelog](#changelog)

## Description
SpotiCraft allow you to connect into your Spotify account and play your favorite music, playlist, artist or even search for new song all in Minecraft :3 (Spotify **Premium** is **required** to use this mod!)

## TODO List
- Login - Finished
- Play music (play, pause, next, previous, shuffle, repeat, volume) - Finished
- Search for music, artist or playlist - Finished
- Artist page (show music of artist, playable music) - Finished
- Album and Playlist - Finished
- Home page (recommendation and featured category can't be used now due to spotify removing api endpoint)  - Finished
- Go back, forward and home button - Finished
- Like music and unlike it - Finished
- Add and remove from playlist - Finished
- UI in general - Finished
- Improvement - WIP

## FAQ

### Minecraft keep crashing after updating the mod

If that happen to you, before making an issue, try to delete the "spoticraft" folder in the Minecraft folder (where the mods folder is)\
If Minecraft keep crashing, try to only have this mod in your mods' folder\
If Minecraft still crash, make an issue and don't forget your log file

## Version List
For now, I'll support Forge + NeoForge from 1.21.4 (and upcoming versions if not a lot of the code need to be changed) to 1.19.
I'm also testng Fabric support, please create an issue.

| Minecraft version | Mod version                 | Mod Loader                | Link                                                                                                                                   |
|-------------------|-----------------------------|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| 1.21.4            | 0.0.1-release               | Forge + NeoForge + Fabric | [1.21.4](https://github.com/LeonimusTTV/SpotiCraft/tree/master) [Fabric](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21.4-fabric) |
| 1.21.3            | 0.0.1-release               | Forge + NeoForge + Fabric | [1.21.3](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21.3) [Fabric](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21.3-fabric) |
| 1.21.2            | 0.0.1-release               | NeoForge + Fabric         | [1.21.2](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21.2) [Fabric](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21.2-fabric) |
| 1.21.1            | 0.0.1-release               | Forge + NeoForge + Fabric | [1.21.1](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21.1) [Fabric](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21.1-fabric) |
| 1.21              | 0.0.1-release               | Forge + NeoForge + Fabric | [1.21](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21) [Fabric](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21-fabric)       |
| 1.20.6            | 0.0.6-beta                  | Forge + NeoForge          | [1.20.6](https://github.com/LeonimusTTV/SpotiCraft/tree/1.20.6) |
| 1.20.5            | 0.0.6-beta                  | NeoForge                  | [1.20.5](https://github.com/LeonimusTTV/SpotiCraft/tree/1.20.5) |
| 1.20.4            | 0.0.6-beta                  | Forge + NeoForge          | [1.20.4](https://github.com/LeonimusTTV/SpotiCraft/tree/1.20.4) |
| 1.20.3            | 0.0.6-beta                  | Forge + NeoForge          | [1.20.3](https://github.com/LeonimusTTV/SpotiCraft/tree/1.20.3) |
| 1.20.2            | 0.0.6-beta                  | Forge + NeoForge          | [1.20.2](https://github.com/LeonimusTTV/SpotiCraft/tree/1.20.2) |
| 1.20.1            | 0.0.6-beta                  | Forge                     | [1.20.1](https://github.com/LeonimusTTV/SpotiCraft/tree/1.20.1) |
| 1.20              | 0.0.6-beta                  | Forge                     | [1.20](https://github.com/LeonimusTTV/SpotiCraft/tree/1.20)     |
| 1.19.4 to 1.19    | -                           | Forge                     | -                                                               |
| before 1.19       | Not planned to be supported | -                         | -                                                               |

## API
### The API will no longer be used after 0.0.5-beta due to Spotify new security policies
[SpotifyAuthHandler.java](https://github.com/LeonimusTTV/SpotiCraft/blob/master/src/main/java/com/leonimust/spoticraft/server/SpotifyAuthHandler.java#L31) use an API to get the access_token and refresh it, if you want to use yours you can get the [repo here](https://github.com/LeonimusTTV/SpotiCraft-API) if you want an example.

## How to build the mod
This guide uses [git](https://git-scm.com/downloads), [IntelliJ from JetBrains](https://www.jetbrains.com/idea/) and the [JAVA JDK from Adoptium](https://adoptium.net/temurin/releases/)

### Installation Part
1. Download the repo using the "Code" button (keep in mind the folder where you're download spoticraft)\
**IMPORTANT NOTE**:\
If you wanna use a specific version, please choose the right branch for that :\
![image](https://github.com/user-attachments/assets/04678ada-9157-4282-aead-c1fb14e8d69e)\
**FABRIC MOD ARE ONLY IN minecraftversion-fabric**\
Please also refere to the [Version List](#version-list) to know what branch is on what version

3. Install [JAVA 21](https://adoptium.net/temurin/releases/) JDK or [JAVA 17](https://adoptium.net/temurin/releases/?version=17) JDK for minecraft 1.20.x (don't forget to check the "Set or override JAVA_HOME variable")\
![image](https://github.com/user-attachments/assets/89a67534-6528-4234-9ca4-fc2cef225d4d)

4. Install [IntelliJ](https://www.jetbrains.com/idea/)

5. Open IntelliJ and open spoticraft's folder

6. Select the right JAVA SDK in the project settings\
![image](https://github.com/user-attachments/assets/5051befd-70e9-442a-8557-c832593287b0)\
![image](https://github.com/user-attachments/assets/c66ef941-a28d-4843-a2e5-f9cd2b1678e7)

7. Now select again the right JAVA SDK in the settings\
![image](https://github.com/user-attachments/assets/1a531a9c-68c0-406b-bbf6-b9fdd975d39c)\
![image](https://github.com/user-attachments/assets/9a776c3a-0d8a-4317-824a-3afce18656c5)\
Do **NOT** forget to check the "Download external annotations for dependencies", it might help for some errors  

8. Wait for gradle to finish the installation (lil hammer with a green dot)\
![image](https://github.com/user-attachments/assets/44418508-aef7-40d7-9fb2-a802314927bb)

9. If you see "BUILD SUCCESSFUL", everything should be good
![image](https://github.com/user-attachments/assets/873ce4de-65e3-464f-ab71-d4a82a3984be)

10. If you don't have a BUILD SUCCESSFUL you might have done something wrong, check again this guide and if it still doesn't work, make an issue

### Spotify Part
1. Go to this url https://developer.spotify.com/ and log yourself

2. Open the dashboard\
![image](https://github.com/user-attachments/assets/01589896-f3b0-43ac-8ab1-e3e856f2d0f2)

3. Click the "Create app" button

4. Enter the name of the app, a description

5. You **MUST** add this EXACT url in your spotify app **http://127.0.0.1:12589/callback** (don't forget to press the "add" button)

6. Check WEB API

7. Check the terms
![image](https://github.com/user-attachments/assets/99547639-1e53-4b4a-a30f-41a90b65cf9f)

9. Create the app

10. Now go to your application and click "Settings"

11. Copy the client ID\
![image](https://github.com/user-attachments/assets/704ca60f-2698-4575-9830-7eed75ba432c)

12. Now open the file called SpotifyAuthHandler.java\
**Forge Path**: src/main/java/com/leonimust/spoticraft/forge/server/SpotifyAuthHandler.java\
**NeoForge Path**: src/main/java/com/leonimust/spoticraft/neoforge/server/SpotifyAuthHandler.java\
**Fabric Path**: src/main/java/com/leonimust/server/SpotifyAuthHandler.java\
![image](https://github.com/user-attachments/assets/dd13cb2f-bc78-450a-9b97-857226913a30)

14. Then on the line where it says "CLIENT_ID" replace it by your client ID\
![image](https://github.com/user-attachments/assets/a98ff868-a17a-46b4-b321-cd75005dac30)

### Build Time
1. Now time to build
**Forge**: for Forge, open the gradle menu at the right of your screen, go into jarJar and double click on "jarJar"\
![image](https://github.com/user-attachments/assets/a8948dc9-8790-4ff0-a705-95d7621a601b)

**NeoForge**: for NeoForge you'll need to enter a command to build the mod (if you're using a version that only has NeoForge like 1.21.2, do the same as Forge)\

**Windows**:
```bash
.\gradlew --build-file=build-neoforge.gradle jarJar
```

**Linux/MacOs**: 
```bash
./gradlew --build-file=build-neoforge.gradle jarJar
```

**Fabric**: for Fabric, open the gradle menu at the right of your screen, go into build and double click on "build"\
![image](https://github.com/user-attachments/assets/3e61e2bb-a5af-4a01-a90f-1473415f8ddd)

2. Now open the "build" folder then "libs"\

**Forge**: the only file you see (the one that ends with -all.jar) is the mod, put it into your mods folder\

**NeoForge**: you'll have two files, one with "-all.jar" at the end and one without, use the one that has "-all.jar", put it into your mods folder\

**Fabric**: you'll also have two files, one that ends with "-sources.jar" and one without, take the one that doens't end with "-sources.jar", take it and put it into your mods folder, don't forget the Fabric API !\
![image](https://github.com/user-attachments/assets/2be30c2d-fda7-4d3f-92ba-36812867880d)


## Changelog
Release 0.0.1
- Fixed refresh token not being updated after being used to refresh the access token
- Fixed resize text in the ui, now using text width and not text length
- Added repeat one and repeat enable icons to the ui
- Added shuffle enable icons to the ui

Beta 0.0.7
- Fixed Like Button position
- Fixed bugs

Beta 0.0.6
- Updated auth system because of Spotify new security policies
- Other fixes

Beta 0.0.5
- Added Turk and French
- Fixed pixel bleeding on 1.20.1
- Fixed crash when searching for an artist, playlist or album without an image

Beta 0.0.4
- Fixed crash when image isn't downloaded successfully
- Fixed crash when user doesn't have an active device
- Fixed UI not refreshing correctly when closing

Beta 0.0.3
- Added NeoForge support for 1.21.4
- Fixed crash when searching (doesn't occur all the time, but sometimes it does)

Beta 0.0.2
- Fixed crash when searching from the last version
- Added home button
- Added a home page (somewhat)
- Added go forward button
- Added play button in playlist
- Removed the text of Play button in albums and playlist, same for Liked Songs
- Fixed scroll not being reset when content change
- Made some general improvement

Beta 0.0.1 - Removed due to crash when searching
- Made ui smaller
- Added Search with artist, songs, playlist and albums
- Added Artist page with top songs and albums
- Added Playlist page with playable music
- Added Albums, same as playlist
- Added music name and artist(s)
- Added Liked songs, same as playlist and albums
- Added back button
- Added a play button in albums (will be reworked on future version)

Alpha 0.0.2
- Fixed crashes
- Bump forge version to 1.21.4-54.0.16

Alpha 0.0.1 - Removed due to issues
- User can log in with their Spotify account
- Base version of the UI to play music, pause music, go to the next music, go to the previous music, shuffle, repeat, change volume of the music and show the Image of the music currently playing
