/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.component;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockComponent;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public record MultiblockGui<S>(ArgContainer<IMultiblockContext<S>, ?> menu) implements IMultiblockComponent<S>
{
	@Override
	public InteractionResult click(
			IMultiblockContext<S> ctx,
			BlockPos posInMultiblock,
			Player player,
			InteractionHand hand,
			BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		if(!isClient)
			player.openMenu(menu.provide(ctx));
		return InteractionResult.SUCCESS;
	}
}
