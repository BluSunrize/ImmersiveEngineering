/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorCovered;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorExtractCovered;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorVertical;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorVerticalCovered;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityConveyorBelt extends TileEntityIEBase implements IDirectionalTile, IAdvancedCollisionBounds, IAdvancedSelectionBounds, IHammerInteraction, IPlayerInteraction, IConveyorTile, IPropertyPassthrough, ITileDrop, ITickable, IGeneralMultiblock, IFaceShape
{
	public EnumFacing facing = EnumFacing.NORTH;
	private IConveyorBelt conveyorBeltSubtype;

	@Override
	@Nullable
	public IConveyorBelt getConveyorSubtype()
	{
		return conveyorBeltSubtype;
	}

	@Override
	public void setConveyorSubtype(IConveyorBelt conveyor)
	{
		this.conveyorBeltSubtype = conveyor;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onEntityCollision(this, entity, facing);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		if(nbt.hasKey("conveyorBeltSubtype"))
		{
			conveyorBeltSubtype = ConveyorHandler.getConveyor(new ResourceLocation(nbt.getString("conveyorBeltSubtype")), this);
			conveyorBeltSubtype.readConveyorNBT(nbt.getCompoundTag("conveyorBeltSubtypeNBT"));
		}

		if(descPacket&&world!=null)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		if(conveyorBeltSubtype!=null)
		{
			nbt.setString("conveyorBeltSubtype", ConveyorHandler.reverseClassRegistry.get(conveyorBeltSubtype.getClass()).toString());
			nbt.setTag("conveyorBeltSubtypeNBT", conveyorBeltSubtype.writeConveyorNBT());
		}
	}

	@Override
	public EnumFacing getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 5;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	public void afterRotation(EnumFacing oldDir, EnumFacing newDir)
	{
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.afterRotation(oldDir, newDir);
	}

	@Override
	public boolean isLogicDummy()
	{
		return this.conveyorBeltSubtype!=null&&!this.conveyorBeltSubtype.isTicking(this);
	}

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onUpdate(this, getFacing());
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking()&&conveyorBeltSubtype!=null&&conveyorBeltSubtype.changeConveyorDirection())
		{
//			if(transportUp)
//			{
//				transportUp = false;
//				transportDown = true;
//			}
//			else if(transportDown)
//				transportDown = false;
//			else
//				transportUp = true;

			this.markDirty();
			this.markContainingBlockForUpdate(null);
			world.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
			return true;
		}
		return false;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(conveyorBeltSubtype!=null)
		{
			boolean update;
			if(conveyorBeltSubtype.canBeDyed()&&Utils.isDye(heldItem))
			{
				EnumDyeColor dye = EnumDyeColor.byDyeDamage(Utils.getDye(heldItem));
				update = dye!=null&&conveyorBeltSubtype.setDyeColour(dye.getColorValue());
			}
			else
				update = conveyorBeltSubtype.playerInteraction(this, player, hand, heldItem, hitX, hitY, hitZ, side);
			if(update)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				world.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
				return true;
			}
		}
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
//		if(conveyorBeltSubtype != null)
//		{
//			AxisAlignedBB aabb = conveyorBeltSubtype.getSelectionBox(this, facing);
//			return new float[]{(float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ};
//		}
		return new float[]{0, 0, 0, 1, .125f, 1};
	}

	static AxisAlignedBB COLISIONBB = new AxisAlignedBB(0, 0, 0, 1, .125F, 1);

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		if(conveyorBeltSubtype!=null)
		{
			List<AxisAlignedBB> boxes = new ArrayList();
			for(AxisAlignedBB aabb : conveyorBeltSubtype.getColisionBoxes(this, facing))
				boxes.add(aabb.offset(getPos()));
			return boxes;
		}
		return Lists.newArrayList(COLISIONBB.offset(getPos()));
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		if(conveyorBeltSubtype!=null)
		{
			List<AxisAlignedBB> boxes = new ArrayList();
			for(AxisAlignedBB aabb : conveyorBeltSubtype.getSelectionBoxes(this, facing))
				boxes.add(aabb.offset(getPos()));
			return boxes;
		}
		return Lists.newArrayList(COLISIONBB.offset(getPos()));
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}

	IItemHandler insertionHandler = new ConveyorInventoryHandler(this);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T)insertionHandler;
		return super.getCapability(capability, facing);
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock());
		if(conveyorBeltSubtype!=null)
			ItemNBTHelper.setString(stack, "conveyorType", ConveyorHandler.reverseClassRegistry.get(this.conveyorBeltSubtype.getClass()).toString());
		return stack;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		String key = ItemNBTHelper.getString(stack, "conveyorType");
		IConveyorBelt subType = ConveyorHandler.getConveyor(new ResourceLocation(key), this);
		setConveyorSubtype(subType);
	}

	@Override
	public BlockFaceShape getFaceShape(EnumFacing side)
	{
		IConveyorBelt subtype = this.getConveyorSubtype();
		if(subtype==null)
			return BlockFaceShape.UNDEFINED;
		if(side==EnumFacing.DOWN&&subtype.getConveyorDirection()==ConveyorDirection.HORIZONTAL)
			return BlockFaceShape.SOLID;
		if(subtype instanceof ConveyorVertical)
		{
			if(side==this.facing)
				return BlockFaceShape.SOLID;
			else if(side.getAxis()==Axis.Y)
				return BlockFaceShape.UNDEFINED;
		}
		if(subtype instanceof ConveyorCovered||subtype instanceof ConveyorVerticalCovered||subtype instanceof ConveyorExtractCovered)
			if(side.getAxis()!=facing.getAxis())
				return BlockFaceShape.SOLID;
		return BlockFaceShape.UNDEFINED;
	}

	public static class ConveyorInventoryHandler implements IItemHandlerModifiable
	{
		TileEntityConveyorBelt conveyor;

		public ConveyorInventoryHandler(TileEntityConveyorBelt conveyor)
		{
			this.conveyor = conveyor;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(!simulate)
			{
				EntityItem entity = new EntityItem(conveyor.getWorld(), conveyor.getPos().getX()+.5, conveyor.getPos().getY()+.1875, conveyor.getPos().getZ()+.5, stack.copy());
				entity.motionX = 0;
				entity.motionY = 0;
				entity.motionZ = 0;
				conveyor.getWorld().spawnEntity(entity);
				if(conveyor.conveyorBeltSubtype!=null)
					conveyor.conveyorBeltSubtype.onItemDeployed(conveyor, entity, conveyor.facing);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}
}