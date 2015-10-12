package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		TileEntityDieselGenerator te = new TileEntityDieselGenerator();
		te.formed=true;
		te.pos=31;
		ClientUtils.bindAtlas(0);
		ClientUtils.tes().startDrawingQuads();
		ClientUtils.tes().setTranslation(-.5,-.5,-1.5);
		ClientUtils.handleStaticTileRenderer(te, false);
		ClientUtils.tes().setTranslation(0,0,0);
		ClientUtils.tes().draw();
		TileEntityRendererDispatcher.instance.renderTileEntityAt(te, -.5D, -.5D, -1.5D, 0.0F);
	}
	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:DieselGenrator";
	}
	
	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalDecoration && (meta==BlockMetalDecoration.META_radiator||meta==BlockMetalDecoration.META_generator);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side==0||side==1)
			return false;

		int startX=x;
		int startY=y;
		int startZ=z;
		if(world.getBlockMetadata(x, y, z)==BlockMetalDecoration.META_generator)
		{
			startX += (side==4?4: side==5?-4: 0);
			startZ += (side==2?4: side==3?-4: 0);

			side = ForgeDirection.OPPOSITES[side];
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
						{
							return false;
						}
					}
					else if(l==4)
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_generator))
						{
							return false;
						}
					}
					else
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_heavyEngineering))
						{
							return false;
						}
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
					TileEntity curr = world.getTileEntity(startX+xx, startY+yy, startZ+zz);
					if(curr instanceof TileEntityDieselGenerator)
					{
						TileEntityDieselGenerator tile = (TileEntityDieselGenerator)curr;
						tile.facing=ForgeDirection.OPPOSITES[side];
						tile.formed=true;
						tile.pos = l*9 + (h+1)*3 + (w+1);
						tile.offset = new int[]{(side==4?(l-3): side==5?(3-l): side==2?-w: w),h,(side==2?(l-3): side==3?(3-l): side==5?-w: w)};
					}
				}
		player.triggerAchievement(IEAchievements.mbDieselGen);
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(IEContent.blockMetalDecoration,6,BlockMetalDecoration.META_generator),
				new ItemStack(IEContent.blockMetalDecoration,9,BlockMetalDecoration.META_radiator),
				new ItemStack(IEContent.blockMetalDecoration,27,BlockMetalDecoration.META_heavyEngineering)};
	}
}