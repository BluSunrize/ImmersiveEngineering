/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class TileRenderWatermill extends TileEntitySpecialRenderer<TileEntityWatermill>
{
	private static List<BakedQuad> quads;

	@Override
	public void render(TileEntityWatermill tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		if(quads==null)
		{
			final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
			IBlockState state = tile.getWorld().getBlockState(tile.getPos());
			if(state.getBlock()!=IEContent.blockWoodenDevice1)
				return;
			state = state.withProperty(IEProperties.FACING_ALL, EnumFacing.NORTH);
			quads = blockRenderer.getModelForState(state).getQuads(state, null, 0);
		}
		Tessellator tessellator = Tessellator.getInstance();
		GlStateManager.pushMatrix();

		GlStateManager.translate(x+.5, y+.5, z+.5);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		final float dir = (tile.facing.getHorizontalAngle()+180)%180;
		float wheelRotation = 360*(tile.rotation+(!tile.canTurn||tile.rotation==0?0: partialTicks)*(float)tile.perTick);
		GlStateManager.rotate(dir, 0, 1, 0);
		GlStateManager.rotate(wheelRotation, 0, 0, 1);
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		ClientUtils.renderModelTESRFast(quads, worldRenderer, tile.getWorld(), tile.getPos());
		worldRenderer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	public static void reset()
	{
		quads = null;
	}
}