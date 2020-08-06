/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceTileEntity;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ArcFurnaceRenderer extends TileEntityRenderer<ArcFurnaceTileEntity>
{
	private TextureAtlasSprite hotMetal_flow = null;
	private TextureAtlasSprite hotMetal_still = null;

	public static DynamicModel<Direction> ELECTRODES;
	public static final ResourceLocation HOT_METLA_STILL = new ResourceLocation(ImmersiveEngineering.MODID, "block/fluid/hot_metal_still");
	public static final ResourceLocation HOT_METLA_FLOW = new ResourceLocation(ImmersiveEngineering.MODID, "block/fluid/hot_metal_flow");


	@Override
	public void render(ArcFurnaceTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;
		List<String> renderedParts = null;
		for(int i = 0; i < 3; i++)
			if(!te.getInventory().get(23+i).isEmpty())
			{
				if(renderedParts==null)
					renderedParts = Lists.newArrayList("electrode"+(i+1));
				else
					renderedParts.add("electrode"+(i+1));
			}
		if(renderedParts==null)
			return;
		if(te.shouldRenderAsActive())
			renderedParts.add("active");

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.arcFurnace)
			return;
		IBakedModel model = ELECTRODES.get(te.getFacing());
		IEObjState objState = new IEObjState(VisibilityList.show(renderedParts));

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.translated(.5, .5, .5);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-blockPos.getX(), -.5-blockPos.getY(), -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorldNonnull(), model, state, blockPos, worldRenderer, true,
				getWorld().rand, 0, new SinglePropertyModelData<>(objState, Model.IE_OBJ_STATE));
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		RenderHelper.enableStandardItemLighting();
		if(te.pouringMetal > 0)
		{
			if(hotMetal_flow==null)
			{
				hotMetal_still = ClientUtils.mc().getTextureMap().getAtlasSprite(HOT_METLA_STILL.toString());
				hotMetal_flow = ClientUtils.mc().getTextureMap().getAtlasSprite(HOT_METLA_FLOW.toString());
			}
			GlStateManager.rotatef(-te.getFacing().getHorizontalAngle()+180, 0, 1, 0);
			int process = 40;
			float speed = 5f;
			int pour = process-te.pouringMetal;
			Vec3d translation = Vec3d.ZERO;
			float h = (pour > (process-speed)?((process-pour)/speed*27): pour > speed?27: (pour/speed*27))/16f;
			GlStateManager.translated(-.5f, 1.25-.6875f, 1.5f);
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			GlStateManager.disableLighting();
			setLightmapDisabled(true);
			if(pour > (process-speed))
				translation = addTranslation(translation, worldRenderer, 0, -1.6875f+h, 0);
			if(h > 1)
			{
				translation = addTranslation(translation, worldRenderer, 0, -h, 0);
				ClientUtils.renderTexturedBox(worldRenderer, .375, 0, .375, .625, 1, .625, hotMetal_flow, true);
				translation = addTranslation(translation, worldRenderer, 0, 1, 0);
				ClientUtils.renderTexturedBox(worldRenderer, .375, 0, .375, .625, h-1, .625, hotMetal_flow, true);
				translation = addTranslation(translation, worldRenderer, 0, -1, 0);
				translation = addTranslation(translation, worldRenderer, 0, h, 0);
			}
			else
			{
				translation = addTranslation(translation, worldRenderer, 0, -h, 0);
				ClientUtils.renderTexturedBox(worldRenderer, .375, 0, .375, .625, h, .625, hotMetal_flow, true);
				translation = addTranslation(translation, worldRenderer, 0, h, 0);
			}
			if(pour > (process-speed))
				translation = addTranslation(translation, worldRenderer, 0, 1.6875f-h, 0);
			if(pour > speed)
			{
				float h2 = (pour > (process-speed)?.625f: pour/(process-speed)*.625f);
				translation = addTranslation(translation, worldRenderer, 0, -1.6875f, 0);
				ClientUtils.renderTexturedBox(worldRenderer, .125, 0, .125, .875, h2, .875, hotMetal_still, false);
				translation = addTranslation(translation, worldRenderer, 0, 1.6875f, 0);
			}
			worldRenderer.setTranslation(0, 0, 0);
			tessellator.draw();
			setLightmapDisabled(false);
			GlStateManager.enableLighting();
		}
		GlStateManager.popMatrix();
	}

	private Vec3d addTranslation(Vec3d tmp, BufferBuilder bb, float x, float y, float z)
	{
		Vec3d ret = tmp.add(x, y, z);
		bb.setTranslation(ret.x, ret.y, ret.z);
		return ret;
	}
}