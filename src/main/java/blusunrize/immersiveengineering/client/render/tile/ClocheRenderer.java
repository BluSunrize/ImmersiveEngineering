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
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class ClocheRenderer extends TileEntityRenderer<ClocheTileEntity>
{
	private static HashMap<Direction, List<BakedQuad>> quads = new HashMap<>();
	private static HashMap<BlockState, List<BakedQuad>> plantQuads = new HashMap<>();

	public ClocheRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(ClocheTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.dummy!=0||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		if(!quads.containsKey(tile.getFacing()))
		{
			BlockState state = tile.getWorld().getBlockState(blockPos);
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
		matrixStack.push();

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
			matrixStack.push();
			GlStateManager.color4f(1,1,1,1);
			GlStateManager.disableBlend();
			matrixStack.translate(0, 1.0625, 0);

			NonNullList<ItemStack> inventory = tile.getInventory();
			ItemStack seed = inventory.get(ClocheTileEntity.SLOT_SEED);
			float growth = MathHelper.clamp(tile.renderGrowth/recipe.time, 0, 1);
			float scale = recipe.renderFunction.getScale(seed, growth);
			matrixStack.translate((1-scale)/2, 0, (1-scale)/2);
			matrixStack.scale(scale, scale, scale);

			Collection<Pair<BlockState, TransformationMatrix>> blocks = recipe.renderFunction.getBlocks(seed, growth);
			for(Pair<BlockState, TransformationMatrix> block : blocks)
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
				matrixStack.push();
				block.getRight().push(matrixStack);
				worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				ClientUtils.renderModelTESRFancy(plantQuadList, worldRenderer, tile.getWorldNonnull(), blockPos, false, col);
				Tessellator.getInstance().draw();
				matrixStack.pop();
			}

			GlStateManager.enableBlend();
			matrixStack.pop();
		}

		GlStateManager.depthMask(false);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		ClientUtils.renderModelTESRFast(quads.get(tile.getFacing()), worldRenderer, tile.getWorldNonnull(), blockPos);
		Tessellator.getInstance().draw();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);

		matrixStack.pop();
		RenderHelper.enableStandardItemLighting();
	}

	public static void reset()
	{
		quads.clear();
		plantQuads.clear();
	}
}