package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityWoodenCrate extends TileEntityIEBase implements IIEInventory, IGuiTile, ITileDrop, IComparatorOverride
{
	ItemStack[] inventory = new ItemStack[27];

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 27);
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			writeInv(nbt, false);
		}
	}
	public void writeInv(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = false;
		NBTTagList invList = new NBTTagList();
		for(int i=0; i<this.inventory.length; i++)
			if(this.inventory[i] != null)
			{
				if(toItem)
					write = true;
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				this.inventory[i].writeToNBT(itemTag);
				invList.appendTag(itemTag);
			}
		if(!toItem || write)
			nbt.setTag("inventory", invList);
	}

	@Override
	public boolean canOpenGui()
	{
		return true;
	}
	@Override
	public int getGuiID()
	{
		return Lib.GUIID_WoodenCrate;
	}
	@Override
	public TileEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public ItemStack[] getInventory()
	{
		return inventory;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return stack!=null && !OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.CRATE.getMeta()), stack, true);
	}
	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}
	@Override
	public void doGraphicalUpdates(int slot)
	{
	}
	
	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		NBTTagCompound tag = new NBTTagCompound();
		writeInv(tag, true);
		if(!tag.hasNoTags())
			stack.setTagCompound(tag);
		return stack;
	}
	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if(stack.hasTagCompound())
			readCustomNBT(stack.getTagCompound(), false);
	}
	@Override
	public int getComparatorInputOverride()
	{
		return Utils.calcRedstoneFromInventory(this);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}
	IItemHandler insertionHandler = new IEInventoryHandler(27,this);
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T)insertionHandler;
		return super.getCapability(capability, facing);
	}
}