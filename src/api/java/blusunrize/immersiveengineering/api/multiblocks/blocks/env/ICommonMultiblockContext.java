/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

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
