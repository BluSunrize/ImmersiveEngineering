/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
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
