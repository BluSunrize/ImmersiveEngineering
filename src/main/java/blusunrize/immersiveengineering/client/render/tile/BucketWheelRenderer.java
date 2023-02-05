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
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import blusunrize.immersiveengineering.client.models.obj.callback.block.BucketWheelCallbacks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BucketWheelLogic.State;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BucketWheelRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	public static final String NAME = "bucket_wheel";
	public static DynamicModel WHEEL;
	private static final Cache<BucketWheelCallbacks.Key, IVertexBufferHolder> CACHED_BUFFERS = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.<Object, IVertexBufferHolder>removalListener(rem -> rem.getValue().reset())
			.build();

	@Override
	public void render(MultiblockBlockEntityMaster<State> tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final IMultiblockBEHelperMaster<State> helper = tile.getHelper();
		final Direction facing = helper.getContext().getLevel().getOrientation().front();
		final State state = helper.getState();
		final boolean mirrored = state.reverseRotation;
		matrixStack.pushPose();

		matrixStack.translate(.5, .5, .5);
		matrixStack.mulPose(new Quaternionf().rotateY(Mth.HALF_PI)); //to mirror different plane. compensate on dir rotate
		bufferIn = BERenderUtils.mirror(new MultiblockOrientation(facing, mirrored), matrixStack, bufferIn);
		float dir = facing==Direction.SOUTH?0: facing==Direction.NORTH?Mth.PI: facing==Direction.EAST?Mth.HALF_PI: -Mth.HALF_PI;
		matrixStack.mulPose(new Quaternionf().rotateY(dir));
		float rot = state.rotation+(float)(state.active?IEServerConfig.MACHINES.excavator_speed.get()*partialTicks: 0);
		matrixStack.mulPose(new Quaternionf().rotateX(rot*Mth.DEG_TO_RAD));

		matrixStack.translate(-.5, -.5, -.5);
		try
		{
			BucketWheelCallbacks.Key key = BucketWheelCallbacks.INSTANCE.extractKey(null, null, null, tile);
			ModelData extraData = ModelDataUtils.single(IEOBJCallbacks.getModelProperty(BucketWheelCallbacks.INSTANCE), key);

			CACHED_BUFFERS.get(key, () -> IVertexBufferHolder.create(
					() -> WHEEL.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, extraData, RenderType.solid())
			)).render(RenderType.solid(), combinedLightIn, combinedOverlayIn, bufferIn, matrixStack, state.reverseRotation);
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