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
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.items.InternalStorageItem;
import blusunrize.immersiveengineering.common.items.ToolboxItem;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.TileContainer;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class ToolboxTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, IBlockBounds, IIEInventory,
		IInteractionObjectIE<ToolboxTileEntity>, ITileDrop, IPlayerInteraction
{
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(ToolboxItem.SLOT_COUNT, ItemStack.EMPTY);
	public Component name;
	private ListTag enchantments;

	public ToolboxTileEntity(BlockPos pos, BlockState state)
	{
		super(IETileTypes.TOOLBOX.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(nbt.contains("name", NBT.TAG_STRING))
			this.name = Component.Serializer.fromJson(nbt.getString("name"));
		if(nbt.contains("enchantments", NBT.TAG_LIST))
			this.enchantments = nbt.getList("enchantments", NBT.TAG_COMPOUND);
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
				ItemEntity entityitem = new ItemEntity(level, getBlockPos().getX()+.5, getBlockPos().getY()+.5,
						getBlockPos().getZ()+.5, getPickBlock(player, getBlockState(), new BlockHitResult(new Vec3(hitX, hitY, hitZ), side, worldPosition, false)));
				entityitem.setDefaultPickUpDelay();
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
	public ToolboxTileEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public TileContainer<ToolboxTileEntity, ?> getContainerType()
	{
		return IEContainerTypes.TOOLBOX_BLOCK;
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
		this.setChanged();
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		ItemStack stack = new ItemStack(Tools.toolbox);
		Tools.toolbox.get().setContainedItems(stack, inventory);
		if(this.name!=null)
			stack.setHoverName(this.name);
		if(enchantments!=null)
			stack.getOrCreateTag().put("ench", enchantments);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		if(stack.getItem() instanceof InternalStorageItem)
		{
			stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(inv ->
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

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	private static final VoxelShape boundsZ = Shapes.box(.125f, 0, .25f, .875f, .625f, .75f);
	private static final VoxelShape boundsX = Shapes.box(.25f, 0, .125f, .75f, .625f, .875f);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return getFacing().getAxis()==Axis.Z?boundsZ: boundsX;
	}
}