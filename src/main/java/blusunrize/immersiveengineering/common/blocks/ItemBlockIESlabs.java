package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockIESlabs extends ItemBlockIEBase
{
	public ItemBlockIESlabs(Block b)
	{
		super(b);
	}


	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		Block block = world.getBlock(x, y, z);
		int x0=x;
		int y0=y;
		int z0=z;
		int x1=x+(side==4?-1: side==5?1: 0);
		int y1=y+(side==0?-1: side==1?1: 0);
		int z1=z+(side==2?-1: side==3?1: 0);

		if(block==Blocks.snow_layer && (world.getBlockMetadata(x, y, z)&7)<1)
			side = 1;
		else if(block!=Blocks.vine && block!=Blocks.tallgrass && block!=Blocks.deadbush && !block.isReplaceable(world, x, y, z))
		{
			if(side == 0)
				--y;
			if(side == 1)
				++y;
			if(side == 2)
				--z;
			if(side == 3)
				++z;
			if(side == 4)
				--x;
			if(side == 5)
				++x;
		}
		TileEntityIESlab stackSlab = null;
		if((side==0||side==1) && field_150939_a.equals(world.getBlock(x0,y0,z0)) && world.getBlockMetadata(x0,y0,z0)==stack.getItemDamage())
		{
			TileEntity te = world.getTileEntity(x0,y0,z0);
			if(te instanceof TileEntityIESlab && ((TileEntityIESlab)te).slabType+side==1)
				stackSlab = ((TileEntityIESlab)te);
		}
		else if(field_150939_a.equals(world.getBlock(x1,y1,z1)) && world.getBlockMetadata(x1,y1,z1)==stack.getItemDamage())
		{
			TileEntity te = world.getTileEntity(x1,y1,z1);
			if(te instanceof TileEntityIESlab)
			{
				int type = ((TileEntityIESlab)te).slabType;
				if((type==0&&(side==0||hitY>=.5))||(type==1&&(side==1||hitY<=.5)))
					stackSlab = ((TileEntityIESlab)te);
			}
		}
		else
			return super.onItemUse(stack,player,world,x,y,z,side,hitX,hitY,hitZ);
		if(stackSlab!=null)
		{
			stackSlab.slabType=2;
			world.markBlockForUpdate(stackSlab.xCoord, stackSlab.yCoord, stackSlab.zCoord);
			world.playSoundEffect(stackSlab.xCoord+.5, stackSlab.yCoord+.5, stackSlab.zCoord+.5, this.field_150939_a.stepSound.func_150496_b(), (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F, this.field_150939_a.stepSound.getPitch() * 0.8F);
			--stack.stackSize;
			return true;
		}
		else
			return super.onItemUse(stack,player,world,x,y,z,side,hitX,hitY,hitZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		if(ret)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if(tileEntity instanceof TileEntityIESlab)
				((TileEntityIESlab) tileEntity).slabType = (side==0||(side!=1&&hitY>=.5))? 1: 0;
		}
		return ret;
	}
}