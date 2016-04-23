#### Issues
### General
- Make sure to use to the latest version of Immersive Engineering (IE for short). You can download it [here](http://minecraft.curseforge.com/projects/immersive-engineering).
- Your bug report should contain answers to these questions (If adding screenshots makes answering one of these questions easier, add them):
  - What is the bug? E.g. "The game crashed with this crash report: `<Link to crashlog>`".
  - What did you do to make it happen? E.g. "I threw a revolver into a crusher".
  - Does it happen every time you do this? Does it happen if you do something else as well? E.g. "The game crashes with a similar crash whenever an item that can not be processed in the crusher is thrown into one.".
  - Did this happen on a dedicated server (multiplayer servers), in LAN multiplayer or in singleplayer? E.g. "I first noticed this on a server, but it happens in singleplayer as well".
  - What other mods were installed when the bug happened? Crashlogs always contain a modlist, so you can skip this part if you already provided one. You can generate a crash and therefore a mod list by pressing and holding F3 and C for 10 seconds, then releasing. Example: "This happened when playing version 2.4.2 of the FTB Infinity modpack" or "A list of mods can be found here: `<link to pastebin/gist/...>`" or "Only IE was installed when this happened".

### Crashlogs
If your Minecraft instance has crashed, a file will have been generated in the folder `crash-reports` of your minecraft folder. To understand what has happened, we need to know the content of that file. But please don't just put it directly in your report (that makes it hard to read), upload it to a site like [pastebin](http://pastebin.com) or [gist](http://gist.github.com) and put a link in the actuall bug report.

### Other mods
Some mods are not officially supported by IE. They will probably work pretty well, but some thing might not work/look weird. If your modpack contains one or more of these mods and you encounter a bug, try removing the unsupported mods. If the bug/crash does not happen without those mods, don't report it since fixing interactions with those mods is usually impossible or extremely hard. The following mods are not officially supported:

- **Optifine**: Optifine changes a lot of Minecraft's rendering code and it is not legally possible to check what those changes are. Another problem is that there is no `dev`/`deobf` version of Optifine which makes running Optifine in a development environment pretty much impossible.

- **Fastcraft**: same as Optifine.

- **(K)Cauldron** and similar server software: While the source code of some of these is available on GitHub or similar platforms, it would require a lot of extra work to test everything with every server software.

### Known issues
 It is not unlikely that the issue you want to report has already been reported and maybe it has even been fixed for the next version of IE. Try searching for different terms related to your issue [here](https://github.com/Blusunrize/ImmersiveEngineering/issues?utf8=%E2%9C%93&q=is%3Aissue+). Example: Search terms for [#689](https://github.com/BluSunrize/ImmersiveEngineering/issues/689) (which is not actually a duplicate) could be `chemthrower`, `chemical thrower` and `flammable`.