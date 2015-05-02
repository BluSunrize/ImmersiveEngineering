package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;

public class MultiblockCokeOven implements IMultiblock
{
	public static MultiblockCokeOven instance = new MultiblockCokeOven();
	
	static ItemStack[][][] structure = new ItemStack[3][3][3];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<3;l++)
				for(int w=0;w<3;w++)
					structure[h][l][w]=new ItemStack(IEContent.blockStoneDevice,1,1);
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
		
//		ItemStack[][][] ss = new ItemStack[2][4][4];
//		for(int i=0; i<16; i++)
//		{
//			ss[0][i/4][i%4] = new ItemStack(Blocks.wool,1,i);
//			ss[1][i/4][i%4] = new ItemStack(Blocks.wool,1,i);
//		}
//
//		ItemStack r = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_radiator);
//		ItemStack e = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_engine);
//		ItemStack g = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_generator);
//		return new ItemStack[][][] {
//				{
//					{g,g,g},
//					{e,e,e},
//					{e,e,e},
//					{e,e,e},
//					{r,r,r}
//				},
//				{
//					{g,g,g},
//					{e,e,e},
//					{e,e,e},
//					{e,e,e},
//					{r,r,r}
//				},
//				{
//					{null,null,null},
//					{e,e,e},
//					{e,e,e},
//					{e,e,e},
//					{r,r,r}
//				}
//		};


//						return new ItemStack[][][]{ { {s,s,s},{s,s,s},{s,s,s} }, { {s,s,s},{s,s,s},{s,s,s} }, { {s,s,s},{s,s,s},{s,s,s} } };
//				return ss;
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockStoneDevice && (meta==1);
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
					if((yy!=0||xx!=0||zz!=0) && !(world.getTileEntity(x+xx, y+yy, z+zz) instanceof TileEntityCokeOven && !((TileEntityCokeOven)world.getTileEntity(x+xx, y+yy, z+zz)).formed))
						return false;

		for(int yy=-1;yy<=1;yy++)
			for(int xx=xMin;xx<=xMax;xx++)
				for(int zz=zMin;zz<=zMax;zz++)
				{
					((TileEntityCokeOven)world.getTileEntity(x+xx, y+yy, z+zz)).offset=new int[]{xx,yy,zz};
					((TileEntityCokeOven)world.getTileEntity(x+xx, y+yy, z+zz)).facing=f;
					((TileEntityCokeOven)world.getTileEntity(x+xx, y+yy, z+zz)).formed=true;
					world.getTileEntity(x+xx, y+yy, z+zz).markDirty();
					world.markBlockForUpdate(x+xx,y+yy,z+zz);
				}
		return true;
	}

}
