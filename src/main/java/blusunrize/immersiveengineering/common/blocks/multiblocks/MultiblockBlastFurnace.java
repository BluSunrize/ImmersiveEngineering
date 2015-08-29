package blusunrize.immersiveengineering.common.blocks.multiblocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;

public class MultiblockBlastFurnace implements IMultiblock
{

	public static MultiblockBlastFurnace instance = new MultiblockBlastFurnace();

	static ItemStack[][][] structure = new ItemStack[3][3][3];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<3;l++)
				for(int w=0;w<3;w++)
					structure[h][l][w]=new ItemStack(IEContent.blockStoneDecoration,1,2);
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
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockStoneDecoration && (meta==2);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
		int xMin= f==5?-2: f==4?0: -1;
		int xMax= f==5?0: f==4?2: 1;
		int zMin= f==3?-2: f==2?0: -1;
		int zMax= f==3?0: f==2?2: 1;
		for(int yy=-1;yy<=1;yy++)
			for(int xx=xMin;xx<=xMax;xx++)
				for(int zz=zMin;zz<=zMax;zz++)
					if((yy!=0||xx!=0||zz!=0) && (!world.getBlock(x+xx,y+yy,z+zz).equals(IEContent.blockStoneDecoration) || world.getBlockMetadata(x+xx,y+yy,z+zz)!=2))
						return false;

		for(int yy=-1;yy<=1;yy++)
			for(int xx=xMin;xx<=xMax;xx++)
				for(int zz=zMin;zz<=zMax;zz++)
				{
					world.setBlock(x+xx, y+yy, z+zz, IEContent.blockStoneDevice, 2, 0x3);
					if(world.getTileEntity(x+xx, y+yy, z+zz) instanceof TileEntityBlastFurnace)
					{
						((TileEntityBlastFurnace)world.getTileEntity(x+xx, y+yy, z+zz)).offset=new int[]{xx,yy,zz};
						((TileEntityBlastFurnace)world.getTileEntity(x+xx, y+yy, z+zz)).facing=f;
						((TileEntityBlastFurnace)world.getTileEntity(x+xx, y+yy, z+zz)).formed=true;
						world.getTileEntity(x+xx, y+yy, z+zz).markDirty();
					}
				}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{new ItemStack(IEContent.blockStoneDecoration,27,2)};
	}
	@Override
	public float getManualScale()
	{
		return 16;
	}
}