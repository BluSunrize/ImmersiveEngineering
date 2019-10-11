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
import blusunrize.immersiveengineering.client.DynamicModelLoader;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.ConfiguredModel;
import blusunrize.immersiveengineering.common.data.model.ModelFile.ExistingModelFile;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class CrusherRenderer extends TileEntityRenderer<CrusherTileEntity>
{
	private static final Map<Direction, ModelResourceLocation> BARREL_NAMES = new HashMap<>();
	private static final ResourceLocation BARREL_LOC = new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/crusher_drum.obj");

	static
	{
		ResourceLocation baseLoc = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/crusher");
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			BARREL_NAMES.put(d, new ModelResourceLocation(baseLoc, d.getName()));
	}

	public CrusherRenderer()
	{
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
		{
			ConfiguredModel model = new ConfiguredModel(new ExistingModelFile(BARREL_LOC), 0,
					(int)d.getHorizontalAngle(), false, ImmutableMap.of("flip-v", true));
			DynamicModelLoader.requestModel(model, BARREL_NAMES.get(d));
		}
	}

	@Override
	public void render(CrusherTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.crusher)
			return;
		Direction dir = te.getFacing();
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelManager().getModel(BARREL_NAMES.get(dir));

		boolean b = te.shouldRenderAsActive();
		float angle = te.animation_barrelRotation+(b?18*partialTicks: 0);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.translated(.5, 1.5, .5);


		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		GlStateManager.translated(te.getFacing().getXOffset()*.5, 0, te.getFacing().getZOffset()*.5);
		GlStateManager.rotatef(angle, -te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());
		renderPart(worldRenderer, blockPos, blockRenderer, tessellator, te, model, state);
		GlStateManager.rotatef(-angle, -te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());
		GlStateManager.translated(te.getFacing().getXOffset()*-1, 0, te.getFacing().getZOffset()*-1);
		GlStateManager.rotatef(-angle, -te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());
		renderPart(worldRenderer, blockPos, blockRenderer, tessellator, te, model, state);
		GlStateManager.rotatef(angle, -te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());

		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}

	private void renderPart(BufferBuilder bb, BlockPos pos, BlockRendererDispatcher blockRenderer, Tessellator tes,
							TileEntity te, IBakedModel model, BlockState state)
	{
		bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		bb.setTranslation(-.5-pos.getX(), -.5-pos.getY(), -.5-pos.getZ());
		bb.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, pos, bb, true, Utils.RAND,
				0, EmptyModelData.INSTANCE);
		bb.setTranslation(0.0D, 0.0D, 0.0D);
		tes.draw();
	}

}