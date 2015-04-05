package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockRenderMetalDecoration implements ISimpleBlockRenderingHandler
{
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		try{

			if(metadata==0)
			{
				GL11.glTranslatef(-.5f,-.5f,-.5f);
				renderer.setRenderBounds(0,0,.375, .25,1,.625);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.setRenderBounds(.75,0,.375, 1,1,.625);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.setRenderBounds(-.125,.8125,.4375, 1.125,.9375,.5625);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.setRenderBounds(-.125,.3125,.4375, 1.125,.4375,.5625);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				GL11.glTranslatef(.5f,.5f,.5f);
			}
			else if(metadata==1)
			{
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				renderer.setRenderBoundsFromBlock(block);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
			else if(metadata==2)
			{
				renderer.setRenderBounds(.3125f,0,.3125f, .6875f,.125f,.6875f);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
				renderer.setRenderBounds(.25f,.125f,.25f, .75f,.8125f,.75f);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(world.getBlockMetadata(x, y, z)==0)
		{
			renderer.setRenderBounds(.375,0,.375, .625,1,.625);
			renderer.renderStandardBlock(block, x, y, z);
			BlockMetalDecoration md = (BlockMetalDecoration)block;

			if(md.canConnectFenceTo(world, x+1, y, z))
			{
				renderer.setRenderBounds(.625,.375,.4375, 1,.5625,.5625);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(.625,.75,.4375, 1,.9375,.5625);
				renderer.renderStandardBlock(block, x, y, z);
			}
			if(md.canConnectFenceTo(world, x-1, y, z))
			{
				renderer.setRenderBounds(0,.375,.4375, .375,.5625,.5625);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(0,.75,.4375, .375,.9375,.5625);
				renderer.renderStandardBlock(block, x, y, z);
			}
			if(md.canConnectFenceTo(world, x, y, z+1))
			{
				renderer.setRenderBounds(.4375,.375,.625, .5625,.5625,1);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(.4375,.75,.625, .5625,.9375,1);
				renderer.renderStandardBlock(block, x, y, z);
			}
			if(md.canConnectFenceTo(world, x, y, z-1))
			{
				renderer.setRenderBounds(.4375,.375,0, .5625,.5625,.375);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(.4375,.75,0, .5625,.9375,.375);
				renderer.renderStandardBlock(block, x, y, z);
			}
			return true;
		}
		else if(world.getBlockMetadata(x, y, z)==1)
		{
			renderer.setRenderBoundsFromBlock(block);
			renderer.renderFromInside=true;
			renderer.renderMinX+=.015625;
			renderer.renderMinY+=.015625;
			renderer.renderMinZ+=.015625;
			renderer.renderMaxX-=.015625;
			renderer.renderMaxY-=.015625;
			renderer.renderMaxZ-=.015625;
			renderer.renderStandardBlock(block, x, y, z);
			renderer.renderMinX-=.015625;
			renderer.renderMinY-=.015625;
			renderer.renderMinZ-=.015625;
			renderer.renderMaxX+=.015625;
			renderer.renderMaxY+=.015625;
			renderer.renderMaxZ+=.015625;
			renderer.renderFromInside=false;
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(world.getBlockMetadata(x, y, z)==2)
		{
			if(world.isAirBlock(x,y-1,z)&&!world.isAirBlock(x,y+1,z))
			{
				renderer.uvRotateWest = 3;
				renderer.uvRotateEast = 3;
				renderer.uvRotateNorth = 3;
				renderer.uvRotateSouth = 3;
				renderer.setRenderBounds(.3125f,.875f,.3125f, .6875f,1,.6875f);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(.25f,.1875f,.25f, .75f,.875f,.75f);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.uvRotateWest = 0;
				renderer.uvRotateEast = 0;
				renderer.uvRotateNorth = 0;
				renderer.uvRotateSouth = 0;
			}
			else
			{
				renderer.setRenderBounds(.3125f,0,.3125f, .6875f,.125f,.6875f);
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBounds(.25f,.125f,.25f, .75f,.8125f,.75f);
				renderer.renderStandardBlock(block, x, y, z);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelID)
	{
		return true;
	}
	@Override
	public int getRenderId()
	{
		return renderID;
	}

}
