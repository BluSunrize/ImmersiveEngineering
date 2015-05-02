package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;

public class MultiblockDieselGenerator implements IMultiblock
{
	public static MultiblockDieselGenerator instance = new MultiblockDieselGenerator();

	static ItemStack[][][] structure = new ItemStack[3][5][3];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<5;l++)
				for(int w=0;w<3;w++)
					if(h!=2 || l!=0)
					{
						int m = l==0?BlockMetalDecoration.META_generator: l==4?BlockMetalDecoration.META_radiator: BlockMetalDecoration.META_heavyEngineering;
						structure[h][l][w]=new ItemStack(IEContent.blockMetalDecoration,1,m);
					}
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalDecoration && (meta==BlockMetalDecoration.META_radiator||meta==BlockMetalDecoration.META_generator);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		int f = ForgeDirection.getOrientation(side).getOpposite().ordinal();
		if(f==0||f==1)
			return false;

		int startX=x;
		int startY=y;
		int startZ=z;
		if(world.getBlockMetadata(x, y, z)==BlockMetalDecoration.META_generator)
		{
			startX += (f==5?4: f==4?-4: 0);
			startZ += (f==3?4: f==2?-4: 0);

			f= (f==2?3: f==3?2: f==4?5: 4);
		}

		for(int l=0;l<5;l++)
			for(int w=-1;w<=1;w++)
				for(int h=-1;h<=(l==4?0:1);h++)
				{
					int xx = startX+ (side==4?l: side==5?-l: side==2?-w : w);
					int yy = startY+ h;
					int zz = startZ+ (side==2?l: side==3?-l: side==5?-w : w);
					if(l==0)
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_radiator))
							return false;
					}
					else if(l==4)
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_generator))
							return false;
					}
					else
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_heavyEngineering))
							return false;
					}
				}


		for(int l=0;l<5;l++)
			for(int w=-1;w<=1;w++)
				for(int h=-1;h<=(l==4?0:1);h++)
				{
					int xx = (side==4?l: side==5?-l: side==2?-w : w);
					int yy = h;
					int zz = (side==2?l: side==3?-l: side==5?-w : w);

					world.setBlock(startX+xx, startY+yy, startZ+zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_dieselGenerator, 3);
					if(world.getTileEntity(startX+xx, startY+yy, startZ+zz) instanceof TileEntityDieselGenerator)
					{
						TileEntityDieselGenerator tile = (TileEntityDieselGenerator)world.getTileEntity(startX+xx,startY+yy,startZ+zz);
						tile.facing=f;
						tile.formed=true;
						tile.pos = l*9 + (h+1)*3 + (w+1);
						tile.offset = new int[]{(f==5?(l-3): f==4?(3-l): w),h,(f==3?(l-3): f==2?(3-l): w)};
					}
				}
		return true;
	}

}
