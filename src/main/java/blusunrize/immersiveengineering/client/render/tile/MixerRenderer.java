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
import blusunrize.immersiveengineering.common.blocks.metal.MixerTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class MixerRenderer extends TileEntityRenderer<MixerTileEntity>
{
	private final DynamicModel<Direction> dynamic = DynamicModel.createSided(
			new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/mixer_agitator.obj"),
			"mixer", ModelType.OBJ);

	@Override
	public void render(MixerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.mixer)
			return;
		IBakedModel model = dynamic.get(te.getFacing());

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);

		if(te.getIsMirrored())
			GlStateManager.scalef(te.getFacing().getXOffset()==0?-1: 1, 1, te.getFacing().getZOffset()==0?-1: 1);

		GlStateManager.pushMatrix();
		GlStateManager.translated(te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.WEST?-.5: .5, 0, te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.EAST?.5: -.5);
		float agitator = te.animation_agitator-(!te.shouldRenderAsActive()?0: (1-partialTicks)*9f);
		GlStateManager.rotatef(agitator, 0, 1, 0);

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
				getWorld().rand, 0, EmptyModelData.INSTANCE);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();

		GlStateManager.translated(te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.WEST?-.5: .5, -.625f, te.getFacing()==Direction.SOUTH||te.getFacing()==Direction.EAST?.5: -.5);
		GlStateManager.scalef(.0625f, 1, .0625f);
		GlStateManager.rotatef(90, 1, 0, 0);

		RenderHelper.disableStandardItemLighting();

		for(int i = te.tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = te.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				int col = fs.getFluid().getAttributes().getColor(fs);
				GlStateManager.color3f((col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f);

				float yy = fs.getAmount()/(float)te.tank.getCapacity()*1.125f;
				GlStateManager.translated(0, 0, -yy);
				float w = (i < te.tank.getFluidTypes()-1||yy >= .125)?26: 16+yy/.0125f;
				ClientUtils.drawRepeatedFluidSprite(fs, -w/2, -w/2, w, w);
			}
		}

		GlStateManager.popMatrix();
	}
}