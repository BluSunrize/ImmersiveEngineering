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
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.BottlingMachineTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.BottlingMachineTileEntity.BottlingProcess;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class BottlingMachineRenderer extends TileEntityRenderer<BottlingMachineTileEntity>
{
	public static DynamicModel<Direction> DYNAMIC;

	@Override
	public void render(BottlingMachineTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		//Grab model
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.bottlingMachine)
			return;
		IBakedModel model = DYNAMIC.get(te.getFacing());

		//Initialize Tesselator and BufferBuilder
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		//Outer GL Wrapping, initial translation
		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);
		if(te.getIsMirrored())
			GlStateManager.scalef(te.getFacing().getXOffset()==0?-1: 1, 1, te.getFacing().getZOffset()==0?-1: 1);

		//Item Displacement
		float[][] itemDisplays = new float[te.bottlingProcessQueue.size()][];
		//Animations
		float lift = 0;

		for(int i = 0; i < itemDisplays.length; i++)
		{
			BottlingProcess process = te.bottlingProcessQueue.get(i);
			if(process==null||process.processTick==process.maxProcessTick)
				continue;

			//+partialTicks
			float processTimer = ((float)process.processTick)/process.maxProcessTick*120;

			float itemX = -1.5f;//-1;
			float itemY = -.15625f;// -.34375f;
			float itemZ = 1;//-.9375f;
			float itemFill = 0;//ClientUtils.mc().player.ticksExisted%100; //0f;

			if(processTimer <= 35)//slide
			{
				itemX += processTimer/35f*1.5;
			}
			else if(processTimer <= 85)//slide
			{
				itemX = 0;
				if(processTimer <= 55)
					lift = (processTimer-35)/20f*.125f;
				else if(processTimer <= 65)
				{
					lift = .125f;
					itemFill = (processTimer-55)/10f;
				}
				else
				{
					lift = (85-processTimer)/20f*.125f;
					itemFill = 1;
				}
				itemY += lift;
				lift += .0625;
			}
			else
			{
				itemX = (processTimer-85)/35f*1.5f;
				itemFill = 1;
			}
			itemDisplays[i] = new float[]{processTimer, itemX, itemY, itemZ, itemFill};

		}

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();

		GlStateManager.translated(0, lift, 0);
		renderModelPart(blockRenderer, tessellator, worldRenderer, te.getWorldNonnull(), state, model, blockPos, "lift");
		GlStateManager.translated(0, -lift, 0);

		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();

		float dir = te.getFacing()==Direction.SOUTH?180: te.getFacing()==Direction.NORTH?0: te.getFacing()==Direction.EAST?-90: 90;
		GlStateManager.rotatef(dir, 0, 1, 0);

		float scale = .0625f;
		FluidStack fs = te.tanks[0].getFluid();
		if(!fs.isEmpty())
		{
			RenderHelper.disableStandardItemLighting();
			GlStateManager.pushMatrix();
			float level = fs.getAmount()/(float)te.tanks[0].getCapacity();
			GlStateManager.translated(-.21875, .376, 1.21875);
			GlStateManager.scalef(scale, scale, scale);
			float h = level*9;
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 7, h);
			GlStateManager.rotatef(90, 0, 1, 0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 7, h);
			GlStateManager.rotatef(90, 0, 1, 0);
			GlStateManager.translated(-7, 0, 7);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 7, h);
			GlStateManager.rotatef(90, 0, 1, 0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 7, h);

			GlStateManager.rotatef(90, 1, 0, 0);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 7, 7);
			GlStateManager.translated(0, 0, -h);
			ClientUtils.drawRepeatedFluidSprite(fs, 0, 0, 7, 7);

			GlStateManager.scalef(1/scale, 1/scale, 1/scale);
			GlStateManager.translated(0, -1, -1);
			GlStateManager.popMatrix();
		}


		//DRAW ITEMS HERE
		for(int i = 0; i < itemDisplays.length; i++)
			if(itemDisplays[i]!=null)
			{
				BottlingProcess process = te.bottlingProcessQueue.get(i);
				if(process==null)
					continue;

				ItemStack display = itemDisplays[i][4]==0||process.items.get(1).isEmpty()?process.items.get(0): process.items.get(1);
				scale = .4375f;

				GlStateManager.translated(itemDisplays[i][1], itemDisplays[i][2], itemDisplays[i][3]);
				GlStateManager.scalef(scale, scale, scale);

				if(itemDisplays[i][4]==0)
					ClientUtils.mc().getItemRenderer().renderItem(process.items.get(0), TransformType.FIXED);
				else if(itemDisplays[i][4]==1||!ClientProxy.stencilBufferEnabled)
					ClientUtils.mc().getItemRenderer().renderItem(display, TransformType.FIXED);
				else
				{
					float h0 = -.5f;
					float h1 = h0+itemDisplays[i][4];

					BufferBuilder worldrenderer = tessellator.getBuffer();

					//TODO move to GlStateManager if that ever gets the stencil functions
					GL11.glEnable(GL11.GL_STENCIL_TEST);

					GlStateManager.colorMask(false, false, false, false);
					GlStateManager.depthMask(false);

					GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
					GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);

					GL11.glStencilMask(0xFF);
					GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT, true);

					GlStateManager.rotatef(90.0F-ClientUtils.mc().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

					GlStateManager.disableTexture();
					worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
					ClientUtils.renderBox(worldrenderer, -.5, h0, -.5, .5, h1, .5);
					tessellator.draw();
					GlStateManager.enableTexture();

					GlStateManager.rotatef(-(90.0F-ClientUtils.mc().getRenderManager().playerViewY), 0.0F, 1.0F, 0.0F);

					GlStateManager.colorMask(true, true, true, true);
					GlStateManager.depthMask(true);

					GL11.glStencilMask(0x00);

					GL11.glStencilFunc(GL11.GL_EQUAL, 0, 0xFF);
					ClientUtils.mc().getItemRenderer().renderItem(process.items.get(0), TransformType.FIXED);

					GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
					ClientUtils.mc().getItemRenderer().renderItem(display, TransformType.FIXED);

					GL11.glDisable(GL11.GL_STENCIL_TEST);
				}

				GlStateManager.scalef(1/scale, 1/scale, 1/scale);
				GlStateManager.translated(-itemDisplays[i][1], -itemDisplays[i][2], -itemDisplays[i][3]);
			}
		GlStateManager.popMatrix();
	}

	public static void renderModelPart(final BlockRendererDispatcher blockRenderer, Tessellator tessellator, BufferBuilder worldRenderer, World world, BlockState state, IBakedModel model, BlockPos pos, String... parts)
	{
		IModelData data = new SinglePropertyModelData<>(new IEObjState(VisibilityList.show(parts)), Model.IE_OBJ_STATE);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.color3f(1, 0, 0);
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-pos.getX(), -.5-pos.getY(), -.5-pos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(world, model, state, pos, worldRenderer, true, world.rand, 0,
				data);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
	}
}
