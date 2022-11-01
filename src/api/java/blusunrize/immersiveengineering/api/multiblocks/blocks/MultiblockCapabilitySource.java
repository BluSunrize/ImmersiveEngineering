package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

public interface MultiblockCapabilitySource
{
	<T> CapabilityReference<T> getCapabilityAt(
			Capability<T> capability, BlockPos posRelativeToMB, RelativeBlockFace face
	);
}
