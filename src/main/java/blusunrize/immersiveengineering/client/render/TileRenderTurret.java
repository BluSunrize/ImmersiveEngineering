/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurret;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurretGun;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class TileRenderTurret extends TileEntitySpecialRenderer<TileEntityTurret>
{
	@Override
	public void render(TileEntityTurret tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;

		//Grab model + correct eextended state
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=IEContent.blockMetalDevice1)
			return;
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

		//Initialize Tesselator and BufferBuilder
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		//Outer GL Wrapping, initial translation
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);

		GlStateManager.rotate(tile.rotationYaw, 0, 1, 0);
		GlStateManager.rotate(tile.rotationPitch, tile.facing.getZOffset(), 0, -tile.facing.getXOffset());

		renderModelPart(blockRenderer, tessellator, worldRenderer, tile.getWorld(), state, model, tile.getPos(), true, "gun");
		if(tile instanceof TileEntityTurretGun)
		{
			if(((TileEntityTurretGun)tile).cycleRender > 0)
			{
				float cycle = 0;
				if(((TileEntityTurretGun)tile).cycleRender > 3)
					cycle = (5-((TileEntityTurretGun)tile).cycleRender)/2f;
				else
					cycle = ((TileEntityTurretGun)tile).cycleRender/3f;

				GlStateManager.translate(-tile.facing.getXOffset()*cycle*.3125, 0, -tile.facing.getZOffset()*cycle*.3125);
			}
			renderModelPart(blockRenderer, tessellator, worldRenderer, tile.getWorld(), state, model, tile.getPos(), false, "action");
		}

		GlStateManager.popMatrix();
	}

	public static void renderModelPart(final BlockRendererDispatcher blockRenderer, Tessellator tessellator, BufferBuilder worldRenderer, World world, IBlockState state, IBakedModel model, BlockPos pos, boolean isFirst, String... parts)
	{
		pos = pos.up();
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(Arrays.asList(parts), true));

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, 0, -.5);
		long randomLong = MathHelper.getPositionRandom(pos);
		int light = world.getCombinedLight(pos, 0);
		List<BakedQuad> quads = model.getQuads(state, null, randomLong);
		ClientUtils.renderModelTESRFancy(quads, worldRenderer, world, pos, !isFirst);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

}