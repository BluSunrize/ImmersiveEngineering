/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallbacks;
import blusunrize.immersiveengineering.client.models.obj.callback.block.BucketWheelCallbacks;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelBlockEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelData;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class BucketWheelRenderer extends IEBlockEntityRenderer<BucketWheelBlockEntity>
{
	public static final String NAME = "bucket_wheel";
	public static DynamicModel WHEEL;
	private static final Cache<BucketWheelCallbacks.Key, IVertexBufferHolder> CACHED_BUFFERS = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.<Object, IVertexBufferHolder>removalListener(rem -> rem.getValue().reset())
			.build();

	@Override
	public void render(BucketWheelBlockEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||!tile.getLevelNonnull().hasChunkAt(tile.getBlockPos())||tile.isDummy())
			return;
		matrixStack.pushPose();

		matrixStack.translate(.5, .5, .5);
		matrixStack.mulPose(new Quaternionf(new Vector3f(0, 1, 0), 90, true)); //to mirror different plane. compensate on dir rotate
		BERenderUtils.mirror(tile, matrixStack);
		float dir = tile.getFacing()==Direction.SOUTH?0: tile.getFacing()==Direction.NORTH?180: tile.getFacing()==Direction.EAST?90: -90;
		matrixStack.mulPose(new Quaternionf(new Vector3f(0, 1, 0), dir, true));
		float rot = tile.rotation+(float)(tile.active?IEServerConfig.MACHINES.excavator_speed.get()*partialTicks: 0);
		matrixStack.mulPose(new Quaternionf(new Vector3f(1, 0, 0), rot, true));

		matrixStack.translate(-.5, -.5, -.5);
		try
		{
			BucketWheelCallbacks.Key key = BucketWheelCallbacks.INSTANCE.extractKey(
					tile.getLevelNonnull(), tile.getBlockPos(), tile.getState(), tile
			);
			ModelData extraData = ModelDataUtils.single(IEOBJCallbacks.getModelProperty(BucketWheelCallbacks.INSTANCE), key);

			CACHED_BUFFERS.get(key, () -> IVertexBufferHolder.create(
					() -> WHEEL.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, extraData, RenderType.solid())
			)).render(RenderType.solid(), combinedLightIn, combinedOverlayIn, bufferIn, matrixStack, tile.getIsMirrored());
		} catch(ExecutionException ex)
		{
			throw new RuntimeException(ex);
		}
		matrixStack.popPose();
	}

	public static void reset()
	{
		CACHED_BUFFERS.invalidateAll();
	}
}