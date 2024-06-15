/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.gui.CrateMenu;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class WoodenCrateBlockEntity extends RandomizableContainerBlockEntity
		implements IIEInventory, IBlockEntityDrop, IComparatorOverride
{
	public static final int CONTAINER_SIZE = 27;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private ItemEnchantments enchantments;

	public WoodenCrateBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.WOODEN_CRATE.get(), pos, state);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, Provider provider)
	{
		super.loadAdditional(nbt, provider);
		loadIEData(nbt, provider);
	}

	private void loadIEData(CompoundTag nbt, Provider provider)
	{
		if(nbt.contains("enchantments"))
			this.enchantments = ItemEnchantments.CODEC.decode(NbtOps.INSTANCE, nbt.get("enchantments"))
					.getOrThrow()
					.getFirst();
		if(!tryLoadLootTable(nbt))
			ContainerHelper.loadAllItems(nbt, inventory, provider);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, Provider provider)
	{
		super.saveAdditional(nbt, provider);
		if(this.enchantments!=null&&this.enchantments.size() > 0)
			nbt.put("enchantments", ItemEnchantments.CODEC.encodeStart(NbtOps.INSTANCE, this.enchantments).getOrThrow());
		if(!trySaveLootTable(nbt))
			ContainerHelper.saveAllItems(nbt, inventory, provider);
	}

	@Override
	protected Component getDefaultName()
	{
		Block b = getBlockState().getBlock();
		if(b==WoodenDevices.REINFORCED_CRATE.get())
			return Component.translatable("block.immersiveengineering.reinforced_crate");
		else
			return Component.translatable("block.immersiveengineering.crate");
	}

	@Override
	protected NonNullList<ItemStack> getItems()
	{
		return inventory;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> pItemStacks)
	{
		this.inventory = pItemStacks;
	}

	@Override
	protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory)
	{
		return new CrateMenu(IEMenuTypes.WOODEN_CRATE.get(), pContainerId, pInventory, this);
	}

	@Override
	@Nonnull
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
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundTag tag = new CompoundTag();
		if(!tag.isEmpty())
			stack.set(IEDataComponents.GENERIC_ITEMS, ItemContainerContents.fromItems(this.inventory));
		Component customName = getCustomName();
		if(customName!=null)
			stack.set(DataComponents.CUSTOM_NAME, customName);
		stack.set(DataComponents.ENCHANTMENTS, enchantments);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		onBEPlaced(ctx.getItemInHand());
	}

	public void onBEPlaced(ItemStack stack)
	{
		if(stack.has(IEDataComponents.GENERIC_ITEMS))
			this.inventory = stack.get(IEDataComponents.GENERIC_ITEMS)
					.stream()
					.collect(ListUtils.collector());
		enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
	}

	private final IItemHandler inventoryCap = new IEInventoryHandler(CONTAINER_SIZE, this);

	public static void registerCapabilities(BECapabilityRegistrar<WoodenCrateBlockEntity> registrar)
	{
		registrar.registerAllContexts(ItemHandler.BLOCK, be -> be.inventoryCap);
	}

	public IItemHandler getInventoryCap()
	{
		return inventoryCap;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack)
	{
		return isStackValid(index, stack);
	}

	@Override
	public int getComparatorInputOverride()
	{
		return Utils.calcRedstoneFromInventory(this);
	}

	@Override
	public int getContainerSize()
	{
		return CONTAINER_SIZE;
	}
}
