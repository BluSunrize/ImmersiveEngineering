/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.crafting.MixerRecipePotion;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MixerTileEntity extends PoweredMultiblockTileEntity<MixerTileEntity, MixerRecipe> implements
		IAdvancedSelectionBounds, IAdvancedCollisionBounds, IInteractionObjectIE
{
	public static TileEntityType<MixerTileEntity> TYPE;
	
	public MultiFluidTank tank = new MultiFluidTank(8000);
	public NonNullList<ItemStack> inventory = NonNullList.withSize(8, ItemStack.EMPTY);
	public float animation_agitator = 0;
	public boolean outputAll;

	public MixerTileEntity()
	{
		super(IEMultiblocks.MIXER, 16000, true, TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tank.readFromNBT(nbt.getCompound("tank"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 8);
		outputAll = nbt.getBoolean("outputAll");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
		nbt.put("tank", tankTag);
		if(!descPacket)
			nbt.put("inventory", Utils.writeInventory(inventory));
		nbt.putBoolean("outputAll", outputAll);
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		super.receiveMessageFromClient(message);
		if(message.contains("outputAll", NBT.TAG_BYTE))
			outputAll = message.getBoolean("outputAll");
	}

	@Override
	public void tick()
	{
		super.tick();
		if(isDummy()||isRSDisabled())
			return;

		if(world.isRemote)
		{
			if(shouldRenderAsActive())
			{
				if(Utils.RAND.nextInt(8)==0)
				{
					FluidStack fs = !tank.fluids.isEmpty()?tank.fluids.get(0): null;
					if(fs!=null)
					{
						float amount = tank.getFluidAmount()/(float)tank.getCapacity()*1.125f;
						Vec3d partPos = new Vec3d(getPos().getX()+.5f+getFacing().getXOffset()*.5f+(getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY()).getXOffset()*.5f, getPos().getY()-.0625f+amount, getPos().getZ()+.5f+getFacing().getZOffset()*.5f+(getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY()).getZOffset()*.5f);
						float r = Utils.RAND.nextFloat()*.8125f;
						float angleRad = (float)Math.toRadians(animation_agitator);
						partPos = partPos.add(r*Math.cos(angleRad), 0, r*Math.sin(angleRad));
						if(Utils.RAND.nextBoolean())
							ImmersiveEngineering.proxy.spawnBubbleFX(world, fs, partPos.x, partPos.y, partPos.z, 0, 0, 0);
						else
							ImmersiveEngineering.proxy.spawnFluidSplashFX(world, fs, partPos.x, partPos.y, partPos.z, 0, 0, 0);
					}
				}
				animation_agitator = (animation_agitator+9)%360;
			}
		}
		else
		{
			boolean update = false;
			boolean foundRecipe = false;
			if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
			{
				int tankAmount = tank.getFluidAmount();
				if(tankAmount > 0)
				{
					Set<Integer> usedInvSlots = new HashSet<>();
					for(MultiblockProcess<MixerRecipe> process : processQueue)
						if(process instanceof MultiblockProcessInMachine)
							for(int i : ((MultiblockProcessInMachine<MixerRecipe>)process).getInputSlots())
								usedInvSlots.add(i);
					NonNullList<ItemStack> components = NonNullList.withSize(this.inventory.size(), ItemStack.EMPTY);
					for(int i = 0; i < components.size(); i++)
						if(!usedInvSlots.contains(i))
							components.set(i, inventory.get(i));

					for(FluidStack fs : tank.fluids)
					{
						MixerRecipe recipe = MixerRecipe.findRecipe(fs, components);
						if(recipe!=null)
						{
							foundRecipe = true;
							MultiblockProcessInMachine process = new MultiblockProcessMixer(recipe, recipe.getUsedSlots(fs, components)).setInputTanks(0);
							if(this.addProcessToQueue(process, true))
							{
								this.addProcessToQueue(process, false);
								update = true;
							}
						}
					}
				}
			}

			if(this.tank.getFluidTypes() > 1||!foundRecipe||outputAll)
			{
				BlockPos outputPos = this.getPos().down().offset(getFacing().getOpposite(), 2);
				update |= FluidUtil.getFluidHandler(world, outputPos, getFacing()).map(output ->
				{
					boolean ret = false;
					if(!outputAll)
					{
						FluidStack inTank = this.tank.getFluid();
						if(inTank!=null)
						{
							FluidStack out = Utils.copyFluidStackWithAmount(inTank, Math.min(inTank.getAmount(), 80), false);
							int accepted = output.fill(out, FluidAction.SIMULATE);
							if(accepted > 0)
							{
								int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false),
										FluidAction.EXECUTE);
								this.tank.drain(drained, FluidAction.EXECUTE);
								ret = true;
							}
						}
					}
					else
					{
						int totalOut = 0;
						Iterator<FluidStack> it = this.tank.fluids.iterator();
						while(it.hasNext())
						{
							FluidStack fs = it.next();
							if(fs!=null)
							{
								FluidStack out = Utils.copyFluidStackWithAmount(fs, Math.min(fs.getAmount(), 80-totalOut), false);
								int accepted = output.fill(out, FluidAction.SIMULATE);
								if(accepted > 0)
								{
									int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
									MultiFluidTank.drain(drained, fs, it, FluidAction.EXECUTE);
									totalOut += drained;
									ret = true;
								}
								if(totalOut >= 80)
									break;
							}
						}
					}
					return ret;
				}).orElse(false);
			}
			if(update)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		if(ImmutableSet.of(
				new BlockPos(0, 0, 2),
				new BlockPos(1, 0, 1),
				new BlockPos(1, 0, 2),
				new BlockPos(2, 0, 0),
				new BlockPos(2, 0, 1),
				new BlockPos(2, 0, 2)
		).contains(posInMultiblock))
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return new float[]{getFacing()==Direction.WEST?.5f: 0, 0, getFacing()==Direction.NORTH?.5f: 0, getFacing()==Direction.EAST?.5f: 1, 1, getFacing()==Direction.SOUTH?.5f: 1};
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		Direction fl = getFacing();
		Direction fw = getFacing().rotateY();
		if(getIsMirrored())
			fw = fw.getOpposite();
		if(new BlockPos(0, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			float minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .125f;
			float maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .25f;
			float minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .125f;
			float maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .25f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .75f;
			maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .875f;
			minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .75f;
			maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .875f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(posInMultiblock.getX() > 0&&posInMultiblock.getY()==0&&posInMultiblock.getZ() > 0)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock.getX()==2)
				fl = fl.getOpposite();
			if(posInMultiblock.getZ()==2)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?.6875f: fl==Direction.EAST?.0625f: fw==Direction.EAST?.0625f: .6875f;
			float maxX = fl==Direction.EAST?.3125f: fl==Direction.WEST?.9375f: fw==Direction.EAST?.3125f: .9375f;
			float minZ = fl==Direction.NORTH?.6875f: fl==Direction.SOUTH?.0625f: fw==Direction.SOUTH?.0625f: .6875f;
			float maxZ = fl==Direction.SOUTH?.3125f: fl==Direction.NORTH?.9375f: fw==Direction.SOUTH?.3125f: .9375f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(new BlockPos(1, 0, 1).equals(posInMultiblock))
			{
				minX = fl==Direction.WEST?.375f: fl==Direction.EAST?.625f: fw==Direction.WEST?-.125f: 0;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.625f: fw==Direction.EAST?1.125f: 1;
				minZ = fl==Direction.NORTH?.375f: fl==Direction.SOUTH?.625f: fw==Direction.NORTH?-.125f: 0;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.625f: fw==Direction.SOUTH?1.125f: 1;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==Direction.WEST?-.125f: fl==Direction.EAST?.625f: fw==Direction.WEST?-.125f: .875f;
				maxX = fl==Direction.EAST?1.125f: fl==Direction.WEST?.375f: fw==Direction.EAST?1.125f: .125f;
				minZ = fl==Direction.NORTH?-.125f: fl==Direction.SOUTH?.625f: fw==Direction.NORTH?-.125f: .875f;
				maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?.375f: fw==Direction.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==Direction.WEST?-.125f: fl==Direction.EAST?.875f: fw==Direction.WEST?-.125f: .875f;
				maxX = fl==Direction.EAST?1.125f: fl==Direction.WEST?.125f: fw==Direction.EAST?1.125f: .125f;
				minZ = fl==Direction.NORTH?-.125f: fl==Direction.SOUTH?.875f: fw==Direction.NORTH?-.125f: .875f;
				maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?.125f: fw==Direction.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .75f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}

			return list;
		}
		else if(posInMultiblock.getX() > 0&&posInMultiblock.getY()==1&&posInMultiblock.getZ() > 0)
		{
			List<AxisAlignedBB> list = new ArrayList<>(3);
			if(posInMultiblock.getX()==2)
				fl = fl.getOpposite();
			if(posInMultiblock.getZ()==2)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?0f: fl==Direction.EAST?.1875f: fw==Direction.EAST?.1875f: 0f;
			float maxX = fl==Direction.EAST?1f: fl==Direction.WEST?.8125f: fw==Direction.EAST?1f: .8125f;
			float minZ = fl==Direction.NORTH?0f: fl==Direction.SOUTH?.1875f: fw==Direction.SOUTH?.1875f: 0f;
			float maxZ = fl==Direction.SOUTH?1f: fl==Direction.NORTH?.8125f: fw==Direction.SOUTH?1f: .8125f;
			list.add(new AxisAlignedBB(minX, -.25, minZ, maxX, 0, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?0f: fl==Direction.EAST?.0625f: fw==Direction.EAST?.0625f: .8125f;
			maxX = fl==Direction.EAST?1f: fl==Direction.WEST?.9375f: fw==Direction.EAST?.1875f: .9375f;
			minZ = fl==Direction.NORTH?0f: fl==Direction.SOUTH?.0625f: fw==Direction.SOUTH?.0625f: .8125f;
			maxZ = fl==Direction.SOUTH?1f: fl==Direction.NORTH?.9375f: fw==Direction.SOUTH?.1875f: .9375f;
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?.8125f: fl==Direction.EAST?.0625f: fw==Direction.EAST?.1875f: 0f;
			maxX = fl==Direction.EAST?.1875f: fl==Direction.WEST?.9375f: fw==Direction.EAST?1f: .8125f;
			minZ = fl==Direction.NORTH?.8125f: fl==Direction.SOUTH?.0625f: fw==Direction.SOUTH?.1875f: 0f;
			maxZ = fl==Direction.SOUTH?.1875f: fl==Direction.NORTH?.9375f: fw==Direction.SOUTH?1f: .8125f;
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			return list;
		}
		else if(new BlockPos(1, 2, 0).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = new ArrayList<>(1);
			float minX = fl==Direction.WEST?.1875f: fl==Direction.EAST?.3125f: fw==Direction.EAST?.1875f: 0f;
			float maxX = fl==Direction.EAST?.8125f: fl==Direction.WEST?.6875f: fw==Direction.EAST?1f: .8125f;
			float minZ = fl==Direction.NORTH?.1875f: fl==Direction.SOUTH?.3125f: fw==Direction.SOUTH?.1875f: 0f;
			float maxZ = fl==Direction.SOUTH?.8125f: fl==Direction.NORTH?.6875f: fw==Direction.SOUTH?1f: .8125f;
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, .625f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(new BlockPos(1, 2, 1).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = new ArrayList<>(2);
			float minX = fl==Direction.WEST?-.4375f: fl==Direction.EAST?.5625f: fw==Direction.EAST?.5625f: -.4375f;
			float maxX = fl==Direction.EAST?1.4375f: fl==Direction.WEST?.4375f: fw==Direction.EAST?1.4375f: .4375f;
			float minZ = fl==Direction.NORTH?-.4375f: fl==Direction.SOUTH?.5625f: fw==Direction.SOUTH?.5625f: -.4375f;
			float maxZ = fl==Direction.SOUTH?1.4375f: fl==Direction.NORTH?.4375f: fw==Direction.SOUTH?1.4375f: .4375f;
			list.add(new AxisAlignedBB(minX, .1875, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?0f: fl==Direction.EAST?.5f: fw==Direction.EAST?0f: .4375f;
			maxX = fl==Direction.EAST?1f: fl==Direction.WEST?.5f: fw==Direction.EAST?.5625f: 1f;
			minZ = fl==Direction.NORTH?0f: fl==Direction.SOUTH?.5f: fw==Direction.SOUTH?0f: .4375f;
			maxZ = fl==Direction.SOUTH?1f: fl==Direction.NORTH?.5f: fw==Direction.SOUTH?.5625f: 1f;
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, .875, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;

		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 0)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 2)
		);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MixerRecipe> process)
	{
		return true;
	}

	private DirectionalBlockPos getOutputPos()
	{
		return new DirectionalBlockPos(pos.offset(getFacing(), 2), getFacing());
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(
			this, this::getOutputPos, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);
	
	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, getOutputPos(), output);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MixerRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 8;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 8;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<MixerRecipe> process)
	{
		return 0;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return new int[0];
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[]{0};
	}

	@Override
	@Nonnull
	public IFluidTank[] getInternalTanks()
	{
		return new IFluidTank[]{tank};
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		MixerTileEntity master = master();
		if(master!=null&&((new BlockPos(0, 0, 1).equals(posInMultiblock)&&(side==null||side==getFacing().getOpposite()))
				||(new BlockPos(1, 0, 0).equals(posInMultiblock)&&(side==null||side==(getIsMirrored()?getFacing().rotateY(): getFacing().rotateYCCW())))))
			return master.getInternalTanks();
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return side==null||side==(getIsMirrored()?getFacing().rotateY(): getFacing().rotateYCCW());
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return side==null||side==getFacing().getOpposite();
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new IEInventoryHandler(8, this, 0, new boolean[]{true, true, true, true, true, true, true, true}, new boolean[8])
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if((facing==null||new BlockPos(2, 1, 1).equals(posInMultiblock))&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			MixerTileEntity master = master();
			if(master!=null)
				return master.insertionHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public MixerRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected MixerRecipe readRecipeFromNBT(CompoundNBT tag)
	{
		return MixerRecipe.loadFromNBT(tag);
	}

	@Override
	protected MultiblockProcess<MixerRecipe> loadProcessFromNBT(CompoundNBT tag)
	{
		MixerRecipe recipe = readRecipeFromNBT(tag);
		if(recipe!=null)
			return new MultiblockProcessMixer(recipe, tag.getIntArray("process_inputSlots")).setInputTanks(tag.getIntArray("process_inputTanks"));
		return null;
	}

	public static class MultiblockProcessMixer extends MultiblockProcessInMachine<MixerRecipe>
	{
		public MultiblockProcessMixer(MixerRecipe recipe, int... inputSlots)
		{
			super(recipe, inputSlots);
		}

		@Override
		protected List<FluidStack> getRecipeFluidOutputs(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			return Collections.emptyList();
		}

		@Override
		protected List<FluidStack> getRecipeFluidInputs(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			return Collections.emptyList();
		}

		@Override
		public boolean canProcess(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			if(!(multiblock instanceof MixerTileEntity))
				return false;
			MixerTileEntity mixer = (MixerTileEntity)multiblock;
			// we don't need to check filling since after draining 1 mB of input fluid there will be space for 1 mB of output fluid
			return mixer.energyStorage.extractEnergy(energyPerTick, true)==energyPerTick&&
					!mixer.tank.drain(Utils.copyFluidStackWithAmount(recipe.fluidInput, 1, false), FluidAction.SIMULATE).isEmpty();
		}

		@Override
		public void doProcessTick(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			int timerStep = Math.max(this.maxTicks/this.recipe.fluidAmount, 1);
			if(timerStep!=0&&this.processTick%timerStep==0)
			{
				int amount = this.recipe.fluidAmount/maxTicks;
				int leftover = this.recipe.fluidAmount%maxTicks;
				if(leftover > 0)
				{
					double distBetweenExtra = maxTicks/(double)leftover;
					if(Math.floor(processTick/distBetweenExtra)!=Math.floor((processTick-1)/distBetweenExtra))
					{
						amount++;
					}
				}
				FluidStack drained = ((MixerTileEntity)multiblock).tank.drain(Utils.copyFluidStackWithAmount(recipe.fluidInput, amount, false), FluidAction.SIMULATE);
				if(!drained.isEmpty())
				{
					NonNullList<ItemStack> components = NonNullList.withSize(this.inputSlots.length, ItemStack.EMPTY);
					for(int i = 0; i < components.size(); i++)
						components.set(i, multiblock.getInventory().get(this.inputSlots[i]));
					FluidStack output = this.recipe.getFluidOutput(drained, components);

					FluidStack fs = Utils.copyFluidStackWithAmount(output, drained.getAmount(), false);
					((MixerTileEntity)multiblock).tank.fill(fs, FluidAction.EXECUTE);
				}
			}
			super.doProcessTick(multiblock);
		}

		@Override
		protected void processFinish(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			super.processFinish(multiblock);
			if(this.recipe instanceof MixerRecipePotion)
				for(int i : this.inputSlots)
					if(!multiblock.getInventory().get(i).isEmpty()&&
							BrewingRecipeRegistry.isValidIngredient(multiblock.getInventory().get(i)))
					{
						multiblock.getInventory().get(i).shrink(1);
						if(multiblock.getInventory().get(i).getCount() <= 0)
							multiblock.getInventory().set(i, ItemStack.EMPTY);
					}
		}
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return formed;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
	}
}