package blusunrize.immersiveengineering.api.tool.conveyor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IConveyorType<T extends IConveyorBelt>
{
	T makeInstance(BlockEntity blockEntity);

	default boolean isTicking()
	{
		return false;
	}

	/**
	 * @return true if the conveyor can be dyed
	 */
	boolean canBeDyed();

	void initClientData(Consumer<IConveyorClientData<? super T>> clientData);

	ResourceLocation getId();

	Block getCover(@Nullable T conveyor);
}
