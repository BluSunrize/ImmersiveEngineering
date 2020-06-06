/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class ClocheRenderer extends TileEntityRenderer<ClocheTileEntity>
{
	private static HashMap<Direction, List<BakedQuad>> quads = new HashMap<>();
	private static HashMap<BlockState, List<BakedQuad>> plantQuads = new HashMap<>();

	@Override
	public void render(ClocheTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.dummy!=0||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		if(!quads.containsKey(tile.getFacing()))
		{
			BlockState state = getWorld().getBlockState(blockPos);
			if(state.getBlock()!=MetalDevices.cloche)
				return;
			IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);
			//TODO use a multi layer model?
			IModelData data = new SinglePropertyModelData<>(
					new IEObjState(VisibilityList.show(Collections.singletonList("glass"))),
					Model.IE_OBJ_STATE);
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

		ClocheRecipe recipe = tile.getRecipe();
		if(recipe!=null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.color4f(1,1,1,1);
			GlStateManager.disableBlend();
			GlStateManager.translated(0, 1.0625, 0);

			NonNullList<ItemStack> inventory = tile.getInventory();
			ItemStack seed = inventory.get(ClocheTileEntity.SLOT_SEED);
			float growth = MathHelper.clamp(tile.renderGrowth/recipe.time, 0, 1);
			float scale = recipe.renderFunction.getScale(seed, growth);
			GlStateManager.translated((1-scale)/2, 0, (1-scale)/2);
			GlStateManager.scalef(scale, scale, scale);

			Collection<Pair<BlockState, TRSRTransformation>> blocks = recipe.renderFunction.getBlocks(seed, growth);
			for(Pair<BlockState, TRSRTransformation> block : blocks)
			{
				BlockState state = block.getLeft();
				List<BakedQuad> plantQuadList = plantQuads.get(state);
				if(plantQuadList==null)
				{
					IBakedModel plantModel = blockRenderer.getModelForState(state);
					plantQuadList = new ArrayList<>(plantModel.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE));
					for(Direction f : Direction.values())
						plantQuadList.addAll(plantModel.getQuads(state, f, Utils.RAND, EmptyModelData.INSTANCE));
					plantQuads.put(state, plantQuadList);
				}
				int col = ClientUtils.mc().getBlockColors().getColor(state, null, blockPos, -1);
				GlStateManager.pushMatrix();
				GlStateManager.multMatrix(TRSRTransformation.toMojang(block.getRight().getMatrixVec()));
				worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				ClientUtils.renderModelTESRFancy(plantQuadList, worldRenderer, tile.getWorldNonnull(), blockPos, false, col);
				Tessellator.getInstance().draw();
				GlStateManager.popMatrix();
			}

			GlStateManager.enableBlend();
			GlStateManager.popMatrix();
		}

		GlStateManager.depthMask(false);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		ClientUtils.renderModelTESRFast(quads.get(tile.getFacing()), worldRenderer, tile.getWorldNonnull(), blockPos);
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