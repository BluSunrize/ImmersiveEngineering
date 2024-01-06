/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.items.InternalStorageItem;
import blusunrize.immersiveengineering.common.items.ToolboxItem;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ToolboxBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional, IBlockBounds, IIEInventory,
		IInteractionObjectIE<ToolboxBlockEntity>, IBlockEntityDrop, IPlayerInteraction
{
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(ToolboxItem.SLOT_COUNT, ItemStack.EMPTY);
	public Component name;
	private ListTag enchantments;

	public ToolboxBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.TOOLBOX.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(nbt.contains("name", Tag.TAG_STRING))
			this.name = Component.Serializer.fromJson(nbt.getString("name"));
		if(nbt.contains("enchantments", Tag.TAG_LIST))
			this.enchantments = nbt.getList("enchantments", Tag.TAG_COMPOUND);
		if(!descPacket)
			ContainerHelper.loadAllItems(nbt, inventory);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(this.name!=null)
			nbt.putString("name", Component.Serializer.toJson(this.name));
		if(this.enchantments!=null)
			nbt.put("enchantments", this.enchantments);
		if(!descPacket)
			ContainerHelper.saveAllItems(nbt, inventory);
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(player.isShiftKeyDown())
		{
			if(!level.isClientSide)
			{
				ItemEntity entityitem = new ItemEntity(level, player.getX(), player.getY(), player.getZ(),
				    getPickBlock(player, getBlockState(), new BlockHitResult(new Vec3(hitX, hitY, hitZ), side, worldPosition, false)),
				    0, 0, 0);
				level.removeBlock(getBlockPos(), false);
				level.addFreshEntity(entityitem);
			}
			return true;
		}
		return false;
	}

	//TODO
	//@Override
	//@Nullable
	//public ITextComponent getDisplayName()
	//{
	//	return name!=null?new TextComponentString(name): new TextComponentTranslation("item.immersiveengineering.toolbox.name");
	//}

	@Override
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public ToolboxBlockEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public ArgContainer<ToolboxBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.TOOLBOX_BLOCK;
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
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(Tools.TOOLBOX);
		Tools.TOOLBOX.get().setContainedItems(stack, inventory);
		if(this.name!=null)
			stack.setHoverName(this.name);
		if(enchantments!=null)
			stack.getOrCreateTag().put("ench", enchantments);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		if(stack.getItem() instanceof InternalStorageItem)
		{
			stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(inv ->
			{
				for(int i = 0; i < inv.getSlots(); i++)
					inventory.set(i, inv.getStackInSlot(i));
			});

			if(stack.hasCustomHoverName())
				this.name = stack.getHoverName();
			enchantments = stack.getEnchantmentTags();
		}
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	private static final VoxelShape boundsZ = Shapes.box(.125f, 0, .25f, .875f, .625f, .75f);
	private static final VoxelShape boundsX = Shapes.box(.25f, 0, .125f, .75f, .625f, .875f);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return getFacing().getAxis()==Axis.Z?boundsZ: boundsX;
	}
}