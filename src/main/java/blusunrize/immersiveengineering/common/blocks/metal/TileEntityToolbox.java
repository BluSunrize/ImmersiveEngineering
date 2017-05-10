package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.items.ItemInternalStorage;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public class TileEntityToolbox extends TileEntityIEBase implements IDirectionalTile, IBlockBounds, IIEInventory, IGuiTile, ITileDrop, IPlayerInteraction
{
	NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
	public String name;
	private EnumFacing facing = EnumFacing.NORTH;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		if(nbt.hasKey("name"))
			this.name = nbt.getString("name");
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 27);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		if(this.name!=null)
			nbt.setString("name", this.name);
		if(!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			if(!getWorld().isRemote)
			{
				EntityItem entityitem = new EntityItem(getWorld(), getPos().getX()+.5,getPos().getY()+.5,getPos().getZ()+.5, getTileDrop(player, getWorld().getBlockState(getPos())));
				entityitem.setDefaultPickupDelay();
				getWorld().setBlockToAir(getPos());
				getWorld().spawnEntity(entityitem);
			}
			return true;
		}
		return false;
	}

	@Override
	@Nullable
	public ITextComponent getDisplayName()
	{
		return name!=null?new TextComponentString(name) : new TextComponentTranslation("item.immersiveengineering.toolbox.name");
	}

	@Override
	public boolean canOpenGui()
	{
		return true;
	}
	@Override
	public int getGuiID()
	{
		return Lib.GUIID_ToolboxBlock;
	}
	@Override
	public TileEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(!stack.isEmpty())
		{
			if(OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0, 1, 0), stack, true))
				return false;
			if(OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0, 1, 5), stack, true))
				return false;
		}
		return true;
	}
	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}
	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(IEContent.itemToolbox);
		((ItemInternalStorage)IEContent.itemToolbox).setContainedItems(stack, inventory);
		if(this.name!=null)
			stack.setStackDisplayName(this.name);
		return stack;
	}
	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if(stack.getItem() instanceof ItemInternalStorage)
		{
			this.inventory = ((ItemInternalStorage)stack.getItem()).getContainedItems(stack);
			if(stack.hasDisplayName())
				this.name = stack.getDisplayName();
		}
	}
	@Override
	public boolean preventInventoryDrop()
	{
		return true;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}
	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}
	@Override
	public int getFacingLimitation()
	{
		return 2;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}
	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	private static final float[] boundsZ = {.125f,0,.25f,.875f,.625f,.75f};
	private static final float[] boundsX = {.25f,0,.125f,.75f,.625f,.875f};
	@Override
	public float[] getBlockBounds()
	{
		return facing.getAxis()==Axis.Z?boundsZ:boundsX;
	}
}