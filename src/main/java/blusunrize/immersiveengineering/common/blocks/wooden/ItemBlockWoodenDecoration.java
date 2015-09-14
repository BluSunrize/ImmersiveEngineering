package blusunrize.immersiveengineering.common.blocks.wooden;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockWoodenDecoration extends ItemBlockIEBase
{
	public ItemBlockWoodenDecoration(Block b)
	{
		super(b);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
		if(stack.getItemDamage()==0)
			list.add("This item is deprecated. Hold it in your inventory to update it.");
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean hand)
	{
		if(ent instanceof EntityPlayer)
		{
			int meta = stack.getItemDamage();
			if(meta==0)
				((EntityPlayer)ent).inventory.setInventorySlotContents(slot, new ItemStack(IEContent.blockTreatedWood, stack.stackSize));
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(stack.getItemDamage()!=2)
			return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
		Block block = world.getBlock(x, y, z);
		int x0=x;
		int y0=y;
		int z0=z;
		int x1=x+(side==4?-1: side==5?1: 0);
		int y1=y+(side==0?-1: side==1?1: 0);
		int z1=z+(side==2?-1: side==3?1: 0);

		if (block == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1)
			side = 1;
		else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z))
		{
			if (side == 0)
				--y;
			if (side == 1)
				++y;
			if (side == 2)
				--z;
			if (side == 3)
				++z;
			if (side == 4)
				--x;
			if (side == 5)
				++x;
		}

		int metaStack = this.getMetadata(stack.getItemDamage());
		int meta = this.field_150939_a.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, metaStack);
		boolean metaMod=false;
		if(this.field_150939_a.equals(world.getBlock(x1,y1,z1)) && (world.getBlockMetadata(x,y,z)==2||world.getBlockMetadata(x,y,z)==3))
			metaMod = world.setBlockMetadataWithNotify(x, y, z, 4, 0x3);

		if(this.field_150939_a.equals(world.getBlock(x0,y0,z0)))
			if(side==1&&world.getBlockMetadata(x0,y0,z0)==2)
				metaMod = world.setBlockMetadataWithNotify(x0, y0, z0, 4, 0x3);
			else if(side==0&&world.getBlockMetadata(x0,y0,z0)==3)
				metaMod = world.setBlockMetadataWithNotify(x0, y0, z0, 4, 0x3);
		if(metaMod)
		{
			world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), this.field_150939_a.stepSound.func_150496_b(), (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F, this.field_150939_a.stepSound.getPitch() * 0.8F);
			--stack.stackSize;
			return true;
		}
		if(side==0 || (side!=1&&hitY>=.5))
			meta=3;

		if (stack.stackSize == 0)
			return false;
		else if (!player.canPlayerEdit(x, y, z, side, stack))
			return false;
		else if (y == 255 && this.field_150939_a.getMaterial().isSolid())
			return false;
		else if (world.canPlaceEntityOnSide(this.field_150939_a, x, y, z, false, side, player, stack))
		{
			if (placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta))
			{
				world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), this.field_150939_a.stepSound.func_150496_b(), (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F, this.field_150939_a.stepSound.getPitch() * 0.8F);
				--stack.stackSize;
			}

			return true;
		}
		else
			return false;
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
		if(ret && world.getTileEntity(x, y, z) instanceof TileEntityWallmount)
		{
			int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
			((TileEntityWallmount)world.getTileEntity(x, y, z)).facing = f;
			((TileEntityWallmount)world.getTileEntity(x, y, z)).inverted = side==1?false: side==0?true: hitY>.5;
			if(side<2)
				((TileEntityWallmount)world.getTileEntity(x, y, z)).sideAttached = side+1;
		}
		return ret;
	}
}