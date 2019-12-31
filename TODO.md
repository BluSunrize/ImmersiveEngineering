Things to do before the release. Not a complete list, but a list of things that are likely to be forgotten if they aren't on a list somewhere.
 - Test auto workbench and check that behavior matches 1.12
 - Port missing manual entries
 - Rendering formed structures in the manual is probably broken right now
   - Render OBJs/block models directly rather than item models?
 - Wire sync is currently broken due to a Forge bug, see [here](https://github.com/MinecraftForge/MinecraftForge/pull/6245)
 - Hemp seeds dropping from grass is broken, also waiting for a Forge PR to be merged: either [this](https://github.com/MinecraftForge/MinecraftForge/pull/5871) or [this](https://github.com/MinecraftForge/MinecraftForge/pull/6267)
 
From TODO_1.13.md:
 - Split `ApiUtils` in a client and a server class
 - Refer to the garden cloche by that name rather than as the belljar in the config, and possibly in code too
