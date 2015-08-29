package blusunrize.immersiveengineering.common.blocks.multiblocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningRod;

public class MultiblockLightningRod implements IMultiblock
{
	public static MultiblockLightningRod instance = new MultiblockLightningRod();
	static ItemStack[][][] structure = new ItemStack[1][3][3];
	static{
		for(int l=0;l<3;l++)
			for(int w=0;w<3;w++)
				structure[0][l][w]=new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_lightningRod);
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}
	@Override
	public boolean overwriteBlockRender(ItemStack stack)
	{
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
	}
	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalMultiblocks && (meta==BlockMetalMultiblocks.META_lightningRod);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side!=0&&side!=1)
			return false;

		for(int xx=-1;xx<=1;xx++)
			for(int zz=-1;zz<=1;zz++)
				if(!(world.getTileEntity(x+xx, y+0, z+zz) instanceof TileEntityLightningRod) || ((TileEntityLightningRod)world.getTileEntity(x+xx, y+0, z+zz)).formed)
				{
					return false;
				}
		for(int xx=-1;xx<=1;xx++)
			for(int zz=-1;zz<=1;zz++)
			{
				TileEntityLightningRod tile = (TileEntityLightningRod)world.getTileEntity(x+xx, y+0, z+zz);
				tile.formed=true;
				tile.pos=(byte) ((xx+1)+(zz+1)*3);
				tile.offset = new int[]{xx,0,zz};
				tile.markDirty();
				world.markBlockForUpdate(x+xx,y+0,z+zz);
				world.addBlockEvent(x+xx, y+0, z+zz, IEContent.blockMetalMultiblocks, 0,1);
			}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{new ItemStack(IEContent.blockMetalMultiblocks,9,BlockMetalMultiblocks.META_lightningRod)};
	}
}