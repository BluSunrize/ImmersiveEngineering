###Changes that should be done in the 1.13 port (aside from porting)

(Or at any other point where breaking all addon compatibility is ok)

 - Fences should extend `BlockFence` to allow lead attachment ()and possibly connection to vanilla fences?) #2771
 - General cleanup of the net handler
   - Probably make a net handler object for each dimension? Currectly all maps have int as the first key
   - Merge `ImmersiveNetHandler.indirectConnectionsNoOut` and `ImmersiveNetHandler.indirectConnections`
   - Public methods shouldn't leave the net handler in an "inconsistent" state (diodes/single-direction edges) etc.
 - Remove config-mirroring fields from all over the place (e.g. `WireType.wireTransferRate`)
 - Remove WireType parameter from ``IImmersiveConnectable.getConnectionMaster``
 - Split `ApiUtils` in a client and a server class
 
Maybe:
 - Change the manual to:
   1. Use a file per entry (in a language-specific directory) rather than a line in the lang file per page
   2. Auto-split the entries into lines and pages ([Possible implementation](https://github.com/malte0811/IndustrialWires/blob/MC1.12/src/main/java/malte0811/industrialWires/client/manual/TextSplitter.java))
   