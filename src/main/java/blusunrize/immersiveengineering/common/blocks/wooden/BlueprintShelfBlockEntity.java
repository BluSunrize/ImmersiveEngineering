/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class BlueprintShelfBlockEntity extends IEBaseBlockEntity implements IIEInventory, IBlockEntityDrop,
		IStateBasedDirectional, IPlayerInteraction, IBlockOverlayText
{
	public static final int NUM_SLOTS = 9;

	private NonNullList<ItemStack> inventory = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);

	public BlueprintShelfBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.BLUEPRINT_SHELF.get(), pos, state);
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_INVERTED_NO_DOWN;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return placer.isShiftKeyDown();
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		ContainerHelper.loadAllItems(nbt, inventory, provider);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		ContainerHelper.saveAllItems(nbt, inventory, provider);
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		stack.set(IEDataComponents.GENERIC_ITEMS, ItemContainerContents.fromItems(inventory));
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		if(stack.has(IEDataComponents.GENERIC_ITEMS))
			inventory = ListUtils.fromStream(stack.get(IEDataComponents.GENERIC_ITEMS).stream(), inventory.size());
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return stack.getItem() instanceof EngineersBlueprintItem;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}

	@Override
	public void doGraphicalUpdates()
	{
		this.setChanged();
		markContainingBlockForUpdate(null);
	}

	private static int getTargetedSlot(Direction side, float hitX, float hitY, float hitZ)
	{
		float targetU = switch(side)
		{
			case SOUTH -> hitX;
			case DOWN, UP, NORTH -> 1-hitX;
			case WEST -> hitZ;
			case EAST -> 1-hitZ;
		};
		float targetV = side==Direction.UP?hitZ: hitY;
		return (int)Math.floor(targetU*3)+(targetV < 0.33?6: targetV < 0.66?3: 0);
	}

	@Override
	public ItemInteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		int targetedSlot = getTargetedSlot(side, hitX, hitY, hitZ);
		ItemStack stackInSlot = this.inventory.get(targetedSlot);
		BlockState state = getLevelNonnull().getBlockState(getBlockPos());
		if(!stackInSlot.isEmpty())
		{
			if(heldItem.isEmpty())
				player.setItemInHand(hand, stackInSlot);
			else if(!getLevelNonnull().isClientSide())
				player.spawnAtLocation(stackInSlot, 0);
			this.inventory.set(targetedSlot, ItemStack.EMPTY);
			this.setState(state.setValue(BlueprintShelfBlock.BLUEPRINT_SLOT_FILLED[targetedSlot], false));
			return ItemInteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
		}
		else if(isStackValid(targetedSlot, heldItem))
		{
			this.inventory.set(targetedSlot, heldItem.copyWithCount(1));
			heldItem.shrink(1);
			this.setState(state.setValue(BlueprintShelfBlock.BLUEPRINT_SLOT_FILLED[targetedSlot], true));
			return ItemInteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
		}
		return ItemInteractionResult.FAIL;
	}

	@Nullable
	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(mop instanceof BlockHitResult bhr)
		{
			final float hitX = (float)bhr.getLocation().x-bhr.getBlockPos().getX();
			final float hitY = (float)bhr.getLocation().y-bhr.getBlockPos().getY();
			final float hitZ = (float)bhr.getLocation().z-bhr.getBlockPos().getZ();
			int targetedSlot = getTargetedSlot(bhr.getDirection(), hitX, hitY, hitZ);
			ItemStack stackInSlot = this.inventory.get(targetedSlot);
			String key = IEApiDataComponents.getBlueprintType(stackInSlot);
			if(key.isEmpty()||IEApiDataComponents.INVALID_BLUEPRINT.equals(key))
				return null;
			String formatKey = Lib.DESC_INFO+"blueprint."+key;
			String formatted = I18n.get(formatKey);
			if(formatKey.equals(formatted))
				return new Component[]{Component.literal(key)};
			else
				return new Component[]{Component.translatable(formatKey)};
		}
		return null;
	}
}