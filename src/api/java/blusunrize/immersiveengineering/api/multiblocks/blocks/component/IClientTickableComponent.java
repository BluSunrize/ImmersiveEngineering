/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.component;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;

public interface IClientTickableComponent<State> extends IMultiblockComponent<State>
{
	void tickClient(IMultiblockContext<State> context);
}
