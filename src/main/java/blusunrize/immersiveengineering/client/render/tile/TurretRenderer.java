/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretTileEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class TurretRenderer extends TileEntityRenderer<TurretTileEntity>
{
	@Override
	public void render(TurretTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;

		//Grab model + correct eextended state
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		BlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=MetalDevices.turretChem&&state.getBlock()!=MetalDevices.turretGun)
			return;
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);

		//Initialize Tesselator and BufferBuilder
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		//Outer GL Wrapping, initial translation
		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);

		GlStateManager.rotatef(tile.rotationYaw, 0, 1, 0);
		GlStateManager.rotatef(tile.rotationPitch, tile.getFacing().getZOffset(), 0, -tile.getFacing().getXOffset());

		renderModelPart(tessellator, worldRenderer, tile.getWorldNonnull(), state, model, tile.getPos(), true, "gun");
		if(tile instanceof TurretGunTileEntity)
		{
			if(((TurretGunTileEntity)tile).cycleRender > 0)
			{
				float cycle = 0;
				if(((TurretGunTileEntity)tile).cycleRender > 3)
					cycle = (5-((TurretGunTileEntity)tile).cycleRender)/2f;
				else
					cycle = ((TurretGunTileEntity)tile).cycleRender/3f;

				GlStateManager.translated(-tile.getFacing().getXOffset()*cycle*.3125, 0, -tile.getFacing().getZOffset()*cycle*.3125);
			}
			renderModelPart(tessellator, worldRenderer, tile.getWorldNonnull(), state, model, tile.getPos(), false, "action");
		}

		GlStateManager.popMatrix();
	}

	public static void renderModelPart(Tessellator tessellator, BufferBuilder worldRenderer, World world, BlockState state, IBakedModel model, BlockPos pos, boolean isFirst, String... parts)
	{
		pos = pos.up();

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, 0, -.5);
		List<BakedQuad> quads = model.getQuads(state, null, Utils.RAND, new SinglePropertyModelData<>(
				new IEObjState(VisibilityList.show(parts)),
				Model.IE_OBJ_STATE));
		ClientUtils.renderModelTESRFancy(quads, worldRenderer, world, pos, !isFirst, -1);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

}