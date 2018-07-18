/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IESmartObjModel;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class TileRenderBucketWheel extends TileEntitySpecialRenderer<TileEntityBucketWheel>
{
	private static IBakedModel model = null;

	@Override
	public void render(TileEntityBucketWheel tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(!tile.formed||!tile.getWorld().isBlockLoaded(tile.getPos(), false)||tile.isDummy())
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = tile.getWorld().getBlockState(tile.getPos());
		if(state.getBlock()!=IEContent.blockMetalMultiblock)
			return;
		if(model==null)
		{
			state = state.withProperty(IEProperties.DYNAMICRENDER, true);
			state = state.withProperty(IEProperties.FACING_HORIZONTAL, EnumFacing.NORTH);
			model = blockRenderer.getModelForState(state);
		}
		OBJState objState = null;
		HashMap<String, String> texMap = new HashMap<>();
		if(state instanceof IExtendedBlockState)
		{
			ArrayList<String> list = Lists.newArrayList("bucketWheel");
			synchronized(tile.digStacks)
			{
				for(int i = 0; i < tile.digStacks.size(); i++)
					if(!tile.digStacks.get(i).isEmpty())
					{
						list.add("dig"+i);
						Block b = Block.getBlockFromItem(tile.digStacks.get(i).getItem());
						IBlockState digState = b!=Blocks.AIR?b.getStateFromMeta(tile.digStacks.get(i).getMetadata()): Blocks.COBBLESTONE.getDefaultState();
						IBakedModel digModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(digState);
						if(digModel!=null&&digModel.getParticleTexture()!=null)
							texMap.put("dig"+i, digModel.getParticleTexture().getIconName());
					}
			}
			objState = new OBJState(list, true);
		}

		Tessellator tessellator = Tessellator.getInstance();
		GlStateManager.pushMatrix();

		GlStateManager.translate(x+.5, y+.5, z+.5);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		EnumFacing facing = tile.facing;
		if(tile.mirrored)
		{
			GlStateManager.scale(facing.getAxis()==Axis.X?-1: 1, 1, facing.getAxis()==Axis.Z?-1: 1);
			GlStateManager.disableCull();
		}
		float dir = tile.facing==EnumFacing.SOUTH?90: tile.facing==EnumFacing.NORTH?-90: tile.facing==EnumFacing.EAST?180: 0;
		GlStateManager.rotate(dir, 0, 1, 0);
		float rot = tile.rotation+(float)(tile.active?IEConfig.Machines.excavator_speed*partialTicks: 0);
		GlStateManager.rotate(rot, 1, 0, 0);

		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		BufferBuilder worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		List<BakedQuad> quads;
		if(model instanceof IESmartObjModel)
			quads = ((IESmartObjModel)model).getQuads(state, null, 0, objState, texMap, true);
		else
			quads = model.getQuads(state, null, 0);
		ClientUtils.renderModelTESRFast(quads, worldRenderer, tile.getWorld(), tile.getPos());
		worldRenderer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		if(tile.mirrored)
		{
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
	//		GlStateManager.translate(x+.5, y+.5, z+.5);
	//		GL11.glRotatef(wheel.facing==3?180: wheel.facing==5?-90: wheel.facing==4?90: 0, 0,1,0);
	//
	//		if(wheel.mirrored)
	//		{
	//			GlStateManager.scale(1,1,-1);
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
	//			GlStateManager.scale(1,1,-1);
	//			GL11.glEnable(GL11.GL_CULL_FACE);
	//		}
	//
	//		GlStateManager.popMatrix();
	//	}

}