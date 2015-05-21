package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockRenderWoodenDevices implements ISimpleBlockRenderingHandler
{
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		try{
			if(metadata==0)
			{
				GL11.glScalef(.5f, .33f, .5f);
				GL11.glTranslatef(-.5f, -2F, -.5f);
				Tessellator.instance.startDrawingQuads();
				ClientUtils.handleStaticTileRenderer(new TileEntityWoodenPost());
				Tessellator.instance.draw();
			}
			else if(metadata==1)
			{
				GL11.glScalef(.25f, .25f, .25f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityWatermill(), 0.0D, 0.0D, 0.0D, 0.0F);
			}
			else if(metadata==2)
			{
				GL11.glScalef(.125f, .125f, .125f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityWindmill(), 0.0D, 0.0D, 0.0D, 0.0F);
			}
			else if(metadata==3)
			{
				GL11.glScalef(.125f, .125f, .125f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(new TileEntityWindmillAdvanced(), 0.0D, 0.0D, 0.0D, 0.0F);
			}
			else if(metadata==4)
			{
				renderer.setRenderBounds(0,0,0, 1,1,1);
				ClientUtils.drawInventoryBlock(block, metadata, renderer);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		GL11.glEnable(32826);
		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(world.getBlockMetadata(x, y, z) == 0)
		{
			TileEntityWoodenPost tile = (TileEntityWoodenPost)world.getTileEntity(x, y, z);
			if(tile.type==0)
			{
				ClientUtils.handleStaticTileRenderer(tile);
				return true;
			}
		}
		if(world.getBlockMetadata(x, y, z) == 4)
		{
			renderer.setRenderBoundsFromBlock(block);
			return renderer.renderStandardBlock(block, x, y, z);
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
