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
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class TileRenderWindmill extends TileEntitySpecialRenderer<TileEntityWindmill>
{
	private static List<BakedQuad>[] quads = new List[9];

	@Override
	public void render(TileEntityWindmill tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		if(quads[tile.sails]==null)
		{
			IBlockState state = getWorld().getBlockState(blockPos);
			if(state.getBlock()!=IEContent.blockWoodenDevice1)
				return;
			state = state.getActualState(getWorld(), blockPos);
			state = state.withProperty(IEProperties.FACING_ALL, EnumFacing.NORTH);
			IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
			if(state instanceof IExtendedBlockState)
			{
				List<String> parts = new ArrayList<>();
				parts.add("base");
				for(int i = 1; i <= tile.sails; i++)
					parts.add("sail_"+i);
				state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(parts, true));
			}
			quads[tile.sails] = model.getQuads(state, null, 0);
		}
		Tessellator tessellator = Tessellator.getInstance();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);

		float dir = tile.facing==EnumFacing.SOUTH?0: tile.facing==EnumFacing.NORTH?180: tile.facing==EnumFacing.EAST?90: -90;
		float rot = 360*(tile.rotation+(!tile.canTurn||tile.rotation==0?0: partialTicks)*tile.perTick);

		GlStateManager.rotate(rot, tile.facing.getAxis()==Axis.X?1: 0, 0, tile.facing.getAxis()==Axis.Z?1: 0);
		GlStateManager.rotate(dir, 0, 1, 0);

		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		ClientUtils.renderModelTESRFast(quads[tile.sails], worldRenderer, tile.getWorld(), blockPos);
		worldRenderer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	public static void reset()
	{
		for(int i = 0; i < quads.length; i++)
			quads[i] = null;
	}
}