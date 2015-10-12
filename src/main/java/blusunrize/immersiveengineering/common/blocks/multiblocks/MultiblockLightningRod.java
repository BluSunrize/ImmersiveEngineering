package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningRod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
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
	public String getUniqueName()
	{
		return "IE:LightningRod";
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
		TileEntity[][] tes = new TileEntity[3][3];
		for(int xx=-1;xx<=1;xx++)
			for(int zz=-1;zz<=1;zz++)
			{
				tes[xx+1][zz+1] = world.getTileEntity(x+xx, y, z+zz);
				if(!(tes[xx+1][zz+1] instanceof TileEntityLightningRod) || ((TileEntityLightningRod)tes[xx+1][zz+1]).formed)
				{
					return false;
				}
			}
		for(int xx=-1;xx<=1;xx++)
			for(int zz=-1;zz<=1;zz++)
			{
				TileEntityLightningRod tile = (TileEntityLightningRod)tes[xx+1][zz+1];
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