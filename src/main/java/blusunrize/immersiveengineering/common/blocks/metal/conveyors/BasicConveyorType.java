package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorClientData;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record BasicConveyorType<T extends ConveyorBase>(
		ResourceLocation id,
		boolean ticking,
		boolean dyeable,
		Function<BlockEntity, T> makeBelt,
		Supplier<IConveyorClientData<T>> makeClientData
) implements IConveyorType<T>
{

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
	public void initClientData(Consumer<IConveyorClientData<? super T>> clientData)
	{
		clientData.accept(makeClientData.get());
	}

	@Override
	public Block getCover(T conveyor)
	{
		return conveyor!=null?conveyor.cover: Blocks.AIR;
	}
}
