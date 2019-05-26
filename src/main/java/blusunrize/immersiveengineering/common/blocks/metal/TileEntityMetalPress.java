/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPoweredMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockMetalPress;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityMetalPress extends TileEntityPoweredMultiblock<TileEntityMetalPress, MetalPressRecipe> implements
		IPlayerInteraction, IConveyorAttachable
{
	public static TileEntityType<TileEntityMetalPress> TYPE;

	public TileEntityMetalPress()
	{
		super(MultiblockMetalPress.instance, 16000, true, TYPE);
	}

	public ItemStack mold = ItemStack.EMPTY;

	@Override
	public void tick()
	{
		super.tick();
		if(isDummy()||isRSDisabled()||world.isRemote)
			return;
		for(MultiblockProcess process : processQueue)
		{
			float tick = 1/(float)process.maxTicks;
			float transportTime = 52.5f/120f;
			float pressTime = 3.75f/120f;
			float fProcess = process.processTick*tick;
			if(fProcess >= transportTime&&fProcess < transportTime+tick)
				world.playSound(null, getPos(), IESounds.metalpress_piston, SoundCategory.BLOCKS, .3F, 1);
			if(fProcess >= (transportTime+pressTime)&&fProcess < (transportTime+pressTime+tick))
				world.playSound(null, getPos(), IESounds.metalpress_smash, SoundCategory.BLOCKS, .3F, 1);
			if(fProcess >= (1-transportTime)&&fProcess < (1-transportTime+tick))
				world.playSound(null, getPos(), IESounds.metalpress_piston, SoundCategory.BLOCKS, .3F, 1);
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		mold = ItemStack.read(nbt.getCompound("mold"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.mold.isEmpty())
			nbt.setTag("mold", this.mold.write(new NBTTagCompound()));
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		TileEntityMetalPress master = master();
		if(master!=null)
			if(player.isSneaking()&&!master.mold.isEmpty())
			{
				if(heldItem.isEmpty())
					player.setHeldItem(hand, master.mold.copy());
				else if(!world.isRemote)
					player.entityDropItem(master.mold.copy(), 0);
				master.mold = ItemStack.EMPTY;
				this.updateMasterBlock(null, true);
				return true;
			}
			else if(MetalPressRecipe.isValidMold(heldItem))
			{
				ItemStack tempMold = !master.mold.isEmpty()?master.mold.copy(): ItemStack.EMPTY;
				master.mold = Utils.copyStackWithAmount(heldItem, 1);
				heldItem.shrink(1);
				if(heldItem.getCount() <= 0)
					heldItem = ItemStack.EMPTY;
				else
					player.setHeldItem(hand, heldItem);
				if(!tempMold.isEmpty())
					if(heldItem.isEmpty())
						player.setHeldItem(hand, tempMold);
					else if(!world.isRemote)
						player.entityDropItem(tempMold, 0);
				this.updateMasterBlock(null, true);
				return true;
			}
		return false;
	}


	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock==3||posInMultiblock==5)
			return new float[]{0, 0, 0, 1, .125f, 1};
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public void replaceStructureBlock(BlockPos pos, IBlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==1&&l!=1)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityConveyorBelt)
				((TileEntityConveyorBelt)tile).setFacing(this.facing);
		}
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(posInMultiblock==3&&!world.isRemote&&entity instanceof EntityItem&&entity.isAlive()
				&&!((EntityItem)entity).getItem().isEmpty())
		{
			TileEntityMetalPress master = master();
			if(master==null)
				return;
			ItemStack stack = ((EntityItem)entity).getItem();
			if(stack.isEmpty())
				return;
			MetalPressRecipe recipe = master.findRecipeForInsertion(stack);
			if(recipe==null)
				return;
			ItemStack displayStack = recipe.getDisplayStack(stack);
			float transformationPoint = 56.25f/120f;
			MultiblockProcess<MetalPressRecipe> process = new MultiblockProcessInWorld<>(recipe, transformationPoint,
					Utils.createNonNullItemStackListFromItemStack(displayStack));
			if(master.addProcessToQueue(process, true))
			{
				master.addProcessToQueue(process, false);
				stack.shrink(displayStack.getCount());
				if(stack.getCount() <= 0)
					entity.remove();
			}
		}
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{7};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{1};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MetalPressRecipe> process)
	{
		return true;
	}

	private DirectionalBlockPos getOutputPos()
	{
		return new DirectionalBlockPos(pos.offset(facing, 2), facing);
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(this,
			this::getOutputPos, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
		{
			DirectionalBlockPos outPos = getOutputPos();
			Utils.dropStackAtPos(world, outPos, output, outPos.direction);
		}
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MetalPressRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 3;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 3;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<MetalPressRecipe> process)
	{
		return 63.75f/120f;
	}


	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
	}

	@Override
	public NonNullList<ItemStack> getDroppedItems()
	{
		return ListUtils.fromItem(mold);
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return false;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 0;
	}

	@Override
	public int[] getOutputSlots()
	{
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return null;
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new MultiblockInventoryHandler_DirectProcessing<>(this)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityMetalPress master = master();
			if(master==null)
				return LazyOptional.empty();
			if(posInMultiblock==3&&facing==this.facing.getOpposite())
				return master.insertionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public MetalPressRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return MetalPressRecipe.findRecipe(mold, inserting);
	}

	@Override
	protected MetalPressRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return MetalPressRecipe.loadFromNBT(tag);
	}

	@Override
	public EnumFacing[] sigOutputDirections()
	{
		if(posInMultiblock==5)
			return new EnumFacing[]{this.facing};
		return new EnumFacing[0];
	}
}