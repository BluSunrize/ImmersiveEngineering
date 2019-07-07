/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TileRenderBelljar extends TileEntityRenderer<TileEntityBelljar>
{
	private static HashMap<Direction, List<BakedQuad>> quads = new HashMap<>();
	private static HashMap<BlockState, List<BakedQuad>> plantQuads = new HashMap<>();

	@Override
	public void render(TileEntityBelljar tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.dummy!=0||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		if(!quads.containsKey(tile.getFacing()))
		{
			BlockState state = getWorld().getBlockState(blockPos);
			if(state.getBlock()!=MetalDevices.belljar)
				return;
			IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);
			IModelData data = new SinglePropertyModelData<>(new OBJState(Collections.singletonList("glass"), true),
					Model.objState);
			quads.put(tile.getFacing(), model.getQuads(state, null, Utils.RAND, data));
		}
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder worldRenderer = Tessellator.getInstance().getBuffer();


		GlStateManager.enableCull();
		IPlantHandler plantHandler = tile.getCurrentPlantHandler();
		if(plantHandler!=null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translated(0, 1.0625, 0);
			GlStateManager.color3f(1, 1, 1);
			NonNullList<ItemStack> inventory = tile.getInventory();
			float scale = plantHandler.getRenderSize(inventory.get(1), inventory.get(0), tile.renderGrowth, tile);
			GlStateManager.translated((1-scale)/2, 0, (1-scale)/2);
			GlStateManager.scalef(scale, scale, scale);
			if(!plantHandler.overrideRender(inventory.get(1), inventory.get(0), tile.renderGrowth, tile, blockRenderer))
			{
				BlockState[] states = plantHandler.getRenderedPlant(inventory.get(1), inventory.get(0), tile.renderGrowth, tile);
				if(states==null||states.length < 1)
					return;
				for(BlockState s : states)
				{
					List<BakedQuad> plantQuadList = plantQuads.get(s);
					if(plantQuadList==null)
					{
						IBakedModel plantModel = blockRenderer.getModelForState(s);
						plantQuadList = new ArrayList<>(plantModel.getQuads(s, null, Utils.RAND, EmptyModelData.INSTANCE));
						for(Direction f : Direction.values())
							plantQuadList.addAll(plantModel.getQuads(s, f, Utils.RAND, EmptyModelData.INSTANCE));
						plantQuads.put(s, plantQuadList);
					}
					GlStateManager.pushMatrix();
					worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					ClientUtils.renderModelTESRFancy(plantQuadList, worldRenderer, tile.getWorld(), blockPos, false);
					Tessellator.getInstance().draw();
					GlStateManager.popMatrix();
					GlStateManager.translated(0, 1, 0);
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