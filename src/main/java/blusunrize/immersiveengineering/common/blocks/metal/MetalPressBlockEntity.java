/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.crafting.MetalPressPackingRecipes;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class MetalPressBlockEntity extends PoweredMultiblockBlockEntity<MetalPressBlockEntity, MetalPressRecipe> implements
		IPlayerInteraction, IConveyorAttachable, IBlockBounds
{
	public static final float TRANSLATION_DISTANCE = 2.5f;
	private static final float STANDARD_TRANSPORT_TIME = 16f*(TRANSLATION_DISTANCE/2); //16 frames in conveyor animation, 1 frame/tick, 2.5 blocks of total translation distance, halved because transport time just affects half the distance
	private static final float STANDARD_PRESS_TIME = 3.75f;
	private static final float MIN_CYCLE_TIME = 60f; //set >= 2*(STANDARD_PRESS_TIME+STANDARD_TRANSPORT_TIME)

	public MetalPressBlockEntity(BlockEntityType<MetalPressBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.METAL_PRESS, 16000, true, type, pos, state);
	}

	public ItemStack mold = ItemStack.EMPTY;

	@Override
	public void tickClient()
	{
		super.tickClient();
		if(isRSDisabled())
			return;
		for(MultiblockProcess<?> process : processQueue)
		{
			float maxTicks = process.maxTicks;
			float transportTime = getTransportTime(maxTicks);
			float pressTime = getPressTime(maxTicks);
			float fProcess = process.processTick;
			LocalPlayer localPlayer = ClientUtils.mc().player;
			//Note: the >= and < check instead of a single == is because fProcess is an int and transportTime and pressTime are floats. Because of that it has to be windowed
			if(fProcess >= transportTime&&fProcess < transportTime+1f)
				level.playSound(localPlayer, getBlockPos(), IESounds.metalpress_piston, SoundSource.BLOCKS, .3F, 1);
			if(fProcess >= (transportTime+pressTime)&&fProcess < (transportTime+pressTime+1f))
				level.playSound(localPlayer, getBlockPos(), IESounds.metalpress_smash, SoundSource.BLOCKS, .3F, 1);
			if(fProcess >= (maxTicks-transportTime)&&fProcess < (maxTicks-transportTime+1f))
				level.playSound(localPlayer, getBlockPos(), IESounds.metalpress_piston, SoundSource.BLOCKS, .3F, 1);
		}
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		mold = ItemStack.of(nbt.getCompound("mold"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.mold.isEmpty())
			nbt.put("mold", this.mold.save(new CompoundTag()));
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		MetalPressBlockEntity master = master();
		if(master!=null)
			if(player.isShiftKeyDown()&&!master.mold.isEmpty())
			{
				if(heldItem.isEmpty())
					player.setItemInHand(hand, master.mold.copy());
				else if(!level.isClientSide)
					player.spawnAtLocation(master.mold.copy(), 0);
				master.mold = ItemStack.EMPTY;
				this.updateMasterBlock(null, true);
				return true;
			}
			else if(MetalPressRecipe.isValidMold(heldItem))
			{
				ItemStack tempMold = !master.mold.isEmpty()?master.mold.copy(): ItemStack.EMPTY;
				master.mold = ItemHandlerHelper.copyStackWithSize(heldItem, 1);
				heldItem.shrink(1);
				if(heldItem.getCount() <= 0)
					heldItem = ItemStack.EMPTY;
				else
					player.setItemInHand(hand, heldItem);
				if(!tempMold.isEmpty())
					if(heldItem.isEmpty())
						player.setItemInHand(hand, tempMold);
					else if(!level.isClientSide)
						player.spawnAtLocation(tempMold, 0);
				this.updateMasterBlock(null, true);
				return true;
			}
		return false;
	}


	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(posInMultiblock.getY()==1&&posInMultiblock.getX()%2==0)
			return Shapes.box(0, 0, 0, 1, .125f, 1);
		return Shapes.block();
	}

	@Override
	public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==1&&l!=1)
		{
			BlockEntity tile = level.getBlockEntity(pos);
			if(tile instanceof ConveyorBeltBlockEntity)
				((ConveyorBeltBlockEntity)tile).setFacing(this.getFacing());
		}
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(new BlockPos(0, 1, 0).equals(posInMultiblock)&&!world.isClientSide&&entity instanceof ItemEntity&&entity.isAlive()
				&&!((ItemEntity)entity).getItem().isEmpty())
		{
			MetalPressBlockEntity master = master();
			if(master==null)
				return;
			ItemStack stack = ((ItemEntity)entity).getItem();
			if(stack.isEmpty())
				return;
			MetalPressRecipe recipe = master.findRecipeForInsertion(stack);
			if(recipe==null)
				return;
			ItemStack displayStack = recipe.getDisplayStack(stack);
//			float processMaxTicks = recipe.getTotalProcessTime();
//			float transformationPoint = (MetalPressRenderer.getPressTime(processMaxTicks)+MetalPressRenderer.getPressTime(processMaxTicks))/processMaxTicks;
			float transformationPoint = 0.5f;
			MultiblockProcess<MetalPressRecipe> process = new MultiblockProcessInWorld<>(recipe, transformationPoint,
					Utils.createNonNullItemStackListFromItemStack(displayStack));
			if(master.addProcessToQueue(process, true))
			{
				master.addProcessToQueue(process, false);
				stack.shrink(displayStack.getCount());
				if(stack.getCount() <= 0)
					entity.discard();
			}
		}
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(1, 2, 0)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(1, 0, 0)
		);
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
		return new DirectionalBlockPos(worldPosition.relative(getFacing(), 2), getFacing());
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forBlockEntityAt(this,
			this::getOutputPos, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
		{
			DirectionalBlockPos outPos = getOutputPos();
			Utils.dropStackAtPos(level, outPos.position(), output, outPos.side());
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
		float maxTicks = process.maxTicks;
		return 1f-(getTransportTime(maxTicks)+getPressTime(maxTicks))/maxTicks;
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
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Override
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new MultiblockInventoryHandler_DirectProcessing<>(this)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			MetalPressBlockEntity master = master();
			if(master==null)
				return LazyOptional.empty();
			if(new BlockPos(0, 1, 0).equals(posInMultiblock)&&facing==this.getFacing().getOpposite())
				return master.insertionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public MetalPressRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return MetalPressRecipe.findRecipe(mold, inserting, level);
	}

	@Override
	protected MetalPressRecipe getRecipeForId(ResourceLocation id)
	{
		MetalPressRecipe recipe = MetalPressRecipe.recipeList.get(id);
		if(recipe==null)
			recipe = MetalPressPackingRecipes.getRecipeDelegate(id);
		return recipe;
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		if(new BlockPos(2, 1, 0).equals(posInMultiblock))
			return new Direction[]{this.getFacing()};
		return new Direction[0];
	}

	public static float getTransportTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_TRANSPORT_TIME;
		else
			return processMaxTicks*STANDARD_TRANSPORT_TIME/MIN_CYCLE_TIME;
	}

	public static float getPressTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_PRESS_TIME;
		else
			return processMaxTicks*STANDARD_PRESS_TIME/MIN_CYCLE_TIME;
	}
}