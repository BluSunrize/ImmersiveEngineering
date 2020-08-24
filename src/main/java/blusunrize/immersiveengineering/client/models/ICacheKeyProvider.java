package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.client.models.connection.RenderCacheKey;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public interface ICacheKeyProvider
{
	@Nullable
	RenderCacheKey getKey(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull Random rand,
			@Nonnull IModelData extraData
	);
}
