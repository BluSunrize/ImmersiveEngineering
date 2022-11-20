package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@NonExtendable
public interface IInitialMultiblockContext<State extends IMultiblockState> extends ICommonMultiblockContext
{
	Supplier<@Nullable Level> levelSupplier();

	Runnable getMarkDirtyRunnable();

	Runnable getSyncRunnable();
}
