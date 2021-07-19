/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.TileContainer;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class ModWorkbenchTileEntity extends IEBaseTileEntity implements IIEInventory, IStateBasedDirectional,
		IHasDummyBlocks, IInteractionObjectIE<ModWorkbenchTileEntity>, IHasObjProperty, IModelOffsetProvider
{
	public static final BlockPos MASTER_POS = BlockPos.ZERO;
	public static final BlockPos DUMMY_POS = new BlockPos(1, 0, 0);
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);

	public ModWorkbenchTileEntity()
	{
		super(IETileTypes.MOD_WORKBENCH.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		ItemStackHelper.loadAllItems(nbt, inventory);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		ItemStackHelper.saveAllItems(nbt, inventory);
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			renderAABB = new AxisAlignedBB(getPos().getX()-1, getPos().getY(), getPos().getZ()-1, getPos().getX()+2, getPos().getY()+2, getPos().getZ()+2);
		return renderAABB;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return slot==0?1: 64;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		applyConfigTo(inventory.get(0), message);
	}

	public static void applyConfigTo(ItemStack stack, CompoundNBT message)
	{
		if(!(stack.getItem() instanceof IConfigurableTool))
			return;
		for(String key : message.keySet())
		{
			INBT tag = message.get(key);
			if(tag instanceof ByteNBT)
				((IConfigurableTool)stack.getItem()).applyConfigOption(stack, key, ((ByteNBT)tag).getByte()!=0);
			else if(tag instanceof FloatNBT)
				((IConfigurableTool)stack.getItem()).applyConfigOption(stack, key, ((FloatNBT)tag).getFloat());
		}
	}

	@Override
	public boolean isDummy()
	{
		return getState().get(IEProperties.MULTIBLOCKSLAVE);
	}

	@Nullable
	@Override
	public ModWorkbenchTileEntity master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterTE!=null)
			return (ModWorkbenchTileEntity)tempMasterTE;
		Direction dummyDir = isDummy()?getFacing().rotateYCCW(): getFacing().rotateY();
		BlockPos masterPos = getPos().offset(dummyDir);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return (te instanceof ModWorkbenchTileEntity)?(ModWorkbenchTileEntity)te: null;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		DeskBlock.placeDummies(getBlockState(), world, pos, ctx);
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		tempMasterTE = master();
		Direction dummyDir = isDummy()?getFacing().rotateYCCW(): getFacing().rotateY();
		world.removeBlock(pos.offset(dummyDir), false);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return true;
	}

	@Override
	public ModWorkbenchTileEntity getGuiMaster()
	{
		if(!isDummy())
			return this;
		Direction dummyDir = getFacing().rotateYCCW();
		TileEntity tileEntityModWorkbench = world.getTileEntity(pos.offset(dummyDir));
		if(tileEntityModWorkbench instanceof ModWorkbenchTileEntity)
			return (ModWorkbenchTileEntity)tileEntityModWorkbench;
		return null;
	}

	@Override
	public TileContainer<ModWorkbenchTileEntity, ?> getContainerType()
	{
		return IEContainerTypes.MOD_WORKBENCH;
	}

	private static VisibilityList normalDisplayList = VisibilityList.show("cube0");
	private static VisibilityList blueprintDisplayList = VisibilityList.show("cube0", "blueprint");

	@Override
	public VisibilityList compileDisplayList(BlockState state)
	{
		ModWorkbenchTileEntity master = master();
		if(master!=null&&master.inventory.get(0).getItem() instanceof EngineersBlueprintItem)
			return blueprintDisplayList;
		return normalDisplayList;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vector3i size)
	{
		if(isDummy())
			return DUMMY_POS;
		else
			return MASTER_POS;
	}
}