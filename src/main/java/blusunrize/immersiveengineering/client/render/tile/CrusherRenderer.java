/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

public class CrusherRenderer extends TileEntityRenderer<CrusherTileEntity>
{
	private final DynamicModel<Direction> barrel = DynamicModel.createSided(
			new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/crusher_drum.obj"),
			"crusher_barrel", ModelType.OBJ
	);

	public CrusherRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(CrusherTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.crusher)
			return;
		Direction dir = te.getFacing();
		IBakedModel model = barrel.get(dir);

		boolean b = te.shouldRenderAsActive();
		float angle = te.animation_barrelRotation+(b?18*partialTicks: 0);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		matrixStack.push();
		matrixStack.translate(.5, 1.5, .5);


		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		matrixStack.translate(te.getFacing().getXOffset()*.5, 0, te.getFacing().getZOffset()*.5);
		GlStateManager.rotatef(angle, -te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());
		renderPart(matrixStack, bufferIn, blockPos, blockRenderer, te, model, state, combinedOverlayIn);
		GlStateManager.rotatef(-angle, -te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());
		matrixStack.translate(te.getFacing().getXOffset()*-1, 0, te.getFacing().getZOffset()*-1);
		GlStateManager.rotatef(-angle, -te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());
		renderPart(matrixStack, bufferIn, blockPos, blockRenderer, te, model, state, combinedOverlayIn);
		GlStateManager.rotatef(angle, -te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());

		RenderHelper.enableStandardItemLighting();

		matrixStack.pop();
	}

	private void renderPart(MatrixStack matrix, IRenderTypeBuffer buffer, BlockPos pos, BlockRendererDispatcher blockRenderer,
							TileEntity te, IBakedModel model, BlockState state, int overlay)
	{
		matrix.push();
		matrix.translate(-.5, -.5, -.5);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, pos, matrix,
				buffer.getBuffer(RenderType.getSolid()), true, te.getWorld().rand,
				0, overlay, EmptyModelData.INSTANCE);
		matrix.pop();
	}

}