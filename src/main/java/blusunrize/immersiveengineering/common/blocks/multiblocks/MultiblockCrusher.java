package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;

public class MultiblockCrusher implements IMultiblock
{
	public static MultiblockCrusher instance = new MultiblockCrusher();
	static ItemStack[][][] structure = new ItemStack[3][5][3];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<5;l++)
				for(int w=0;w<3;w++)
				{
					if(l==0)
					{
						if(h<2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering);
					}
					else if(l==4)
					{
						if(h<1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding);
						else if(h<2&&w<2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering);
					}
					else if(h==0)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1, (l==2&&(w==0||w==1))?BlockMetalDecoration.META_lightEngineering: BlockMetalDecoration.META_scaffolding);
					else if(h==1)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1, (w==1&&l==2)?BlockMetalDecoration.META_lightEngineering: BlockMetalDecoration.META_fence);
					else if(h==2)
						structure[h][l][w] = new ItemStack(Blocks.hopper);
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
		return b==IEContent.blockMetalDecoration && (meta==BlockMetalDecoration.META_fence);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side==0||side==1)
			return false;
		int startX=x;
		int startY=y;
		int startZ=z;

		for(int l=0;l<3;l++)
			for(int w=-2;w<=2;w++)
				for(int h=-1;h<=(w==-2||w==2?0:1);h++)
				{
					int xx = startX+ (side==4?l: side==5?-l: side==2?-w : w);
					int yy = startY+ h;
					int zz = startZ+ (side==2?l: side==3?-l: side==5?-w : w);

					if(w==-2)
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_lightEngineering))
							return false;
					}
					else if(w==2 && h==0)
					{
						if(l<2 && !(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_lightEngineering))
							return false;
					}
					else if(h==-1)
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==((w==0&&(l==0||l==1))?BlockMetalDecoration.META_lightEngineering: BlockMetalDecoration.META_scaffolding) ))
							return false;
					}
					else if(h==0)
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==((l==1&&w==0)?BlockMetalDecoration.META_lightEngineering: BlockMetalDecoration.META_fence) ))
							return false;
					}
					else if(h==1)
					{
						if(!world.getBlock(xx, yy, zz).equals(Blocks.hopper))
							return false;
					}
				}

		for(int l=0;l<3;l++)
			for(int w=-2;w<=2;w++)
				for(int h=-1;h<=(w==-2||w==2?0:1);h++)
				{
					if(h==0&&w==2&&l==2)
						continue;
					int xx = startX+ (side==4?l: side==5?-l: side==2?-w : w);
					int yy = startY+ h;
					int zz = startZ+ (side==2?l: side==3?-l: side==5?-w : w);

					world.setBlock(xx, yy, zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_crusher, 0x3);
					if(world.getTileEntity(xx, yy, zz) instanceof TileEntityCrusher)
					{
						TileEntityCrusher tile = (TileEntityCrusher)world.getTileEntity(xx,yy,zz);
						tile.facing=side;
						tile.formed=true;
						tile.pos = l*15 + (h+1)*5 + (w+2);
						tile.offset = new int[]{(side==4?l-1: side==5?1-l: side==2?-w: w),h+1,(side==2?l-1: side==3?1-l: side==5?-w: w)};
					}
				}
		return true;
	}

}
