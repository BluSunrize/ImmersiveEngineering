package blusunrize.immersiveengineering.common.blocks.wooden;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.util.Lib;

public class ItemBlockWoodenDevices extends ItemBlockIEBase
{
	public ItemBlockWoodenDevices(Block b)
	{
		super(b);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
		if(stack.getItemDamage()==4)
			list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"crate"));
		if(stack.getItemDamage()==6)
		{
			if(stack.hasTagCompound())
			{
				NBTTagCompound tag = stack.getTagCompound().getCompoundTag("tank");
				if(!tag.hasKey("Empty"))
				{
					FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
					list.add(fluid.getLocalizedName()+": "+fluid.amount+"mB");
				}				
				else
				{
					list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"barrel"));
					list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"barrelTemp"));
				}
			}
			else
			{
				list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"barrel"));
				list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"barrelTemp"));
			}
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;

		if(meta==0)
			for(int i=0;i<=3;i++)
				if(!player.canPlayerEdit(x,y+1,z, side, stack) || !world.getBlock(x,y+i,z).isReplaceable(world, x,y+i,z) )
					//						|| !world.isAirBlock(x,y+i,z))
					return false;
		if(meta==1)
			for(int yy=-2;yy<=2;yy++)
			{
				int r=yy<-1||yy>1?1:2;
				for(int ww=-r;ww<=r;ww++)
					if(!world.getBlock(x+((f==2||f==3)?ww:0), y+yy, z+((f==2||f==3)?0:ww)).isReplaceable(world, x+((f==2||f==3)?ww:0), y+yy, z+((f==2||f==3)?0:ww)) )
						return false;
			}
		if(meta==2||meta==3)
			for(int yy=-6;yy<=6;yy++)
			{
				int r=Math.abs(yy)==6?1: Math.abs(yy)==5?3: Math.abs(yy)==4?4: Math.abs(yy)>1?5: 6;
				for(int ww=-r;ww<=r;ww++)
					if(!world.getBlock(x+(f<=3?ww:0), y+yy, z+(f<=3?0:ww)).isReplaceable(world, x+(f<=3?ww:0), y+yy, z+(f<=3?0:ww)) )
						return false;
			}
		if(meta==5)
		{
			if(f==2||f==3)
			{
				if(!world.getBlock(x+1,y,z).isReplaceable(world, x+1,y,z) && !world.getBlock(x-1,y,1).isReplaceable(world, x-1,y,z) )
					return false;
			}
			else
			{
				if(!world.getBlock(x,y,z+1).isReplaceable(world, x,y,z+1) && !world.getBlock(x,y,z-1).isReplaceable(world, x,y,z-1) )
					return false;
			}
		}

		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		if(!ret)
			return false;
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityWoodenPost)
		{
			TileEntity tileEntityWoodenPost;
			for(int i=1; i<=3; i++)
			{
				world.setBlock(x, y+i, z, field_150939_a, meta, 0x3);
				tileEntityWoodenPost = world.getTileEntity(x, y+i, z);
				if(tileEntityWoodenPost instanceof TileEntityWoodenPost)
					((TileEntityWoodenPost)tileEntityWoodenPost).type=(byte) i;
			}
		}
		if(tileEntity instanceof TileEntityWatermill)
		{
			((TileEntityWatermill)tileEntity).facing=f;
			TileEntity tileEntityWatermill;
			for(int yy=-2;yy<=2;yy++)
			{
				int r=yy<-1||yy>1?1:2;
				for(int ww=-r;ww<=r;ww++)
					if(yy!=0||ww!=0)
					{
						world.setBlock(x+((f==2||f==3)?ww:0), y+yy, z+((f==2||f==3)?0:ww), field_150939_a, meta, 0x3);
						tileEntityWatermill = world.getTileEntity(x+((f==2||f==3)?ww:0), y+yy, z+((f==2||f==3)?0:ww));
						if(tileEntityWatermill instanceof TileEntityWatermill)
						{
							((TileEntityWatermill)tileEntityWatermill).facing=f;
							((TileEntityWatermill)tileEntityWatermill).offset= new int[]{ww,yy};
						}
					}
			}
		}
		if(tileEntity instanceof TileEntityWindmill)
			((TileEntityWindmill)tileEntity).facing=f;

		if(tileEntity instanceof TileEntityWoodenCrate)
		{
			if(stack.hasTagCompound())
				((TileEntityWoodenCrate)tileEntity).readInv(stack.getTagCompound());
		}

		if(tileEntity instanceof TileEntityModWorkbench)
		{
			int xOff = f>3?0:(hitX<.5?-1:1);
			int zOff = f<4?0:(hitZ<.5?-1:1);
			if(!world.getBlock(x+xOff,y,z+zOff).isReplaceable(world, x+xOff,y,z+zOff))
			{
				xOff = f>3?0:(hitX>=.5?-1:1);
				zOff = f<4?0:(hitZ>=.5?-1:1);
			}
			int off = f>3?zOff:xOff;

			((TileEntityModWorkbench)tileEntity).facing=f;
			((TileEntityModWorkbench)tileEntity).dummyOffset=off;
			world.setBlock(x+xOff,y,z+zOff, field_150939_a, meta, 0x3);
			TileEntity tileEntityDummy = world.getTileEntity(x+xOff,y,z+zOff);
			if(tileEntityDummy instanceof TileEntityModWorkbench)
			{
				((TileEntityModWorkbench)tileEntityDummy).facing =f;
				((TileEntityModWorkbench)tileEntityDummy).dummy = true;
				((TileEntityModWorkbench)tileEntityDummy).dummyOffset = off;
			}

		}
		if(tileEntity instanceof TileEntityWoodenBarrel)
		{
			if(stack.hasTagCompound())
				((TileEntityWoodenBarrel)tileEntity).readTank(stack.getTagCompound());
		}
		return true;
	}
}