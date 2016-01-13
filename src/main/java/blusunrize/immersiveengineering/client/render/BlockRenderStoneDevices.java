package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBlastFurnaceAdvanced;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnaceAdvanced;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class BlockRenderStoneDevices implements ISimpleBlockRenderingHandler
{
	public static int renderPass = 0;
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		if(metadata==5)
		{
			GL11.glPushMatrix();
			GL11.glScalef(.375f, .375f, .375f);
			GL11.glRotatef(180, 0,1,0);
			MultiblockBlastFurnaceAdvanced.instance.renderFormedStructure();
			GL11.glEnable(32826);
			GL11.glPopMatrix();
		}
		else
		{
			renderer.setRenderBounds(0,0,0, 1,1,1);
			ClientUtils.drawInventoryBlock(block, metadata, renderer);
		}
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
			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof TileEntityBlastFurnaceAdvanced)
			{
				if(((TileEntityBlastFurnaceAdvanced)te).offset[0]==0&&((TileEntityBlastFurnaceAdvanced)te).offset[1]==0&&((TileEntityBlastFurnaceAdvanced)te).offset[2]==0)
				{
					ClientUtils.handleStaticTileRenderer(te);
					return true;
				}
				return false;
			}
			else
			{
				block.setBlockBoundsBasedOnState(world, x, y, z);
				renderer.setRenderBoundsFromBlock(block);
				return renderer.renderStandardBlock(block, x, y, z);
			}
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
