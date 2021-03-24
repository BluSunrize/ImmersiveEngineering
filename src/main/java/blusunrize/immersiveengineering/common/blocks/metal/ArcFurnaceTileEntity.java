/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ArcFurnaceTileEntity extends PoweredMultiblockTileEntity<ArcFurnaceTileEntity, ArcFurnaceRecipe>
		implements ISoundTile, IInteractionObjectIE, ISelectionBounds, ICollisionBounds
{
	public static final int FIRST_ELECTRODE_SLOT = 23;
	public static final int ELECTRODE_COUNT = 3;
	private static final int SLAG_SLOT = 22;
	private static final int FIRST_OUT_SLOT = 16;
	private static final int OUT_SLOT_COUNT = 6;
	private static final BlockPos SLAG_OUT_POS = new BlockPos(2, 0, 0);
	private static final BlockPos MAIN_OUT_POS = new BlockPos(2, 0, 4);
	private static final int[] OUTPUT_SLOTS;

	static
	{
		OUTPUT_SLOTS = new int[OUT_SLOT_COUNT];
		for(int i = 0; i < OUT_SLOT_COUNT; ++i)
		{
			OUTPUT_SLOTS[i] = FIRST_OUT_SLOT+i;
		}
	}

	public NonNullList<ItemStack> inventory = NonNullList.withSize(26, ItemStack.EMPTY);
	public int pouringMetal = 0;
	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntityAt(this,
			() -> new DirectionalBlockPos(this.getBlockPosForPos(MAIN_OUT_POS).offset(getFacing(), -1), getFacing().getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	private CapabilityReference<IItemHandler> slagOut = CapabilityReference.forTileEntityAt(this,
			() -> new DirectionalBlockPos(this.getBlockPosForPos(SLAG_OUT_POS).offset(getFacing()), getFacing().getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	public ArcFurnaceTileEntity()
	{
		super(IEMultiblocks.ARC_FURNACE, 64000, true, IETileTypes.ARC_FURNACE.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 26);
		else
		{
			byte electrodeStatus = nbt.getByte("electrodeStatus");
			for(int i = 0; i < ELECTRODE_COUNT; ++i)
			{
				boolean hasElectrodeServer = (electrodeStatus&1)!=0;
				int slot = FIRST_ELECTRODE_SLOT+i;
				boolean hasElectrodeClient = !inventory.get(slot).isEmpty();
				if(hasElectrodeServer&&!hasElectrodeClient)
					inventory.set(slot, new ItemStack(Misc.graphiteElectrode));
				else if(!hasElectrodeServer&&hasElectrodeClient)
					inventory.set(slot, ItemStack.EMPTY);
				electrodeStatus >>>= 1;
			}
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
			nbt.put("inventory", Utils.writeInventory(inventory));
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
	public void tick()
	{
		super.tick();
		if(isDummy())
			return;
		if(world.isRemote)
		{
			if(pouringMetal > 0)
				pouringMetal--;
			if(shouldRenderAsActive())
				for(int i = 0; i < 4; i++)
				{
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5-.25*getFacing().getXOffset(),
								getPos().getY()+2.9, getPos().getZ()+.5-.25*getFacing().getZOffset(),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5+(getFacing()==Direction.EAST?-.25: .25),
								getPos().getY()+2.9, getPos().getZ()+.5+(getFacing()==Direction.SOUTH?.25: -.25),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5+(getFacing()==Direction.WEST?.25: -.25),
								getPos().getY()+2.9, getPos().getZ()+.5+(getFacing()==Direction.NORTH?-.25: .25),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
				}
		}
		else if(!isRSDisabled()&&energyStorage.getEnergyStored() > 0)
		{
			if(this.tickedProcesses > 0)
				for(int i = FIRST_ELECTRODE_SLOT; i < FIRST_ELECTRODE_SLOT+ELECTRODE_COUNT; i++)
					if(this.inventory.get(i).attemptDamageItem(1, Utils.RAND, null))
					{
						this.inventory.set(i, ItemStack.EMPTY);
						//						updateClient = true;
						//						update = true;
					}

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

				NonNullList<ItemStack> additives = NonNullList.withSize(4, ItemStack.EMPTY);
				for(int i = 0; i < 4; i++)
					if(!inventory.get(12+i).isEmpty())
					{
						additives.set(i, inventory.get(12+i).copy());
						if(usedInvSlots.containsKey(12+i))
							additives.get(i).shrink(usedInvSlots.get(12+i));
					}

				for(int slot = 0; slot < 12; slot++)
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

			if(world.getGameTime()%8==0)
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
	public boolean receiveClientEvent(int id, int type)
	{
		if(id==0)
			pouringMetal = type;
		return super.receiveClientEvent(id, type);
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		//		if(renderAABB==null)
		//			if(posInMultiblock==17)
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+3,zCoord+(facing==4||facing==5?3:2));
		//			else
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
		//		return renderAABB;
		return new AxisAlignedBB(getPos().getX()-(getFacing().getAxis()==Axis.Z?2: 1), getPos().getY(), getPos().getZ()-(getFacing().getAxis()==Axis.X?2: 1), getPos().getX()+(getFacing().getAxis()==Axis.Z?3: 2), getPos().getY()+3, getPos().getZ()+(getFacing().getAxis()==Axis.X?3: 2));
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> COLLISION_SHAPES =
			CachedShapesWithTransform.createForMultiblock(ArcFurnaceTileEntity::getCollisionShape);

	@Nonnull
	@Override
	public VoxelShape getCollisionShape(ISelectionContext ctx)
	{
		return getShape(COLLISION_SHAPES);
	}

	private static List<AxisAlignedBB> getCollisionShape(BlockPos posInMultiblock)
	{
		if(ImmutableSet.of(
				new BlockPos(3, 0, 4),
				new BlockPos(1, 0, 4)
		).contains(posInMultiblock))
			return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, .5f, .5625f));
		else if(posInMultiblock.getY()==0&&posInMultiblock.getZ() > 0&&!posInMultiblock.equals(new BlockPos(2, 0, 4)))
			return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, .5f, 1));
		else if(new BlockPos(0, 1, 4).equals(posInMultiblock))
			return ImmutableList.of(new AxisAlignedBB(0, 0, .5f, 1, 1, 1));
		else if(new MutableBoundingBox(1, 1, 1, 3, 1, 2)
				.isVecInside(posInMultiblock))
		{
			AxisAlignedBB aabb;
			if(posInMultiblock.getX()==2)
				aabb = new AxisAlignedBB(0, 0.5, 0, 1, 1, 1);
			else
				aabb = Utils.flipBox(false, posInMultiblock.getX()==3,
						new AxisAlignedBB(0.125, 0.5, 0.125, 1, 1, 0.875));
			if(posInMultiblock.getZ()==2)
				aabb = aabb.offset(0, 0, 0.875);
			return ImmutableList.of(aabb);
		}
		else if(ImmutableSet.of(
				new BlockPos(4, 1, 1),
				new BlockPos(0, 1, 1)
		).contains(posInMultiblock))
			return Utils.flipBoxes(false, posInMultiblock.getX()==4,
					new AxisAlignedBB(.125f, .125f, 0, .375f, .375f, 1));
		else if(posInMultiblock.getZ()==0&&posInMultiblock.getY()==1&&posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3)
			return ImmutableList.of(new AxisAlignedBB(0, 0, .25f, 1, 1, 1));
		else if(new BlockPos(2, 3, 0).equals(posInMultiblock))
			return ImmutableList.of(new AxisAlignedBB(0, 0, .375f, 1, 1, .625f));
		else if(new BlockPos(2, 4, 0).equals(posInMultiblock))
			return ImmutableList.of(new AxisAlignedBB(0, 0, .3125f, 1, .9375f, 1));
		else if(new BlockPos(2, 4, 1).equals(posInMultiblock))
			return ImmutableList.of(new AxisAlignedBB(0, .625f, 0, 1, .9375f, 1));
		else if(new BlockPos(2, 4, 2).equals(posInMultiblock))
			return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, .9375f, .875f));
		else if(ImmutableSet.of(
				new BlockPos(3, 2, 4),
				new BlockPos(1, 2, 4),
				new BlockPos(3, 3, 0),
				new BlockPos(1, 3, 0),
				new BlockPos(3, 4, 0),
				new BlockPos(1, 4, 0)
		).contains(posInMultiblock))
			return Utils.flipBoxes(false, posInMultiblock.getX()==3,
					new AxisAlignedBB(.5f, 0, 0, 1, 1, 1));
		else
			return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(ArcFurnaceTileEntity::getShape);

	@Override
	public VoxelShape getSelectionShape(@Nullable ISelectionContext ctx)
	{
		return getShape(SHAPES);
	}

	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock)
	{
		if(new BlockPos(0, 0, 4).equals(posInMultiblock))
			return ImmutableList.of(
					new AxisAlignedBB(0, 0, 0, 1, .5f, 1),
					new AxisAlignedBB(0.125, .5f, 0.625, 0.25, 1, 0.875),
					new AxisAlignedBB(0.75, .5f, 0.625, 0.875, 1, 0.875)
			);
		else if(posInMultiblock.getZ()==0&&posInMultiblock.getY()==1&&posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3)
			return ImmutableList.of(
					new AxisAlignedBB(0, 0, 0.25, 1, 1, 1),
					new AxisAlignedBB(0.25, .25f, 0, 0.75, .75, 0.25));
		else if(posInMultiblock.getX()%4==0&&posInMultiblock.getZ() <= 2)
		{
			List<AxisAlignedBB> list = posInMultiblock.getY()==0?Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1)): new ArrayList<>(2);
			final boolean flip = posInMultiblock.getX()==4;
			double minX = !flip?.5f: 0;
			double maxX = flip?.5f: 1;
			if(posInMultiblock.getX()!=3)
				list.add(new AxisAlignedBB(minX, posInMultiblock.getY()==0?.5: 0, 0, maxX, 1, 1));
			if(posInMultiblock.getY()==0)
			{
				int move = (4-posInMultiblock.getZ())-2;
				minX = !flip?.125f: .625f;
				maxX = !flip?.375f: .875f;
				AxisAlignedBB aabb = new AxisAlignedBB(minX, .6875, -1.625f, maxX, .9375, 0.625);
				aabb = aabb.offset(0, 0, move);
				list.add(aabb);

				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				aabb = new AxisAlignedBB(minX, .6875, 0.375, maxX, .9375, 0.625);
				aabb = aabb.offset(0, 0, move);
				list.add(aabb);

				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				aabb = new AxisAlignedBB(minX, .6875, -1.625f, maxX, .9375, -1.375f);
				aabb = aabb.offset(0, 0, move);
				list.add(aabb);
			}
			else if(posInMultiblock.getY()==1)
			{
				int move = (4-posInMultiblock.getZ())-2;
				minX = !flip?.125f: .625f;
				maxX = !flip?.375f: .875f;
				AxisAlignedBB aabb = new AxisAlignedBB(minX, .125, -1.625f, maxX, .375, .625f);
				aabb = aabb.offset(0, 0, move);
				list.add(aabb);

				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				aabb = new AxisAlignedBB(minX, .125, 0.375, maxX, .375, 0.625);
				aabb = aabb.offset(0, 0, move);
				if(posInMultiblock.getX()==0)
					aabb = aabb.offset(0, .6875, 0);
				list.add(aabb);
				if(posInMultiblock.getX()==0)
				{
					minX = !flip?.125f: .625f;
					maxX = !flip?.375f: .875f;
					aabb = new AxisAlignedBB(minX, .375, 0.375, maxX, 1.0625, 0.625);
					aabb = aabb.offset(0, 0, move);
					list.add(aabb);
				}
				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				aabb = new AxisAlignedBB(minX, .125, -1.625f, maxX, .375, -1.375f);
				aabb = aabb.offset(0, 0, move);
				list.add(aabb);
			}
			else if(ImmutableSet.of(
					new BlockPos(4, 2, 2),
					new BlockPos(0, 2, 2)
			).contains(posInMultiblock))
			{
				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				list.add(new AxisAlignedBB(minX, .25, 0.25, maxX, .75, 0.75));
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

	@Override
	public int getComparatorInputOverride()
	{
		if(new BlockPos(2, 4, 2).equals(posInMultiblock))
		{
			ArcFurnaceTileEntity master = master();
			if(master!=null)
			{
				float f = 0;
				for(int i = FIRST_ELECTRODE_SLOT; i < FIRST_ELECTRODE_SLOT+ELECTRODE_COUNT; i++)
					if(!master.inventory.get(i).isEmpty())
						f += 1-(master.inventory.get(i).getDamage()/(float)master.inventory.get(i).getMaxDamage());
				return MathHelper.floor(Math.max(f/3f, 0)*15);
			}
		}
		return super.getComparatorInputOverride();
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean shouldRenderAsActive()
	{
		return hasElectrodes()&&super.shouldRenderAsActive();
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
			BlockPos pos = getPos().add(0, -1, 0).offset(getFacing(), -2);
			Utils.dropStackAtPos(world, pos, output, getFacing());
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
	public void doGraphicalUpdates(int slot)
	{
	}


	private LazyOptional<IItemHandler> inputHandler = registerConstantCap(
			new IEInventoryHandler(12, this, 0, true, false)
	{
		//ignore the given slot and spread it out
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(stack.isEmpty())
				return stack;
			stack = stack.copy();
			List<Integer> possibleSlots = new ArrayList<>(12);
			for(int i = 0; i < 12; i++)
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
			new IEInventoryHandler(4, this, 12, true, false));
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
			ArcFurnaceTileEntity master = master();
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
	protected MultiblockProcess<ArcFurnaceRecipe> loadProcessFromNBT(CompoundNBT tag)
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
	public boolean canUseGui(PlayerEntity player)
	{
		return formed&&(specialGuiPositions.contains(posInMultiblock)||
				(posInMultiblock.getY() > 0&&posInMultiblock.getX() > 0&&posInMultiblock.getX() < 4&&posInMultiblock.getZ()==4)
				||!isDummy());
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
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
		protected NonNullList<ItemStack> getRecipeItemOutputs(PoweredMultiblockTileEntity<?, ArcFurnaceRecipe> multiblock)
		{
			ItemStack input = multiblock.getInventory().get(this.inputSlots[0]);
			NonNullList<ItemStack> additives = NonNullList.withSize(4, ItemStack.EMPTY);
			for(int i = 0; i < 4; i++)
				additives.set(i, !multiblock.getInventory().get(12+i).isEmpty()?multiblock.getInventory().get(12+i).copy(): ItemStack.EMPTY);
			return recipe.getOutputs(input, additives);
		}

		@Override
		protected void processFinish(PoweredMultiblockTileEntity<?, ArcFurnaceRecipe> te)
		{
			super.processFinish(te);
			te.getWorldNonnull().addBlockEvent(te.getPos(), te.getBlockState().getBlock(), 0, 40);
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