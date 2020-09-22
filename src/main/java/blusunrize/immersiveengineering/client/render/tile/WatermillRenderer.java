/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.wooden.WatermillTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;

public class WatermillRenderer extends TileEntityRenderer<WatermillTileEntity>
{
	public static DynamicModel<Void> MODEL;
	private static final IVertexBufferHolder MODEL_BUFFER = IVertexBufferHolder.create(() -> {
		BlockState state = WoodenDevices.watermill.getDefaultState()
				.with(IEProperties.FACING_HORIZONTAL, Direction.NORTH);
		return MODEL.get(null).getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
	});

	public WatermillRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(WatermillTileEntity tile, float partialTicks, MatrixStack transform, IRenderTypeBuffer bufferIn,
					   int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		transform.push();
		transform.translate(.5, .5, .5);
		final float dir = (tile.getFacing().getHorizontalAngle()+180)%180;
		float wheelRotation = 360*(tile.rotation+(!tile.canTurn||tile.rotation==0?0: partialTicks)*(float)tile.perTick);
		transform.rotate(new Quaternion(new Vector3f(0, 1, 0), dir, true));
		transform.rotate(new Quaternion(new Vector3f(0, 0, 1), wheelRotation, true));
		transform.translate(-.5, -.5, -.5);
		MODEL_BUFFER.render(RenderType.getCutoutMipped(), combinedLightIn, combinedOverlayIn, bufferIn, transform);
		transform.pop();
	}

	public static void reset()
	{
		MODEL_BUFFER.reset();
	}
}