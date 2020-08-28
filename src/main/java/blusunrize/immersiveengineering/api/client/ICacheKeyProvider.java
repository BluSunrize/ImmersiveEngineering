package blusunrize.immersiveengineering.api.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Implement in IBakedModel when using dynamic split models. Models with equivalent cache keys will only be queried
 * and split once
 */
public interface ICacheKeyProvider<K> extends IBakedModel
{
	@Nullable
	K getKey(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull Random rand,
			@Nonnull IModelData extraData
	);
}
