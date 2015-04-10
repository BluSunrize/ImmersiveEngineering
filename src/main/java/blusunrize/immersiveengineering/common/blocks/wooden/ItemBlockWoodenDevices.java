package blusunrize.immersiveengineering.common.blocks.wooden;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;

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
			list.add(StatCollector.translateToLocal("desc.ImmersiveEngineering.flavour.crate"));
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;

		if(meta==0)
			for(int i=0;i<=3;i++)
				if(!player.canPlayerEdit(x,y+1,z, side, stack) || !world.isAirBlock(x,y+i,z))
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


		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		if(ret && world.getTileEntity(x, y, z) instanceof TileEntityWoodenPost)
		{
			for(int i=1; i<=3; i++)
			{
				world.setBlock(x, y+i, z, field_150939_a, meta, 0x3);
				if(world.getTileEntity(x, y+i, z) instanceof TileEntityWoodenPost)
					((TileEntityWoodenPost)world.getTileEntity(x, y+i, z)).type=(byte) i;
			}
		}
		if(ret && world.getTileEntity(x, y, z) instanceof TileEntityWatermill)
		{
			((TileEntityWatermill)world.getTileEntity(x,y,z)).facing=f;
			for(int yy=-2;yy<=2;yy++)
			{
				int r=yy<-1||yy>1?1:2;
				for(int ww=-r;ww<=r;ww++)
					if(yy!=0||ww!=0)
					{
						world.setBlock(x+((f==2||f==3)?ww:0), y+yy, z+((f==2||f==3)?0:ww), field_150939_a, meta, 0x3);
						if(world.getTileEntity(x+((f==2||f==3)?ww:0), y+yy, z+((f==2||f==3)?0:ww)) instanceof TileEntityWatermill)
						{
							((TileEntityWatermill)world.getTileEntity(x+((f==2||f==3)?ww:0), y+yy, z+((f==2||f==3)?0:ww))).facing=f;
							((TileEntityWatermill)world.getTileEntity(x+((f==2||f==3)?ww:0), y+yy, z+((f==2||f==3)?0:ww))).offset= new int[]{ww,yy};
						}
					}
			}
		}
		if(ret && world.getTileEntity(x, y, z) instanceof TileEntityWindmill)
			((TileEntityWindmill)world.getTileEntity(x,y,z)).facing=f;

		if(ret && world.getTileEntity(x, y, z) instanceof TileEntityWoodenCrate)
		{
			if(stack.hasTagCompound())
				((TileEntityWoodenCrate)world.getTileEntity(x, y, z)).readInv(stack.getTagCompound());
		}
		return ret;
	}
}