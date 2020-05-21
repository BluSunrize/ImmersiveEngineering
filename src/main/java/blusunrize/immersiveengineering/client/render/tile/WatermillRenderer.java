/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.wooden.WatermillTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;

public class WatermillRenderer extends TileEntityRenderer<WatermillTileEntity>
{
	private static List<BakedQuad> quads;
	private final DynamicModel<Void> model = DynamicModel.createSimple(
			new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_device/watermill.obj.ie"),
			"watermill", ModelType.IE_OBJ);

	public WatermillRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(WatermillTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		if(quads==null)
		{
			BlockState state = tile.getWorldNonnull().getBlockState(tile.getPos());
			if(state.getBlock()!=WoodenDevices.watermill)
				return;
			state = state.with(IEProperties.FACING_HORIZONTAL, Direction.NORTH);
			quads = model.get(null).getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
		}
		Tessellator tessellator = Tessellator.getInstance();
		matrixStack.push();

		matrixStack.translate(.5, .5, .5);
		final float dir = (tile.getFacing().getHorizontalAngle()+180)%180;
		float wheelRotation = 360*(tile.rotation+(!tile.canTurn||tile.rotation==0?0: partialTicks)*(float)tile.perTick);
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), dir, true));
		matrixStack.rotate(new Quaternion(new Vector3f(0, 0, 1), wheelRotation, true));
		matrixStack.translate(-.5, -.5, -.5);
		IVertexBuilder builder = bufferIn.getBuffer(RenderType.getSolid());
		ClientUtils.renderModelTESRFast(quads, builder, matrixStack, combinedLightIn);
		matrixStack.pop();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	public static void reset()
	{
		quads = null;
	}
}