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
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillTileEntity;
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
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

//TODO maybe replace with Forge animations?
public class WindmillRenderer extends TileEntityRenderer<WindmillTileEntity>
{
	private static List<BakedQuad>[] quads = new List[9];
	public static DynamicModel<Void> MODEL;

	@Override
	public void render(WindmillTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		if(quads[tile.sails]==null)
		{
			BlockState state = getWorld().getBlockState(blockPos);
			if(state.getBlock()!=WoodenDevices.windmill)
				return;
			state = state.with(IEProperties.FACING_HORIZONTAL, Direction.NORTH);
			IBakedModel model = this.MODEL.get(null);
			List<String> parts = new ArrayList<>();
			parts.add("base");
			for(int i = 1; i <= tile.sails; i++)
				parts.add("sail_"+i);
			IModelData data = new SinglePropertyModelData<>(
					new IEObjState(VisibilityList.show(parts)), IEProperties.Model.IE_OBJ_STATE);
			quads[tile.sails] = model.getQuads(state, null, Utils.RAND, data);
		}
		Tessellator tessellator = Tessellator.getInstance();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);

		float dir = tile.getFacing()==Direction.SOUTH?0: tile.getFacing()==Direction.NORTH?180: tile.getFacing()==Direction.EAST?90: -90;
		float rot = 360*(tile.rotation+(!tile.canTurn||tile.rotation==0?0: partialTicks)*tile.perTick);

		GlStateManager.rotatef(rot, tile.getFacing().getAxis()==Axis.X?1: 0, 0, tile.getFacing().getAxis()==Axis.Z?1: 0);
		GlStateManager.rotatef(dir, 0, 1, 0);

		RenderHelper.disableStandardItemLighting();
		Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		ClientUtils.renderModelTESRFast(quads[tile.sails], worldRenderer, tile.getWorldNonnull(), blockPos);
		worldRenderer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	public static void reset()
	{
		for(int i = 0; i < quads.length; i++)
			quads[i] = null;
	}
}