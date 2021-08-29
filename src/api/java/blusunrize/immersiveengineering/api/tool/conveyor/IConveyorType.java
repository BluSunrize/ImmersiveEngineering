package blusunrize.immersiveengineering.api.tool.conveyor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public interface IConveyorType<T extends IConveyorBelt>
{
	T makeInstance(BlockEntity blockEntity);

	boolean isTicking();

	/**
	 * @return true if the conveyor can be dyed
	 */
	boolean canBeDyed();

	void initClientData(Consumer<IConveyorClientData<T>> clientData);

	ResourceLocation getId();

	boolean acceptsCovers();
}
