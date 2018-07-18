/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TileRenderBelljar extends TileEntitySpecialRenderer<TileEntityBelljar>
{
	private static HashMap<EnumFacing, List<BakedQuad>> quads = new HashMap<>();
	private static HashMap<IBlockState, List<BakedQuad>> plantQuads = new HashMap<>();

	@Override
	public void render(TileEntityBelljar tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(tile.dummy!=0||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		if(!quads.containsKey(tile.getFacing()))
		{
			IBlockState state = getWorld().getBlockState(blockPos);
			if(state.getBlock()!=IEContent.blockMetalDevice1)
				return;
			state = state.getActualState(getWorld(), blockPos);
			IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
			if(state instanceof IExtendedBlockState)
				state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(Arrays.asList("glass"), true));
			quads.put(tile.getFacing(), model.getQuads(state, null, 0));
		}
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder worldRenderer = Tessellator.getInstance().getBuffer();


		GlStateManager.enableCull();
		IPlantHandler plantHandler = tile.getCurrentPlantHandler();
		if(plantHandler!=null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 1.0625, 0);
			GlStateManager.color(1, 1, 1, 1);
			NonNullList<ItemStack> inventory = tile.getInventory();
			float scale = plantHandler.getRenderSize(inventory.get(1), inventory.get(0), tile.renderGrowth, tile);
			GlStateManager.translate((1-scale)/2, 0, (1-scale)/2);
			GlStateManager.scale(scale, scale, scale);
			if(!plantHandler.overrideRender(inventory.get(1), inventory.get(0), tile.renderGrowth, tile, blockRenderer))
			{
				IBlockState[] states = plantHandler.getRenderedPlant(inventory.get(1), inventory.get(0), tile.renderGrowth, tile);
				if(states==null||states.length < 1)
					return;
				for(IBlockState s : states)
				{
					List<BakedQuad> plantQuadList = plantQuads.get(s);
					if(plantQuadList==null)
					{
						IBakedModel plantModel = blockRenderer.getModelForState(s);
						plantQuadList = new ArrayList<BakedQuad>(plantModel.getQuads(s, null, 0));
						for(EnumFacing f : EnumFacing.values())
							plantQuadList.addAll(plantModel.getQuads(s, f, 0));
						plantQuads.put(s, plantQuadList);
					}
					if(plantQuadList!=null)
					{
						GlStateManager.pushMatrix();
						worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
						ClientUtils.renderModelTESRFancy(plantQuadList, worldRenderer, tile.getWorld(), blockPos, false);
						Tessellator.getInstance().draw();
						GlStateManager.popMatrix();
						GlStateManager.translate(0, 1, 0);
					}
				}
			}
			GlStateManager.popMatrix();
		}

		GlStateManager.depthMask(false);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		ClientUtils.renderModelTESRFast(quads.get(tile.getFacing()), worldRenderer, tile.getWorld(), blockPos);
		Tessellator.getInstance().draw();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);

		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
	}

	public static void reset()
	{
		quads.clear();
		plantQuads.clear();
	}
}