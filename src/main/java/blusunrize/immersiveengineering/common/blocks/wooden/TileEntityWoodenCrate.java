/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.CapabilityHolder;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TileEntityWoodenCrate extends TileEntityIEBase implements IIEInventory, IGuiTile, ITileDrop, IComparatorOverride, ILootContainer
{
	public static TileEntityType<TileEntityWoodenCrate> TYPE;
	NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
	public ResourceLocation lootTable;
	public String name;
	private NBTTagList enchantments;
	private boolean reinforced = false;

	public TileEntityWoodenCrate()
	{
		super(TYPE);
	}

	public TileEntityWoodenCrate(boolean reinforced)
	{
		this();
		this.reinforced = reinforced;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		if(nbt.contains("name", NBT.TAG_STRING))
			this.name = nbt.getString("name");
		if(nbt.contains("enchantments", NBT.TAG_LIST))
			this.enchantments = nbt.getList("enchantments", NBT.TAG_COMPOUND);
		if(!descPacket)
		{
			if(nbt.contains("lootTable", NBT.TAG_STRING))
				this.lootTable = new ResourceLocation(nbt.getString("lootTable"));
			else
				inventory = Utils.readInventory(nbt.getList("inventory", NBT.TAG_COMPOUND), 27);
		}
		this.reinforced = nbt.getBoolean("reinforced");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		if(this.name!=null)
			nbt.setString("name", this.name);
		if(this.enchantments!=null&&this.enchantments.size() > 0)
			nbt.setTag("enchantments", this.enchantments);
		if(!descPacket)
		{
			if(lootTable!=null)
				nbt.setString("lootTable", lootTable.toString());
			else
				writeInv(nbt, false);
		}
		nbt.setBoolean("reinforced", reinforced);
	}

	public void writeInv(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = false;
		NBTTagList invList = new NBTTagList();
		for(int i = 0; i < this.inventory.size(); i++)
			if(!this.inventory.get(i).isEmpty())
			{
				if(toItem)
					write = true;
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				this.inventory.get(i).write(itemTag);
				invList.add(itemTag);
			}
		if(!toItem||write)
			nbt.setTag("inventory", invList);
	}

	//TODO @Override
	@Nullable
	public ITextComponent getDisplayName()
	{
		if(name!=null)
			return new TextComponentString(name);
		else
		{
			if(reinforced)
				return new TextComponentTranslation("tile.immersiveengineering.wooden_device0.reinforced_crate.name");
			else
				return new TextComponentTranslation("tile.immersiveengineering.wooden_device0.crate.name");
		}
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
	public void onGuiOpened(EntityPlayer player, boolean clientside)
	{
		if(this.lootTable!=null&&!clientside)
		{
			LootTable loottable = this.world.getServer().getLootTableManager()
					.getLootTableFromLocation(this.lootTable);
			this.lootTable = null;
			LootContext.Builder contextBuilder = new LootContext.Builder((WorldServer)this.world);
			if(player!=null)
				contextBuilder.withLuck(player.getLuck());
			LootContext context = contextBuilder.build();
			Random rand = new Random();

			List<ItemStack> list = loottable.generateLootForPools(rand, context);
			List<Integer> listSlots = Lists.newArrayList();
			for(int i = 0; i < inventory.size(); i++)
				if(inventory.get(i).isEmpty())
					listSlots.add(i);
			Collections.shuffle(listSlots, rand);
			if(listSlots.isEmpty())
				return;
			Utils.shuffleLootItems(list, listSlots.size(), rand);
			for(ItemStack itemstack : list)
			{
				int slot = listSlots.remove(listSlots.size()-1);
				inventory.set(slot, itemstack);
			}
			this.markDirty();
		}
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return IEApi.isAllowedInCrate(stack);
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
		ItemStack stack = new ItemStack(state.getBlock(), 1);
		NBTTagCompound tag = new NBTTagCompound();
		writeInv(tag, true);
		if(!tag.isEmpty())
			stack.setTag(tag);
		if(this.name!=null)
			stack.setDisplayName(new TextComponentString(this.name));
		if(enchantments!=null&&enchantments.size() > 0)
			ItemNBTHelper.getTag(stack).setTag("ench", enchantments);
		return stack;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if(stack.hasTag())
		{
			readCustomNBT(stack.getOrCreateTag(), false);
			if(stack.hasDisplayName())
				this.name = stack.getDisplayName().getString();
			enchantments = stack.getEnchantmentTagList();
		}
	}

	@Override
	public boolean preventInventoryDrop()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride()
	{
		return Utils.calcRedstoneFromInventory(this);
	}

	private IItemHandler insertionHandler = new IEInventoryHandler(27, this);
	private CapabilityHolder<IItemHandler> insertionCap = CapabilityHolder.ofConstant(insertionHandler);

	{
		caps.add(insertionCap);
	}
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionCap.get().cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public ResourceLocation getLootTable()
	{
		return this.lootTable;
	}
}