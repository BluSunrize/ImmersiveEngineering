package blusunrize.immersiveengineering.client.models;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelProperty;

public class PrivateProperties
{
	/**
	 * This completely defeats the model data system, and is a bit of a nightmare in general. However removing all
	 * usage of it will take a lot of work, so for now it's staying.
	 */
	public static final ModelProperty<BlockEntity> BLOCKENTITY_PASSTHROUGH = new ModelProperty<>();
}
