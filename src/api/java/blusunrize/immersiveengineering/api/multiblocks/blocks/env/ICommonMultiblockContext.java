package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ICommonMultiblockContext
{
	default <T> CapabilityReference<T> getCapabilityAt(Capability<T> capability, MultiblockFace face)
	{
		return getCapabilityAt(capability, face.posInMultiblock(), face.face());
	}

	<T> CapabilityReference<T> getCapabilityAt(
			Capability<T> capability, BlockPos posRelativeToMB, RelativeBlockFace face
	);
}
