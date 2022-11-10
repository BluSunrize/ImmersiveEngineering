package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface IInitialMultiblockContext<State extends IMultiblockState> extends ICommonMultiblockContext
{
	Supplier<@Nullable Level> levelSupplier();
}
