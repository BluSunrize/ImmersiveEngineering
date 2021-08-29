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
		Supplier<IConveyorClientData<T>> makeClientData,
		boolean acceptsCovers
) implements IConveyorType<T>
{
	public BasicConveyorType(
			ResourceLocation id,
			boolean ticking,
			boolean dyeable,
			Function<BlockEntity, T> makeBelt,
			Supplier<IConveyorClientData<T>> makeClientData
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
	public void initClientData(Consumer<IConveyorClientData<T>> clientData)
	{
		clientData.accept(makeClientData.get());
	}
}
