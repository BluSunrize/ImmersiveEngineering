package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record InitialMultiblockContext<State extends IMultiblockState>(
		BlockEntity masterBE,
		MultiblockOrientation orientation,
		BlockPos masterOffset
) implements IInitialMultiblockContext<State>
{
	@Override
	public <T> CapabilityReference<T> getCapabilityAt(
			Capability<T> capability, BlockPos posRelativeToMB, RelativeBlockFace face
	)
	{
		return getCapabilityAt(masterBE, orientation, masterOffset, capability, posRelativeToMB, face);
	}

	@Override
	public Supplier<@Nullable Level> levelSupplier()
	{
		return masterBE::getLevel;
	}

	@Override
	public void markMasterDirty()
	{
		masterBE.setChanged();
	}

	public static <T> CapabilityReference<T> getCapabilityAt(
			BlockEntity masterBE, MultiblockOrientation orientation, BlockPos masterOffset,
			Capability<T> capability, BlockPos posRelativeToMB, RelativeBlockFace face
	)
	{
		return CapabilityReference.forBlockEntityAt(masterBE, () -> {
			final var offset = orientation.getAbsoluteOffset(posRelativeToMB.subtract(masterOffset));
			final var pos = masterBE.getBlockPos().offset(offset);
			final var absoluteFace = face.forFront(orientation);
			return new DirectionalBlockPos(pos, absoluteFace);
		}, capability);
	}
}
