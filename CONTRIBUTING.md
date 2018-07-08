### Pull requests
When wanting to contribute code to IE, please make sure to follow its inherent style of formatting.
A matching xml file for use with IntelliJ's codestyle feature can be found [here](https://gist.github.com/BluSunrize/5c05af8f29de9581426551d9b29b1809).

### Issues
#### General
- Make sure to use to the latest version of Immersive Engineering (IE for short). You can download it [here](http://minecraft.curseforge.com/projects/immersive-engineering).
- IE will create a file for its coremod in your `mods` folder. This is a `.jar` file called `ImmersiveEngineering-0.12-x-core.jar` before IE build 81 and Forge build 2656 and a directory called `memory-repo` afterwards. This is normal and not something to worry about. It's Forge convention to place coremods in separate files. The file is not downloaded, it is shipped with IE and automatically unpacked by Forge.
- Your bug report should contain answers to these questions (If adding screenshots makes answering one of these questions easier, add them):
  - What is the bug? E.g. "The game crashed with this crash report: `<Link to crashlog>`".
  - What did you do to make it happen? E.g. "I threw a revolver into a crusher".
  - Does it happen every time you do this? Does it happen if you do something else as well? E.g. "The game crashes with a similar crash whenever an item that can not be processed in the crusher is thrown into one.".
  - Did this happen on a dedicated server (multiplayer servers), in LAN multiplayer or in singleplayer? E.g. "I first noticed this on a server, but it happens in singleplayer as well".
  - What other mods were installed when the bug happened? Crashlogs always contain a modlist, so you can skip this part if you already provided one. You can generate a crash and therefore a mod list by pressing and holding F3 and C for 10 seconds, then releasing. Example: "This happened when playing version 2.4.2 of the FTB Infinity modpack" or "A list of mods can be found here: `<link to pastebin/gist/...>`" or "Only IE was installed when this happened".

#### Crashlogs
If your Minecraft instance has crashed, a file will have been generated in the folder `crash-reports` of your minecraft folder. To understand what has happened, we need to know the content of that file. But please don't just put it directly in your report (that makes it hard to read), upload it to a site like [pastebin](http://pastebin.com) or [gist](http://gist.github.com) and put a link in the actuall bug report.

There is one case where no crash log will be generated: If it wasn't MC that crashed but Java. There will be a file called `hs_err` and then some number in your minecraft folder. If that happens, make sure your Java version is up to date (But not Java 9, Minecraft isn't compatible with it). Also make sure that Minecraft is actually using that version, the vanilla launcher includes an old Java version (Java 8 update 25, published in autumn 2014). You can see what version Minecraft is using in the top right of the debug screen (F3). There is a known JVM crash that happens at random while browsing the engineer's manual but only with the old Java version.

#### Other mods
Some mods are not officially supported by IE. They will probably work pretty well, but some thing might not work/look weird. If your modpack contains one or more of these mods and you encounter a bug, try removing the unsupported mods. If the bug/crash does not happen without those mods, don't report it since fixing interactions with those mods is usually impossible or extremely hard. The following mods are not officially supported:

- **Optifine**: Optifine changes a lot of Minecraft's rendering code and it is not legally possible to check what those changes are. Another problem is that there is no `dev`/`deobf` version of Optifine which makes running Optifine in a development environment pretty much impossible.

- **Fastcraft**: same as Optifine.

- **SpongeForge** and similar server software: While the source code of some of these is available on GitHub or similar platforms, it would require a lot of extra work to test everything with every server software.

- **Torcherino** and other mods to speed up machines (not crops): If they cause a crash, report it. If they don't work on IE machines or cause rendering glitches, don't report it.

#### Known issues
 It is not unlikely that the issue you want to report has already been reported and maybe it has even been fixed for the next version of IE. Try searching for different terms related to your issue [here](https://github.com/Blusunrize/ImmersiveEngineering/issues?utf8=%E2%9C%93&q=is%3Aissue+). You can enter search terms in the box above the list of issues. Please <b>always</b> search for both open and closed issues as issues are closed when the fix has been written, not when a release containing a fix is published. If your issue has already been reported, please do <b>not</b> post comments like "I have the same crash: &lt;link to crashlog&gt;" unless someone specifically asks for additional crashlogs to help track down the issue.
 
Example: Assume you experienced the crash described in [#1549](https://github.com/BluSunrize/ImmersiveEngineering/issues/1549). Some possible search terms are ["crash crafting wire cutter"](https://github.com/BluSunrize/ImmersiveEngineering/issues?utf8=%E2%9C%93&q=is%3Aissue%20crash%20crafting%20wire%20cutter%20), ["crafting crash"](https://github.com/BluSunrize/ImmersiveEngineering/issues?utf8=%E2%9C%93&q=is%3Aissue%20crafting%20crash) and ["voltmeter crash"](https://github.com/BluSunrize/ImmersiveEngineering/issues?utf8=%E2%9C%93&q=is%3Aissue%20voltmeter%20crash).