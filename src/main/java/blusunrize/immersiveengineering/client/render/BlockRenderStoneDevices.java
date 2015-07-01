package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockRenderStoneDevices implements ISimpleBlockRenderingHandler
{
	public static int renderPass = 0;
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		renderer.setRenderBoundsFromBlock(block);
		ClientUtils.drawInventoryBlock(block, metadata, renderer);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(world.getBlockMetadata(x,y,z)==4 && renderPass==1)
		{
			block.setBlockBoundsBasedOnState(world, x, y, z);
			renderer.setRenderBoundsFromBlock(block);
			return renderer.renderStandardBlock(block, x, y, z);
		}
		else if(world.getBlockMetadata(x,y,z)!=4 && renderPass==0)
		{
			block.setBlockBoundsBasedOnState(world, x, y, z);
			renderer.setRenderBoundsFromBlock(block);
			if(world.getTileEntity(x,y,z) instanceof TileEntityCokeOven && ((TileEntityCokeOven)world.getTileEntity(x,y,z)).formed)
			{
				int[] off = ((TileEntityCokeOven)world.getTileEntity(x,y,z)).offset;
				int f = ((TileEntityCokeOven)world.getTileEntity(x,y,z)).facing;
				if(off[1]==0)
					if(f<4 && off[0]==0 && Math.abs(off[2])==1)
						renderer.setRenderBounds(-1,-1,-1, 2,2,2);
					else if(f>3 && off[2]==0&&Math.abs(off[0])==1)
						renderer.setRenderBounds(-1,-1,-1, 2,2,2);
			}			
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
