/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

public class SampleDrillRenderer extends TileEntityRenderer<SampleDrillTileEntity>
{
	private final DynamicModel<Void> drill = DynamicModel.createSimple(
			new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_device/core_drill_center.obj"),
			"sample_drill", ModelType.OBJ
	);


	@Override
	public void render(SampleDrillTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = tile.getWorldNonnull().getBlockState(tile.getPos());
		BlockPos blockPos = tile.getPos();
		IBakedModel model = drill.get(null);
		if(state.getBlock()!=MetalDevices.sampleDrill)
			return;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);

		int max = IEConfig.MACHINES.coredrill_time.get();
		if(tile.process > 0&&tile.process < max)
		{
			GlStateManager.rotatef(((tile.process+partialTicks)*22.5f)%360f, 0, 1, 0);
			float push = tile.process/(float)max;
			if(tile.process > max/2)
				push = 1-push;
			GlStateManager.translated(0, -2.8f*push, 0);
		}

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-blockPos.getX(), -.5-blockPos.getY(), -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorldNonnull(), model, state, tile.getPos(), worldRenderer, true,
				getWorld().rand, 0, EmptyModelData.INSTANCE);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
	}
}