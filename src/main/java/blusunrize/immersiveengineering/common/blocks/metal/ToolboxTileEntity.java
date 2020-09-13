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
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.InternalStorageItem;
import blusunrize.immersiveengineering.common.items.ToolboxItem;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class ToolboxTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, IBlockBounds, IIEInventory,
		IInteractionObjectIE, ITileDrop, IPlayerInteraction
{
	NonNullList<ItemStack> inventory = NonNullList.withSize(ToolboxItem.SLOT_COUNT, ItemStack.EMPTY);
	public ITextComponent name;
	private ListNBT enchantments;

	public ToolboxTileEntity()
	{
		super(IETileTypes.TOOLBOX.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(nbt.contains("name", NBT.TAG_STRING))
			this.name = ITextComponent.Serializer.fromJson(nbt.getString("name"));
		if(nbt.contains("enchantments", NBT.TAG_LIST))
			this.enchantments = nbt.getList("enchantments", NBT.TAG_COMPOUND);
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", NBT.TAG_COMPOUND), ToolboxItem.SLOT_COUNT);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(this.name!=null)
			nbt.putString("name", ITextComponent.Serializer.toJson(this.name));
		if(this.enchantments!=null)
			nbt.put("enchantments", this.enchantments);
		if(!descPacket)
			nbt.put("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			if(!world.isRemote)
			{
				ItemEntity entityitem = new ItemEntity(world, getPos().getX()+.5, getPos().getY()+.5,
						getPos().getZ()+.5, getPickBlock(player, getBlockState(), new BlockRayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos, false)));
				entityitem.setDefaultPickupDelay();
				world.removeBlock(getPos(), false);
				world.addEntity(entityitem);
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
	public boolean canUseGui(PlayerEntity player)
	{
		return true;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
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
		ItemStack stack = new ItemStack(Tools.toolbox);
		((InternalStorageItem)Tools.toolbox).setContainedItems(stack, inventory);
		if(this.name!=null)
			stack.setDisplayName(this.name);
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
				inventory = NonNullList.withSize(inv.getSlots(), ItemStack.EMPTY);
				for(int i = 0; i < inv.getSlots(); i++)
					inventory.set(i, inv.getStackInSlot(i));
			});

			if(stack.hasDisplayName())
				this.name = stack.getDisplayName();
			enchantments = stack.getEnchantmentTagList();
		}
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
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
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	private static final VoxelShape boundsZ = VoxelShapes.create(.125f, 0, .25f, .875f, .625f, .75f);
	private static final VoxelShape boundsX = VoxelShapes.create(.25f, 0, .125f, .75f, .625f, .875f);

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return getFacing().getAxis()==Axis.Z?boundsZ: boundsX;
	}
}