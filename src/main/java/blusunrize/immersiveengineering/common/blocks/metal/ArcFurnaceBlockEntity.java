/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEParticles;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ArcFurnaceBlockEntity extends PoweredMultiblockBlockEntity<ArcFurnaceBlockEntity, ArcFurnaceRecipe>
		implements ISoundBE, IInteractionObjectIE<ArcFurnaceBlockEntity>, ISelectionBounds, ICollisionBounds,
		IEClientTickableBE
{
	public static final int FIRST_IN_SLOT = 0;
	public static final int IN_SLOT_COUNT = 12;
	public static final int FIRST_ADDITIVE_SLOT = 12;
	public static final int ADDITIVE_SLOT_COUNT = 4;
	public static final int FIRST_OUT_SLOT = 16;
	public static final int OUT_SLOT_COUNT = 6;
	public static final int SLAG_SLOT = 22;
	public static final int FIRST_ELECTRODE_SLOT = 23;
	public static final int ELECTRODE_COUNT = 3;
	private static final BlockPos SLAG_OUT_POS = new BlockPos(2, 0, 0);
	private static final BlockPos MAIN_OUT_POS = new BlockPos(2, 0, 4);
	private static final int[] OUTPUT_SLOTS;

	static
	{
		OUTPUT_SLOTS = new int[OUT_SLOT_COUNT];
		for(int i = 0; i < OUT_SLOT_COUNT; ++i)
			OUTPUT_SLOTS[i] = FIRST_OUT_SLOT+i;
	}

	public NonNullList<ItemStack> inventory = NonNullList.withSize(26, ItemStack.EMPTY);
	public int pouringMetal = 0;
	private final CapabilityReference<IItemHandler> output = CapabilityReference.forBlockEntityAt(this,
			() -> new DirectionalBlockPos(this.getBlockPosForPos(MAIN_OUT_POS).relative(getFacing(), -1), getFacing().getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	private final CapabilityReference<IItemHandler> slagOut = CapabilityReference.forBlockEntityAt(this,
			() -> new DirectionalBlockPos(this.getBlockPosForPos(SLAG_OUT_POS).relative(getFacing()), getFacing().getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	public ArcFurnaceBlockEntity(BlockEntityType<ArcFurnaceBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.ARC_FURNACE, 64000, true, type, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
			ContainerHelper.loadAllItems(nbt, inventory);
		else
		{
			byte electrodeStatus = nbt.getByte("electrodeStatus");
			for(int i = 0; i < ELECTRODE_COUNT; ++i)
			{
				boolean hasElectrodeServer = (electrodeStatus&1)!=0;
				int slot = FIRST_ELECTRODE_SLOT+i;
				boolean hasElectrodeClient = !inventory.get(slot).isEmpty();
				if(hasElectrodeServer&&!hasElectrodeClient)
					inventory.set(slot, new ItemStack(Misc.GRAPHITE_ELECTRODE));
				else if(!hasElectrodeServer&&hasElectrodeClient)
					inventory.set(slot, ItemStack.EMPTY);
				electrodeStatus >>>= 1;
			}
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
			ContainerHelper.saveAllItems(nbt, inventory);
		else
		{
			byte packed = 0;
			byte mask = 1;
			for(int i = 0; i < ELECTRODE_COUNT; ++i)
			{
				if(!inventory.get(FIRST_ELECTRODE_SLOT+i).isEmpty())
					packed += mask;
				mask *= 2;
			}
			nbt.putByte("electrodeStatus", packed);
		}
	}

	@Override
	public void tickClient()
	{
		if(isDummy())
			return;
		if(pouringMetal > 0)
			pouringMetal--;
		if(shouldRenderAsActive())
			for(int i = 0; i < 4; i++)
			{
				if(Utils.RAND.nextInt(6)==0)
					level.addParticle(IEParticles.SPARKS.get(), getBlockPos().getX()+.5-.25*getFacing().getStepX(),
							getBlockPos().getY()+2.9, getBlockPos().getZ()+.5-.25*getFacing().getStepZ(),
							Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
				if(Utils.RAND.nextInt(6)==0)
					level.addParticle(IEParticles.SPARKS.get(), getBlockPos().getX()+.5+(getFacing()==Direction.EAST?-.25: .25),
							getBlockPos().getY()+2.9, getBlockPos().getZ()+.5+(getFacing()==Direction.SOUTH?.25: -.25),
							Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
				if(Utils.RAND.nextInt(6)==0)
					level.addParticle(IEParticles.SPARKS.get(), getBlockPos().getX()+.5+(getFacing()==Direction.WEST?.25: -.25),
							getBlockPos().getY()+2.9, getBlockPos().getZ()+.5+(getFacing()==Direction.NORTH?-.25: .25),
							Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
			}
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		if (isDummy())
			return;
		if(!isRSDisabled()&&energyStorage.getEnergyStored() > 0)
		{
			if(this.tickedProcesses > 0)
				for(int i = FIRST_ELECTRODE_SLOT; i < FIRST_ELECTRODE_SLOT+ELECTRODE_COUNT; i++)
					if(this.inventory.get(i).hurt(1, Utils.RAND, null))
						this.inventory.set(i, ItemStack.EMPTY);
			updateComparators(
					this, ImmutableList.of(ELECTRODE_COMPARATOR_POS), electrodeComparatorValue, getElectrodeComparatorValueOnMaster()
			);

			if(this.processQueue.size() < this.getProcessQueueMaxLength())
			{
				Map<Integer, Integer> usedInvSlots = new HashMap<>();
				for(MultiblockProcess<ArcFurnaceRecipe> process : processQueue)
					if(process instanceof MultiblockProcessInMachine)
					{
						int[] inputSlots = ((MultiblockProcessInMachine<ArcFurnaceRecipe>)process).getInputSlots();
						int[] inputAmounts = ((MultiblockProcessInMachine<ArcFurnaceRecipe>)process).getInputAmounts();
						if(inputAmounts!=null)
							for(int i = 0; i < inputSlots.length; i++)
								if(inputAmounts[i] > 0)
								{
									if(usedInvSlots.containsKey(inputSlots[i]))
										usedInvSlots.put(inputSlots[i], usedInvSlots.get(inputSlots[i])+inputAmounts[i]);
									else
										usedInvSlots.put(inputSlots[i], inputAmounts[i]);
								}
					}

				NonNullList<ItemStack> additives = NonNullList.withSize(ADDITIVE_SLOT_COUNT, ItemStack.EMPTY);
				for(int i = 0; i < ADDITIVE_SLOT_COUNT; i++)
					if(!inventory.get(FIRST_ADDITIVE_SLOT+i).isEmpty())
					{
						additives.set(i, inventory.get(FIRST_ADDITIVE_SLOT+i).copy());
						if(usedInvSlots.containsKey(FIRST_ADDITIVE_SLOT+i))
							additives.get(i).shrink(usedInvSlots.get(FIRST_ADDITIVE_SLOT+i));
					}

				for(int slot = FIRST_IN_SLOT; slot < IN_SLOT_COUNT; slot++)
					if(!usedInvSlots.containsKey(slot))
					{
						ItemStack stack = this.getInventory().get(slot);
						if(!stack.isEmpty()&&stack.getCount() > 0)
						{
							ArcFurnaceRecipe recipe = ArcFurnaceRecipe.findRecipe(stack, additives);
							if(recipe!=null)
							{
								MultiblockProcessArcFurnace process = new MultiblockProcessArcFurnace(recipe, slot, 12, 13, 14, 15);
								if(this.addProcessToQueue(process, true))
								{
									this.addProcessToQueue(process, false);
									int[] consumedAdditives = recipe.getConsumedAdditives(additives, true);
									if(consumedAdditives!=null)
										process.setInputAmounts(
												recipe.input.getCount(),
												consumedAdditives[0],
												consumedAdditives[1],
												consumedAdditives[2],
												consumedAdditives[3]
										);
								}
							}
						}
					}
			}

			if(level.getGameTime()%8==0)
			{
				if(output.isPresent())
					for(int j : OUTPUT_SLOTS)
						if(!inventory.get(j).isEmpty())
						{
							ItemStack stack = ItemHandlerHelper.copyStackWithSize(inventory.get(j), 1);
							stack = Utils.insertStackIntoInventory(output, stack, false);
							if(stack.isEmpty())
							{
								this.inventory.get(j).shrink(1);
								if(this.inventory.get(j).getCount() <= 0)
									this.inventory.set(j, ItemStack.EMPTY);
							}
						}
				if(!inventory.get(SLAG_SLOT).isEmpty()&&slagOut.isPresent())
				{
					int out = Math.min(inventory.get(SLAG_SLOT).getCount(), 16);
					ItemStack stack = ItemHandlerHelper.copyStackWithSize(inventory.get(SLAG_SLOT), out);
					stack = Utils.insertStackIntoInventory(slagOut, stack, false);
					if(!stack.isEmpty())
						out -= stack.getCount();
					this.inventory.get(SLAG_SLOT).shrink(out);
					if(this.inventory.get(SLAG_SLOT).getCount() <= 0)
						this.inventory.set(SLAG_SLOT, ItemStack.EMPTY);
				}
			}
		}
	}

	@Override
	public boolean triggerEvent(int id, int type)
	{
		if(id==0)
			pouringMetal = type;
		return super.triggerEvent(id, type);
	}

	//TODO cache
	private AABB renderAABB;

	@Override
	public AABB getRenderBoundingBox()
	{
		//		if(renderAABB==null)
		//			if(posInMultiblock==17)
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+3,zCoord+(facing==4||facing==5?3:2));
		//			else
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
		//		return renderAABB;
		return new AABB(getBlockPos().getX()-(getFacing().getAxis()==Axis.Z?2: 1), getBlockPos().getY(), getBlockPos().getZ()-(getFacing().getAxis()==Axis.X?2: 1), getBlockPos().getX()+(getFacing().getAxis()==Axis.Z?3: 2), getBlockPos().getY()+3, getBlockPos().getZ()+(getFacing().getAxis()==Axis.X?3: 2));
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> COLLISION_SHAPES =
			CachedShapesWithTransform.createForMultiblock(ArcFurnaceBlockEntity::getCollisionShape);

	@Nonnull
	@Override
	public VoxelShape getCollisionShape(CollisionContext ctx)
	{
		return getShape(COLLISION_SHAPES);
	}

	private static List<AABB> getCollisionShape(BlockPos posInMultiblock)
	{
		if(ImmutableSet.of(
				new BlockPos(3, 0, 4),
				new BlockPos(1, 0, 4)
		).contains(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, 0, 1, .5f, .5625f));
		else if(posInMultiblock.getY()==0&&posInMultiblock.getZ() > 0&&!posInMultiblock.equals(new BlockPos(2, 0, 4)))
			return ImmutableList.of(new AABB(0, 0, 0, 1, .5f, 1));
		else if(new BlockPos(0, 1, 4).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, .5f, 1, 1, 1));
		else if(new BoundingBox(1, 1, 1, 3, 1, 2)
				.isInside(posInMultiblock))
		{
			AABB aabb;
			if(posInMultiblock.getX()==2)
				aabb = new AABB(0, 0.5, 0, 1, 1, 1);
			else
				aabb = Utils.flipBox(false, posInMultiblock.getX()==3,
						new AABB(0.125, 0.5, 0.125, 1, 1, 0.875));
			if(posInMultiblock.getZ()==2)
				aabb = aabb.move(0, 0, 0.875);
			return ImmutableList.of(aabb);
		}
		else if(ImmutableSet.of(
				new BlockPos(4, 1, 1),
				new BlockPos(0, 1, 1)
		).contains(posInMultiblock))
			return Utils.flipBoxes(false, posInMultiblock.getX()==4,
					new AABB(.125f, .125f, 0, .375f, .375f, 1));
		else if(posInMultiblock.getZ()==0&&posInMultiblock.getY()==1&&posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3)
			return ImmutableList.of(new AABB(0, 0, .25f, 1, 1, 1));
		else if(new BlockPos(2, 3, 0).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, .375f, 1, 1, .625f));
		else if(new BlockPos(2, 4, 0).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, .3125f, 1, .9375f, 1));
		else if(new BlockPos(2, 4, 1).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0, .625f, 0, 1, .9375f, 1));
		else if(new BlockPos(2, 4, 2).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, 0, 1, .9375f, .875f));
		else if(ImmutableSet.of(
				new BlockPos(3, 2, 4),
				new BlockPos(1, 2, 4),
				new BlockPos(3, 3, 0),
				new BlockPos(1, 3, 0),
				new BlockPos(3, 4, 0),
				new BlockPos(1, 4, 0)
		).contains(posInMultiblock))
			return Utils.flipBoxes(false, posInMultiblock.getX()==3,
					new AABB(.5f, 0, 0, 1, 1, 1));
		else
			return ImmutableList.of(new AABB(0, 0, 0, 1, 1, 1));
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(ArcFurnaceBlockEntity::getShape);

	@Override
	public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
	{
		return getShape(SHAPES);
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		if(new BlockPos(0, 0, 4).equals(posInMultiblock))
			return ImmutableList.of(
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0.125, .5f, 0.625, 0.25, 1, 0.875),
					new AABB(0.75, .5f, 0.625, 0.875, 1, 0.875)
			);
		else if(posInMultiblock.getZ()==0&&posInMultiblock.getY()==1&&posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3)
			return ImmutableList.of(
					new AABB(0, 0, 0.25, 1, 1, 1),
					new AABB(0.25, .25f, 0, 0.75, .75, 0.25));
		else if(posInMultiblock.getX()%4==0&&posInMultiblock.getZ() <= 2)
		{
			List<AABB> list = posInMultiblock.getY()==0?Lists.newArrayList(new AABB(0, 0, 0, 1, .5f, 1)): new ArrayList<>(2);
			final boolean flip = posInMultiblock.getX()==4;
			double minX = !flip?.5f: 0;
			double maxX = flip?.5f: 1;
			if(posInMultiblock.getX()!=3)
				list.add(new AABB(minX, posInMultiblock.getY()==0?.5: 0, 0, maxX, 1, 1));
			if(posInMultiblock.getY()==0)
			{
				int move = (4-posInMultiblock.getZ())-2;
				minX = !flip?.125f: .625f;
				maxX = !flip?.375f: .875f;
				AABB aabb = new AABB(minX, .6875, -1.625f, maxX, .9375, 0.625);
				aabb = aabb.move(0, 0, move);
				list.add(aabb);

				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				aabb = new AABB(minX, .6875, 0.375, maxX, .9375, 0.625);
				aabb = aabb.move(0, 0, move);
				list.add(aabb);

				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				aabb = new AABB(minX, .6875, -1.625f, maxX, .9375, -1.375f);
				aabb = aabb.move(0, 0, move);
				list.add(aabb);
			}
			else if(posInMultiblock.getY()==1)
			{
				int move = (4-posInMultiblock.getZ())-2;
				minX = !flip?.125f: .625f;
				maxX = !flip?.375f: .875f;
				AABB aabb = new AABB(minX, .125, -1.625f, maxX, .375, .625f);
				aabb = aabb.move(0, 0, move);
				list.add(aabb);

				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				aabb = new AABB(minX, .125, 0.375, maxX, .375, 0.625);
				aabb = aabb.move(0, 0, move);
				if(posInMultiblock.getX()==0)
					aabb = aabb.move(0, .6875, 0);
				list.add(aabb);
				if(posInMultiblock.getX()==0)
				{
					minX = !flip?.125f: .625f;
					maxX = !flip?.375f: .875f;
					aabb = new AABB(minX, .375, 0.375, maxX, 1.0625, 0.625);
					aabb = aabb.move(0, 0, move);
					list.add(aabb);
				}
				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				aabb = new AABB(minX, .125, -1.625f, maxX, .375, -1.375f);
				aabb = aabb.move(0, 0, move);
				list.add(aabb);
			}
			else if(ImmutableSet.of(
					new BlockPos(4, 2, 2),
					new BlockPos(0, 2, 2)
			).contains(posInMultiblock))
			{
				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				list.add(new AABB(minX, .25, 0.25, maxX, .75, 0.75));
			}
			return list;
		}
		return getCollisionShape(posInMultiblock);
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(1, 1, 0),
				new BlockPos(2, 1, 0),
				new BlockPos(3, 1, 0)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 4)
		);
	}

	private static final BlockPos ELECTRODE_COMPARATOR_POS = new BlockPos(2, 4, 2);
	private final MutableInt electrodeComparatorValue = new MutableInt(-1);

	private int getElectrodeComparatorValueOnMaster() {
		float f = 0;
		for(int i = FIRST_ELECTRODE_SLOT; i < FIRST_ELECTRODE_SLOT+ELECTRODE_COUNT; i++)
			if(!inventory.get(i).isEmpty())
				f += 1-(inventory.get(i).getDamageValue()/(float)inventory.get(i).getMaxDamage());
		return Mth.floor(Math.max(f/3f, 0)*15);
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(ELECTRODE_COMPARATOR_POS.equals(posInMultiblock))
		{
			ArcFurnaceBlockEntity master = master();
			if(master!=null)
				return master.getElectrodeComparatorValueOnMaster();
		}
		return super.getComparatorInputOverride();
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean shouldRenderAsActiveImpl()
	{
		return hasElectrodes()&&super.shouldRenderAsActiveImpl();
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		if(!hasElectrodes())
			return false;
		if(process.recipe!=null&&!process.recipe.slag.isEmpty())
		{
			if(this.inventory.get(SLAG_SLOT).isEmpty())
				return true;
			return ItemHandlerHelper.canItemStacksStack(this.inventory.get(SLAG_SLOT), process.recipe.slag)&&inventory.get(SLAG_SLOT).getCount()+process.recipe.slag.getCount() <= getSlotLimit(SLAG_SLOT);
		}
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(this.output, output, false);
		if(!output.isEmpty())
		{
			BlockPos pos = getBlockPos().offset(0, -1, 0).relative(getFacing(), -2);
			Utils.dropStackAtPos(level, pos, output, getFacing());
		}
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		if(!process.recipe.slag.isEmpty())
		{
			if(this.inventory.get(SLAG_SLOT).isEmpty())
				this.inventory.set(SLAG_SLOT, process.recipe.slag.copy());
			else if(ItemHandlerHelper.canItemStacksStack(this.inventory.get(SLAG_SLOT), process.recipe.slag)||inventory.get(SLAG_SLOT).getCount()+process.recipe.slag.getCount() > getSlotLimit(SLAG_SLOT))
				this.inventory.get(SLAG_SLOT).grow(process.recipe.slag.getCount());
		}
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 12;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 12;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		return 0;
	}


	@Override
	public int getComparatedSize()
	{
		return 12;
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
		return slot > SLAG_SLOT?1: 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return OUTPUT_SLOTS;
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
	}


	private LazyOptional<IItemHandler> inputHandler = registerConstantCap(
			new IEInventoryHandler(IN_SLOT_COUNT, this, FIRST_IN_SLOT, true, false)
			{
				//ignore the given slot and spread it out
				@Override
				public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
				{
					if(stack.isEmpty())
						return stack;
					stack = stack.copy();
					List<Integer> possibleSlots = new ArrayList<>(IN_SLOT_COUNT);
					for(int i = FIRST_IN_SLOT; i < IN_SLOT_COUNT; i++)
					{
						ItemStack here = inventory.get(i);
						if(here.isEmpty())
						{
							if(!simulate)
								inventory.set(i, stack);
							return ItemStack.EMPTY;
						}
						else if(ItemHandlerHelper.canItemStacksStack(stack, here)&&here.getCount() < here.getMaxStackSize())
						{
					possibleSlots.add(i);
				}
			}
			possibleSlots.sort(Comparator.comparingInt(a -> inventory.get(a).getCount()));
			for(int i : possibleSlots)
			{
				ItemStack here = inventory.get(i);
				int fillCount = Math.min(here.getMaxStackSize()-here.getCount(), stack.getCount());
				if(!simulate)
					here.grow(fillCount);
				stack.shrink(fillCount);
				if(stack.isEmpty())
					return ItemStack.EMPTY;
			}
					return stack;
				}
			});
	private LazyOptional<IItemHandler> additiveHandler = registerConstantCap(
			new IEInventoryHandler(ADDITIVE_SLOT_COUNT, this, FIRST_ADDITIVE_SLOT, true, false));
	private LazyOptional<IItemHandler> outputHandler = registerConstantCap(
			new IEInventoryHandler(OUT_SLOT_COUNT, this, FIRST_OUT_SLOT, false, true));
	private LazyOptional<IItemHandler> slagHandler = registerConstantCap(
			new IEInventoryHandler(1, this, SLAG_SLOT, false, true));
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			ArcFurnaceBlockEntity master = master();
			if(master==null)
				return LazyOptional.empty();
			if(posInMultiblock==MAIN_OUT_POS)
				return master.outputHandler.cast();
			else if(posInMultiblock==SLAG_OUT_POS)
				return master.slagHandler.cast();
				//TODO are these swapped?
			else if(new BlockPos(1, 3, 2).equals(posInMultiblock))
				return master.inputHandler.cast();
			else if(new BlockPos(3, 3, 2).equals(posInMultiblock))
				return master.additiveHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public ArcFurnaceRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected ArcFurnaceRecipe getRecipeForId(ResourceLocation id)
	{
		return ArcFurnaceRecipe.recipeList.get(id);
	}

	@Override
	@Nullable
	protected MultiblockProcess<ArcFurnaceRecipe> loadProcessFromNBT(CompoundTag tag)
	{
		String id = tag.getString("recipe");
		ArcFurnaceRecipe recipe = getRecipeForId(new ResourceLocation(id));
		if(recipe!=null)
		{
			MultiblockProcessArcFurnace process = new MultiblockProcessArcFurnace(recipe, tag.getIntArray("process_inputSlots"));
			if(tag.contains("process_inputAmounts", NBT.TAG_INT_ARRAY))
				process.setInputAmounts(tag.getIntArray("process_inputAmounts"));
			return process;
		}
		return null;
	}

	private static final Set<BlockPos> specialGuiPositions = ImmutableSet.of(
			new BlockPos(2, 0, 4),
			new BlockPos(0, 1, 4)
	);
	@Override
	public boolean canUseGui(Player player)
	{
		return formed&&(specialGuiPositions.contains(posInMultiblock)||
				(posInMultiblock.getY() > 0&&posInMultiblock.getX() > 0&&posInMultiblock.getX() < 4&&posInMultiblock.getZ()==4)
				||!isDummy());
	}

	@Override
	public ArcFurnaceBlockEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public BEContainer<ArcFurnaceBlockEntity, ?> getContainerType()
	{
		return IEContainerTypes.ARC_FURNACE;
	}

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return false;
	}

	public static class MultiblockProcessArcFurnace extends MultiblockProcessInMachine<ArcFurnaceRecipe>
	{
		public MultiblockProcessArcFurnace(ArcFurnaceRecipe recipe, int... inputSlots)
		{
			super(recipe, inputSlots);
		}

		@Override
		protected NonNullList<ItemStack> getRecipeItemOutputs(PoweredMultiblockBlockEntity<?, ArcFurnaceRecipe> multiblock)
		{
			ItemStack input = multiblock.getInventory().get(this.inputSlots[0]);
			NonNullList<ItemStack> additives = NonNullList.withSize(ADDITIVE_SLOT_COUNT, ItemStack.EMPTY);
			for(int i = 0; i < ADDITIVE_SLOT_COUNT; i++)
				additives.set(i, !multiblock.getInventory().get(FIRST_ADDITIVE_SLOT+i).isEmpty()?multiblock.getInventory().get(FIRST_ADDITIVE_SLOT+i).copy(): ItemStack.EMPTY);
			return recipe.getOutputs(input, additives);
		}

		@Override
		protected void processFinish(PoweredMultiblockBlockEntity<?, ArcFurnaceRecipe> te)
		{
			super.processFinish(te);
			te.getLevelNonnull().blockEvent(te.getBlockPos(), te.getBlockState().getBlock(), 0, 40);
		}
	}

	public boolean hasElectrodes()
	{
		for(int i = FIRST_ELECTRODE_SLOT; i < FIRST_ELECTRODE_SLOT+ELECTRODE_COUNT; i++)
			if(inventory.get(i).isEmpty())
				return false;
		return true;
	}
}