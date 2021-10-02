/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WoodenCrateTileEntity extends IEBaseTileEntity implements IIEInventory, IInteractionObjectIE, ITileDrop, IComparatorOverride
{
	NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
	public ResourceLocation lootTable;
	public String name;
	private ListTag enchantments;

	public WoodenCrateTileEntity()
	{
		super(IETileTypes.WOODEN_CRATE.get());
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
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
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(this.name!=null)
			nbt.putString("name", this.name);
		if(this.enchantments!=null&&this.enchantments.size() > 0)
			nbt.put("enchantments", this.enchantments);
		if(!descPacket)
		{
			if(lootTable!=null)
				nbt.putString("lootTable", lootTable.toString());
			else
				writeInv(nbt, false);
		}
	}

	public void writeInv(CompoundTag nbt, boolean toItem)
	{
		boolean write = false;
		ListTag invList = new ListTag();
		for(int i = 0; i < this.inventory.size(); i++)
			if(!this.inventory.get(i).isEmpty())
			{
				if(toItem)
					write = true;
				CompoundTag itemTag = new CompoundTag();
				itemTag.putByte("Slot", (byte)i);
				this.inventory.get(i).save(itemTag);
				invList.add(itemTag);
			}
		if(!toItem||write)
			nbt.put("inventory", invList);
	}

	@Override
	@Nonnull
	public Component getDisplayName()
	{
		if(name!=null)
			return new TextComponent(name);
		else
		{
			Block b = getBlockState().getBlock();
			if(b instanceof CrateBlock&&((CrateBlock)b).isReinforced())
				return new TranslatableComponent("block.immersiveengineering.reinforced_crate");
			else
				return new TranslatableComponent("block.immersiveengineering.crate");
		}
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return this;
	}

	@Nonnull
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player)
	{
		if(this.lootTable!=null)
		{
			LootTable loottable = this.level.getServer().getLootTables()
					.get(this.lootTable);
			this.lootTable = null;
			LootContext.Builder contextBuilder = new LootContext.Builder((ServerLevel)this.level);
			contextBuilder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition));
			if(player!=null)
				contextBuilder.withLuck(player.getLuck());
			LootContext context = contextBuilder.create(LootContextParamSets.CHEST);
			Random rand = new Random();

			List<ItemStack> list = loottable.getRandomItems(context);
			List<Integer> listSlots = Lists.newArrayList();
			for(int i = 0; i < inventory.size(); i++)
				if(inventory.get(i).isEmpty())
					listSlots.add(i);
			Collections.shuffle(listSlots, rand);
			if(!listSlots.isEmpty())
			{
				Utils.shuffleLootItems(list, listSlots.size(), rand);
				for(ItemStack itemstack : list)
				{
					int slot = listSlots.remove(listSlots.size()-1);
					inventory.set(slot, itemstack);
				}
				this.setChanged();
			}
		}
		return IInteractionObjectIE.super.createMenu(id, playerInventory, player);
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
	public void doGraphicalUpdates()
	{
		this.setChanged();
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundTag tag = new CompoundTag();
		writeInv(tag, true);
		if(!tag.isEmpty())
			stack.setTag(tag);
		if(this.name!=null)
			stack.setHoverName(new TextComponent(this.name));
		if(enchantments!=null&&enchantments.size() > 0)
			stack.getOrCreateTag().put("ench", enchantments);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		if(stack.hasTag())
		{
			readCustomNBT(stack.getOrCreateTag(), false);
			if(stack.hasCustomHoverName())
				this.name = stack.getHoverName().getString();
			enchantments = stack.getEnchantmentTags();
		}
	}

	@Override
	public int getComparatorInputOverride()
	{
		return Utils.calcRedstoneFromInventory(this);
	}

	private LazyOptional<IItemHandler> insertionCap = registerConstantCap(new IEInventoryHandler(27, this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionCap.cast();
		return super.getCapability(capability, facing);
	}
}