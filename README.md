# SpotiCraft - A new way of listening to music

This is the **official** repo for this mod -> https://www.curseforge.com/minecraft/mc-mods/spoticraft-2 \
Another also called SpotiCraft exist, it's not maintained anymore, but you can always go check it -> https://github.com/IMB11/Spoticraft/tree/master (fabric only)

## Table of Content
[Description](#description)\
[TODO List](#todo-list)\
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
- Like music and unlike it - TODO
- Add and remove from playlist - TODO
- UI in general -WIP
- Improvement - WIP

## Version List
Once the mod is in a *finish* state in 1.21.4, it'll be ported from 1.21.4 to 1.19, version before 1.19 will not be supported, at least for now.
| Minecraft version | Mod version | Mod Loader | Link |
|---|---|---|---|
| 1.21.4 | 0.0.3-beta | Forge | [Main](https://github.com/LeonimusTTV/SpotiCraft/tree/master) |
| 1.21.4 | 0.0.3-beta | NeoForge | [NeoForge](https://github.com/LeonimusTTV/SpotiCraft/tree/1.21.4-neoforge) |
| 1.21.3 to 1.19 | - | Forge + NeoForge (1.20.2) | - |
| before 1.19 | Not planned to be supported | - |  - |

## API
[SpotifyAuthHandler.java](https://github.com/LeonimusTTV/SpotiCraft/blob/master/src/main/java/com/leonimust/spoticraft/server/SpotifyAuthHandler.java#L31) use an API to get the access_token and refresh it, if you want to use yours you can get the [repo here](https://github.com/LeonimusTTV/SpotiCraft-API) if you want an example.

## How to build the mod
Install [JAVA 21](https://adoptium.net/temurin/releases/) JDK\
Use JAVA 21 as your JDK\
Build and enjoy :3

## Changelog
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
