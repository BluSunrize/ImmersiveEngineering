package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class TileRenderWindmill extends TileEntitySpecialRenderer<TileEntityWindmill>
{
	private List<BakedQuad>[] quads = new List[9];
	private static WeakHashMap<TileRenderWindmill, Boolean> instances = new WeakHashMap<>();
	{
		instances.put(this, true);
	}
	@Override
	public void render(TileEntityWindmill tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	//	public void renderTileEntityFast(TileEntityWindmill tile, double x, double y, double z, float partialTicks, int destroyStage, BufferBuilder BufferBuilder)
	{
		if(!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		if(quads[tile.sails]==null)
		{
			IBlockState state = getWorld().getBlockState(blockPos);
			state = state.getActualState(getWorld(), blockPos);
			state = state.withProperty(IEProperties.FACING_ALL, EnumFacing.NORTH);
			IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
			if(state instanceof IExtendedBlockState)
			{
				List<String> parts = new ArrayList<>();
				parts.add("base");
				for(int i=1; i<=tile.sails; i++)
					parts.add("sail_"+i);
				state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(parts, true));
			}
			quads[tile.sails] = model.getQuads(state, null, 0);
		}
		Tessellator tessellator = Tessellator.getInstance();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + .5, z + .5);

		float dir = tile.facing == EnumFacing.SOUTH ? 0 : tile.facing == EnumFacing.NORTH ? 180 : tile.facing == EnumFacing.EAST ? 90 : -90;
		float rot = 360 * (tile.rotation + (!tile.canTurn || tile.rotation == 0 ? 0 : partialTicks)*tile.perTick);

		GlStateManager.rotate(rot, tile.facing.getAxis() == Axis.X ? 1 : 0, 0, tile.facing.getAxis() == Axis.Z ? 1 : 0);
		GlStateManager.rotate(dir, 0, 1, 0);

		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		ClientUtils.renderModelTESRFast(quads[tile.sails], worldRenderer, tile.getWorld(), blockPos);
		worldRenderer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}
	public static void reset()
	{
		for (TileRenderWindmill r:instances.keySet())
			for(int i=0; i<r.quads.length; i++)
				r.quads[i] = null;
	}
}