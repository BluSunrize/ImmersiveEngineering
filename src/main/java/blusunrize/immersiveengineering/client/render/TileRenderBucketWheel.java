package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;

public class TileRenderBucketWheel extends TileEntitySpecialRenderer<TileEntityBucketWheel>
{
	@Override
	public void renderTileEntityAt(TileEntityBucketWheel tile, double x, double y, double z, float f, int destroyStage)
	{
		if (!tile.getWorld().isBlockLoaded(tile.getPos(), false)||tile.isDummy())
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = tile.getWorld().getBlockState(tile.getPos());
		BlockPos blockPos = tile.getPos();
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getModelForState(state);
		if(state instanceof IExtendedBlockState)
		{
			ArrayList<String> list = Lists.newArrayList("bucketWheel");
			HashMap<String,String> texMap = new HashMap();
			for(int i=0; i<tile.digStacks.length; i++)
				if(tile.digStacks[i]!=null)
				{
					list.add("dig"+i);
					Block b = Block.getBlockFromItem(tile.digStacks[i].getItem());
					IBlockState digState = b!=null?b.getStateFromMeta(tile.digStacks[i].getMetadata()): Blocks.STONE.getDefaultState();
					IBakedModel digModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(digState);
					if(digModel!=null && digModel.getParticleTexture()!=null)
						texMap.put("dig"+i, digModel.getParticleTexture().getIconName());
				}
			state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(list, true));
			state = ((IExtendedBlockState)state).withProperty(IEProperties.OBJ_TEXTURE_REMAP, texMap);
		}

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldRenderer = tessellator.getBuffer();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);
		
		EnumFacing facing = state.getValue(IEProperties.FACING_HORIZONTAL);
		GlStateManager.rotate(facing==EnumFacing.NORTH?180: facing==EnumFacing.EAST?90: facing==EnumFacing.WEST?-90: 0, 0, 1, 0);

		if(tile.mirrored)
		{
			GlStateManager.scale(1,1,-1);
			GlStateManager.disableCull();
		}
		
		float rot =  tile.rotation+(float)(tile.active?Config.getDouble("excavator_speed")*f:0);
		GlStateManager.rotate(rot, 0,0,1);

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation( -.5-blockPos.getX(), -.5- blockPos.getY(),  -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		
		if(tile.mirrored)
		{
//			GlStateManager.scale(1,1,-1);
			GlStateManager.enableCull();
		}
	}

	//	@Override
	//	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	//	{
	//		TileEntityBucketWheel wheel = (TileEntityBucketWheel)tile;
	//		if(!wheel.formed || wheel.pos!=24)
	//			return;
	//		GL11.glPushMatrix();
	//
	//		GL11.glTranslated(x+.5, y+.5, z+.5);
	//		GL11.glRotatef(wheel.facing==3?180: wheel.facing==5?-90: wheel.facing==4?90: 0, 0,1,0);
	//
	//		if(wheel.mirrored)
	//		{
	//			GL11.glScalef(1,1,-1);
	//			GL11.glDisable(GL11.GL_CULL_FACE);
	//		}
	//
	//		float rot =  wheel.rotation+(float)(wheel.active?Config.getDouble("excavator_speed")*f:0);
	//		GL11.glRotatef(rot, 0,0,-1);
	//		ClientUtils.bindTexture("immersiveengineering:textures/models/bucketWheel.png");
	//		model.renderOnly("bucketWheel");
	//
	//		for(int i=0; i<8; i++)
	//		{
	//			ItemStack stack = wheel.digStacks[i];
	//			//			String ss = ClientUtils.getResourceNameForItemStack(stack);
	//			//			if(!ss.isEmpty())
	//			if(stack==null || stack.getItem()==null)
	//				continue;
	//			IIcon ic = null;
	//			Block b = Block.getBlockFromItem(stack.getItem());
	//			if(b!=null&&b!=Blocks.air)
	//				ic = b.getIcon(2, stack.getItemDamage());
	//			else
	//				ic = stack.getIconIndex();
	//			if(ic!=null)
	//			{
	//				ClientUtils.bindAtlas(stack.getItemSpriteNumber());
	//				ClientUtils.tes().startDrawingQuads();
	//				for(GroupObject go : model.groupObjects)
	//				{
	//					if(go.name.equals("dig"+i))
	//					{
	//						for(Face face : go.faces)
	//						{
	//							float minU = ic.getMinU();
	//							float sizeU = ic.getMaxU() - minU;
	//							float minV = ic.getMinV();
	//							float sizeV = ic.getMaxV() - minV;
	//
	//							TextureCoordinate[] oldUVs = new TextureCoordinate[face.textureCoordinates.length];
	//							for(int v=0; v<face.vertices.length; ++v)
	//							{
	//								oldUVs[v] = face.textureCoordinates[v]; 
	//								TextureCoordinate textureCoordinate = face.textureCoordinates[v];
	//								face.textureCoordinates[v] = new TextureCoordinate(
	//										minU + sizeU * textureCoordinate.u,
	//										minV + sizeV * textureCoordinate.v
	//										);
	//							}
	//							face.addFaceForRender(ClientUtils.tes(),0);
	//							for(int v=0; v<face.vertices.length; ++v)
	//								face.textureCoordinates[v] = new TextureCoordinate(oldUVs[v].u,oldUVs[v].v);
	////							face.textureCoordinates = oldUVs;
	//						}
	////						go.render();
	//					}
	//				}
	//				ClientUtils.tes().draw();
	//			}
	//		}
	//
	//		if(wheel.mirrored)
	//		{
	//			GL11.glScalef(1,1,-1);
	//			GL11.glEnable(GL11.GL_CULL_FACE);
	//		}
	//
	//		GL11.glPopMatrix();
	//	}

}