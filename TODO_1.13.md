###Changes that should be done in the 1.13 port (aside from porting)

(Or at any other point where breaking all addon compatibility is ok)

 - Fences should extend `BlockFence` to allow lead attachment ()and possibly connection to vanilla fences?) #2771
 - Remove config-mirroring fields from all over the place (e.g. `WireType.wireTransferRate`)
 - Remove WireType parameter from ``IImmersiveConnectable.getConnectionMaster``
 - Split `ApiUtils` in a client and a server class
 - Refer to the garden cloche by that name rather than as the belljar in the config, and possibly in code too
 
Maybe:
 - Use the hooks added by [this PR](https://github.com/MinecraftForge/MinecraftForge/pull/6010) to replace the `DYNAMICRENDER` blockstate property