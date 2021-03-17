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
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
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
	private ListNBT enchantments;

	public WoodenCrateTileEntity()
	{
		super(IETileTypes.WOODEN_CRATE.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
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
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
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

	public void writeInv(CompoundNBT nbt, boolean toItem)
	{
		boolean write = false;
		ListNBT invList = new ListNBT();
		for(int i = 0; i < this.inventory.size(); i++)
			if(!this.inventory.get(i).isEmpty())
			{
				if(toItem)
					write = true;
				CompoundNBT itemTag = new CompoundNBT();
				itemTag.putByte("Slot", (byte)i);
				this.inventory.get(i).write(itemTag);
				invList.add(itemTag);
			}
		if(!toItem||write)
			nbt.put("inventory", invList);
	}

	@Override
	@Nonnull
	public ITextComponent getDisplayName()
	{
		if(name!=null)
			return new StringTextComponent(name);
		else
		{
			Block b = getBlockState().getBlock();
			if(b instanceof CrateBlock&&((CrateBlock)b).isReinforced())
				return new TranslationTextComponent("block.immersiveengineering.reinforced_crate");
			else
				return new TranslationTextComponent("block.immersiveengineering.crate");
		}
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
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
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player)
	{
		if(this.lootTable!=null)
		{
			LootTable loottable = this.world.getServer().getLootTableManager()
					.getLootTableFromLocation(this.lootTable);
			this.lootTable = null;
			LootContext.Builder contextBuilder = new LootContext.Builder((ServerWorld)this.world);
			contextBuilder.withParameter(LootParameters.ORIGIN, Vector3d.copyCentered(pos));
			if(player!=null)
				contextBuilder.withLuck(player.getLuck());
			LootContext context = contextBuilder.build(LootParameterSets.CHEST);
			Random rand = new Random();

			List<ItemStack> list = loottable.generate(context);
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
				this.markDirty();
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
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundNBT tag = new CompoundNBT();
		writeInv(tag, true);
		if(!tag.isEmpty())
			stack.setTag(tag);
		if(this.name!=null)
			stack.setDisplayName(new StringTextComponent(this.name));
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
			if(stack.hasDisplayName())
				this.name = stack.getDisplayName().getString();
			enchantments = stack.getEnchantmentTagList();
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