/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.ExtraComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Consumer;

public record ComponentInstance<S>(IMultiblockComponent<S> component, IMultiblockContext<S> wrappedContext)
{
	public static <S, O>
	ComponentInstance<S> make(ExtraComponent<O, S> c, O state, IMultiblockContext<?> ctx)
	{
		final S wrappedState = c.makeWrapper().wrapState(state);
		IMultiblockContext<S> wrapped;
		if(wrappedState==state)
			//noinspection unchecked
			wrapped = (IMultiblockContext<S>)ctx;
		else
			wrapped = new WrappingMultiblockContext<>(ctx, c.makeWrapper().wrapState(state));
		return new ComponentInstance<>(c.component(), wrapped);
	}

	public <T> LazyOptional<T> getCapability(CapabilityPosition position, Capability<T> cap)
	{
		return component.getCapability(wrappedContext, position, cap);
	}

	public void onEntityCollision(BlockPos positionInMB, Entity collided)
	{
		component.onEntityCollision(wrappedContext, positionInMB, collided);
	}

	public InteractionResult click(
			BlockPos positionInMB, Player player, InteractionHand hand, BlockHitResult hit, boolean isClientSide
	)
	{
		return component.click(wrappedContext, positionInMB, player, hand, hit, isClientSide);
	}

	public void dropExtraItems(Consumer<ItemStack> drop)
	{
		component.dropExtraItems(wrappedContext().getState(), drop);
	}

	public void tickServer()
	{
		if(component instanceof IServerTickableComponent<S> serverTickable)
			serverTickable.tickServer(wrappedContext);
	}

	public void tickClient()
	{
		if(component instanceof IClientTickableComponent<S> clientTickable)
			clientTickable.tickClient(wrappedContext);
	}

	public S state()
	{
		return wrappedContext.getState();
	}
}
