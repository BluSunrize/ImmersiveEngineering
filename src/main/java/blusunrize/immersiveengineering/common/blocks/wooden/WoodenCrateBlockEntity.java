/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.gui.CrateMenu;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class WoodenCrateBlockEntity extends RandomizableContainerBlockEntity
		implements IIEInventory, IBlockEntityDrop, IComparatorOverride, IPlayerInteraction, IBlockOverlayText
{
	public static final int CONTAINER_SIZE = 27;
	public static final int HITS_TO_SEAL = 6;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private ListTag enchantments;

	private int sealingProgress = 0;

	public WoodenCrateBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.WOODEN_CRATE.get(), pos, state);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		loadIEData(nbt);
	}

	private void loadIEData(CompoundTag nbt)
	{
		if(nbt.contains("enchantments", Tag.TAG_LIST))
			this.enchantments = nbt.getList("enchantments", Tag.TAG_COMPOUND);
		// Support for unopened legacy crates
		if(nbt.contains("lootTable", Tag.TAG_STRING)&&!nbt.contains("LootTable"))
			nbt.putString("LootTable", nbt.getString("lootTable"));
		if(!tryLoadLootTable(nbt))
			ContainerHelper.loadAllItems(nbt, inventory);
		this.sealingProgress = nbt.getInt("sealingProgress");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if(this.enchantments!=null&&this.enchantments.size() > 0)
			nbt.put("enchantments", this.enchantments);
		if(!trySaveLootTable(nbt))
			ContainerHelper.saveAllItems(nbt, inventory);
		nbt.putInt("sealingProgress", this.sealingProgress);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this, be -> {
			CompoundTag nbttagcompound = new CompoundTag();
			this.saveAdditional(nbttagcompound);
			return nbttagcompound;
		});
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
	{
		CompoundTag nonNullTag = pkt.getTag()!=null?pkt.getTag(): new CompoundTag();
		if(nonNullTag.contains("CustomName", 8))
		{
			Component customName = Component.Serializer.fromJson(nonNullTag.getString("CustomName"));
			if(customName!=null)
				this.setCustomName(customName);
		}
	}

	@Override
	public CompoundTag getUpdateTag()
	{
		CompoundTag nbt = super.getUpdateTag();
		if(getCustomName()!=null)
			nbt.putString("CustomName", Component.Serializer.toJson(getCustomName()));
		return nbt;
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
		// on minecarts, this can be null
		if(this.level!=null)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundTag tag = new CompoundTag();
		if(isSealed())
			ContainerHelper.saveAllItems(tag, inventory, false);
		else
			this.inventory.forEach(drop);
		if(!tag.isEmpty())
			stack.setTag(tag);
		Component customName = getCustomName();
		if(customName!=null)
			stack.setHoverName(customName);
		if(enchantments!=null&&enchantments.size() > 0)
			stack.getOrCreateTag().put("ench", enchantments);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		onBEPlaced(ctx.getItemInHand());
	}

	public void onBEPlaced(ItemStack stack)
	{
		if(stack.hasTag())
		{
			loadIEData(stack.getOrCreateTag());
			if(stack.hasCustomHoverName())
				setCustomName(stack.getHoverName());
			enchantments = stack.getEnchantmentTags();
		}
	}

	@Override
	public boolean canOpen(Player player)
	{
		return super.canOpen(player)&&!isSealed();
	}

	public boolean isSealed()
	{
		return sealingProgress >= HITS_TO_SEAL;
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

	@Override
	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(heldItem.is(IETags.hammers)&&player.isCrouching())
		{
			if(!player.getCooldowns().isOnCooldown(heldItem.getItem())&&!isSealed())
			{
				if(++sealingProgress >= HITS_TO_SEAL)
					player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"crate_sealed"), true);
				player.playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR);
				player.getCooldowns().addCooldown(heldItem.getItem(), 10);
				return InteractionResult.sidedSuccess(Objects.requireNonNull(getLevel()).isClientSide);
			}
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	@Nullable
	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		Component customName = getCustomName();
		if(customName!=null)
			return new Component[]{getCustomName()};
		return null;
	}


}
