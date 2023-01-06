/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.conveyor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record BasicConveyorType<T extends IConveyorBelt>(
		ResourceLocation id,
		boolean ticking,
		boolean dyeable,
		Function<BlockEntity, T> makeBelt,
		Supplier<IConveyorModelRender<T>> makeClientData,
		boolean acceptsCovers
) implements IConveyorType<T>
{
	public BasicConveyorType(
			ResourceLocation id,
			boolean ticking,
			boolean dyeable,
			Function<BlockEntity, T> makeBelt,
			Supplier<IConveyorModelRender<T>> makeClientData
	)
	{
		this(id, ticking, dyeable, makeBelt, makeClientData, true);
	}

	@Override
	public T makeInstance(BlockEntity blockEntity)
	{
		return makeBelt.apply(blockEntity);
	}

	@Override
	public boolean canBeDyed()
	{
		return dyeable;
	}

	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Override
	public boolean isTicking()
	{
		return ticking;
	}

	@Override
	public void initClientData(Consumer<IConveyorModelRender<T>> clientData)
	{
		clientData.accept(makeClientData.get());
	}
}
