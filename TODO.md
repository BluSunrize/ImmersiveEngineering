Things to do before the release. Not a complete list, but a list of things that are likely to be forgotten if they aren't on a list somewhere.
 - Rendering formed structures in the manual is probably broken right now
   - Render OBJs/block models directly rather than item models?
 - Wire sync is currently broken due to a Forge bug, see [here](https://github.com/MinecraftForge/MinecraftForge/pull/6519) (Now has an ASM workaround in IE)
 - Hemp seeds dropping from grass is broken, also waiting for a Forge PR to be merged: either [this](https://github.com/MinecraftForge/MinecraftForge/pull/5871) or [this](https://github.com/MinecraftForge/MinecraftForge/pull/6267)
 - Fluids currently don't push the player around. They would need to be in the water-tag for that, which would likely have a lot of other unintended side effects.
 
From TODO_1.13.md:
 - Split `ApiUtils` in a client and a server class
 - Refer to the garden cloche by that name rather than as the belljar in the config, and possibly in code too
