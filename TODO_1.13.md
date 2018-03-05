###Changes that should be done in the 1.13 port (aside from porting)

 - Fences should extend `BlockFence` to allow lead attachment ()and possibly connection to vanilla fences?) #2771
 - General cleanup of the net handler
   - Probably make a net handler object for each dimension? Currectly all maps have int as the first key
   - Merge `ImmersiveNetHandler.indirectConnectionsNoOut` and `ImmersiveNetHandler.indirectConnections`
   - Public methods shouldn't leave the net handler in an "inconsistent" state (diodes/single-direction edges) etc.