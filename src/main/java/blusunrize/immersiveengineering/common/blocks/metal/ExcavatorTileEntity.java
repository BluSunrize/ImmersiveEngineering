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
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExcavatorTileEntity extends PoweredMultiblockTileEntity<ExcavatorTileEntity, IMultiblockRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds
{
	public static TileEntityType<ExcavatorTileEntity> TYPE;
	private static final BlockPos wheelCenterOffset = new BlockPos(4, 1, 1);
	public boolean active = false;

	public ExcavatorTileEntity()
	{
		super(IEMultiblocks.EXCAVATOR, 64000, true, TYPE);
	}


	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(!this.isRedstonePos())
			return 0;
		BlockPos wheelPos = this.getBlockPosForPos(wheelCenterOffset);
		if(world.isBlockLoaded(wheelPos)&&world.getTileEntity(wheelPos) instanceof BucketWheelTileEntity)
		{
			MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(world, wheelPos.getX() >> 4, wheelPos.getZ() >> 4);
			if(info==null)
				return 0;
			float remain = (ExcavatorHandler.mineralVeinCapacity-info.depletion)/(float)ExcavatorHandler.mineralVeinCapacity;
			return MathHelper.floor(Math.max(remain, 0)*15);
		}
		return 0;
	}

	@Override
	public void tick()
	{
		super.tick();
		if(isDummy())
			return;
		BlockPos wheelPos = this.getBlockPosForPos(wheelCenterOffset);
		if(!world.isRemote&&world.isBlockLoaded(wheelPos))
		{
			TileEntity center = world.getTileEntity(wheelPos);

			if(center instanceof BucketWheelTileEntity)
			{
				float rot = 0;
				int target = -1;
				BucketWheelTileEntity wheel = ((BucketWheelTileEntity)center);
				Direction fRot = this.facing.rotateYCCW();
				if(wheel.facing==fRot)
				{
					if(active!=wheel.active)
						world.addBlockEvent(wheel.getPos(), wheel.getBlockState().getBlock(), 0, active?1: 0);
					rot = wheel.rotation;
					if(rot%45 > 40)
						target = Math.round(rot/360f*8)%8;
				}

				if(wheel.facing!=fRot||wheel.mirrored!=this.mirrored)
				{
					for(int h = -3; h <= 3; h++)
						for(int w = -3; w <= 3; w++)
						{
							TileEntity te = world.getTileEntity(wheelPos.add(0, h, 0).offset(facing, w));
							if(te instanceof BucketWheelTileEntity)
							{
								((BucketWheelTileEntity)te).facing = fRot;
								((BucketWheelTileEntity)te).mirrored = this.mirrored;
								te.markDirty();
								((BucketWheelTileEntity)te).markContainingBlockForUpdate(null);
								world.addBlockEvent(te.getPos(), te.getBlockState().getBlock(), 255, 0);
							}
						}
				}

				if(!isRSDisabled())
				{
					ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(world, wheelPos.getX() >> 4, wheelPos.getZ() >> 4);

					int consumed = IEConfig.MACHINES.excavator_consumption.get();
					int extracted = energyStorage.extractEnergy(consumed, true);
					if(extracted >= consumed)
					{
						energyStorage.extractEnergy(consumed, false);
						active = true;

						if(target >= 0)
						{
							int targetDown = (target+4)%8;
							CompoundNBT packet = new CompoundNBT();
							if(wheel.digStacks.get(targetDown).isEmpty())
							{
								ItemStack blocking = this.digBlocksInTheWay(wheel);
								BlockPos lowGroundPos = wheelPos.add(0, -5, 0);
								if(!blocking.isEmpty())
								{
									wheel.digStacks.set(targetDown, blocking);
									wheel.markDirty();
									this.markContainingBlockForUpdate(null);
								}
								else if(mineral!=null)
								{
									ItemStack ore = mineral.getRandomOre(Utils.RAND);
									float configChance = Utils.RAND.nextFloat();
									float failChance = Utils.RAND.nextFloat();
									if(!ore.isEmpty()&&configChance > IEConfig.MACHINES.excavator_fail_chance.get()&&failChance > mineral.failChance)
									{
										wheel.digStacks.set(targetDown, ore);
										wheel.markDirty();
										this.markContainingBlockForUpdate(null);
									}
									ExcavatorHandler.depleteMinerals(world, wheelPos.getX() >> 4, wheelPos.getZ() >> 4);
								}
								if(!wheel.digStacks.get(targetDown).isEmpty())
								{
									packet.putInt("fill", targetDown);
									packet.put("fillStack", wheel.digStacks.get(targetDown).write(new CompoundNBT()));
								}
							}
							if(!wheel.digStacks.get(target).isEmpty())
							{
								this.doProcessOutput(wheel.digStacks.get(target).copy());
								Block b = Block.getBlockFromItem(wheel.digStacks.get(target).getItem());
								if(b!=null&&b!=Blocks.AIR)
									wheel.particleStack = wheel.digStacks.get(target).copy();
								wheel.digStacks.set(target, ItemStack.EMPTY);
								wheel.markDirty();
								this.markContainingBlockForUpdate(null);
								packet.putInt("empty", target);
							}
							if(!packet.isEmpty())
								ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)),
										new MessageTileSync(wheel, packet));
						}
					}
					else if(active)
						active = false;
				}
				else if(active)
				{
					active = false;
				}
			}
		}
	}

	ItemStack digBlocksInTheWay(BucketWheelTileEntity wheel)
	{
		BlockPos pos = wheel.getPos().add(0, -4, 0);
		ItemStack s = digBlock(pos);
		if(!s.isEmpty())
			return s;
		//Backward 1
		s = digBlock(pos.offset(facing, -1));
		if(!s.isEmpty())
			return s;
		//Backward 2
		s = digBlock(pos.offset(facing, -2));
		if(!s.isEmpty())
			return s;
		//Forward 1
		s = digBlock(pos.offset(facing, 1));
		if(!s.isEmpty())
			return s;
		//Forward 2
		s = digBlock(pos.offset(facing, 2));
		if(!s.isEmpty())
			return s;

		//Backward+Sides
		s = digBlock(pos.offset(facing, -1).offset(facing.rotateY()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.offset(facing, -1).offset(facing.rotateYCCW()));
		if(!s.isEmpty())
			return s;
		//Center Sides
		s = digBlock(pos.offset(facing.rotateY()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.offset(facing.rotateYCCW()));
		if(!s.isEmpty())
			return s;
		//Forward+Sides
		s = digBlock(pos.offset(facing, 1).offset(facing.rotateY()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.offset(facing, 1).offset(facing.rotateYCCW()));
		if(!s.isEmpty())
			return s;
		return ItemStack.EMPTY;
	}


	ItemStack digBlock(BlockPos pos)
	{
		if(!(world instanceof ServerWorld))
			return ItemStack.EMPTY;
		FakePlayer fakePlayer = FakePlayerUtil.getFakePlayer(world);
		BlockState blockstate = world.getBlockState(pos);
		Block block = blockstate.getBlock();
		if(block!=null&&!world.isAirBlock(pos)&&blockstate.getPlayerRelativeBlockHardness(fakePlayer, world, pos)!=0)
		{
			if(!block.canHarvestBlock(blockstate, world, pos, fakePlayer))
				return ItemStack.EMPTY;
			block.onBlockHarvested(world, pos, blockstate, fakePlayer);
			if(block.removedByPlayer(blockstate, world, pos, fakePlayer, true, blockstate.getFluidState()))
			{
				block.onPlayerDestroy(world, pos, blockstate);

				ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
				tool.addEnchantment(Enchantments.SILK_TOUCH, 1);
				LootContext.Builder dropContext = new Builder((ServerWorld)world)
						.withNullableParameter(LootParameters.POSITION, pos)
						.withNullableParameter(LootParameters.TOOL, tool);

				List<ItemStack> itemsNullable = blockstate.getDrops(dropContext);
				NonNullList<ItemStack> items = NonNullList.create();
				items.addAll(itemsNullable);
				ForgeEventFactory.fireBlockHarvesting(items, world, pos, blockstate, 0, 1.0f, true, fakePlayer);

				for(int i = 0; i < items.size(); i++)
					if(i!=0)
					{
						ItemEntity ei = new ItemEntity(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, items.get(i).copy());
						this.world.addEntity(ei);
					}
				world.playEvent(2001, pos, Block.getStateId(blockstate));
				if(items.size() > 0)
					return items.get(0);
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public float[] getBlockBounds()
	{
		Direction fl = facing;
		Direction fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();

		if(new BlockPos(3, 2, 0).equals(posInMultiblock))
			return new float[]{fl==Direction.EAST?.5f: 0, 0, fl==Direction.SOUTH?.5f: 0, fl==Direction.WEST?.5f: 1, .5f, fl==Direction.NORTH?.5f: 1};
		if(new BlockPos(4, 2, 0).equals(posInMultiblock))
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(new BlockPos(5, 2, 0).equals(posInMultiblock))
			return new float[]{fl==Direction.WEST?.5f: 0, 0, fl==Direction.NORTH?.5f: 0, fl==Direction.EAST?.5f: 1, .5f, fl==Direction.SOUTH?.5f: 1};

		if(new BlockPos(3, 2, 2).equals(posInMultiblock))
			return new float[]{fl==Direction.EAST?.5f: fl==Direction.WEST?.375f: 0, 0, fl==Direction.SOUTH?.5f: fl==Direction.NORTH?.375f: 0, fl==Direction.WEST?.5f: fl==Direction.EAST?.625f: 1, 1, fl==Direction.NORTH?.5f: fl==Direction.SOUTH?.625f: 1};
		if(new BlockPos(4, 2, 2).equals(posInMultiblock))
			return new float[]{fw==Direction.EAST?.875f: 0, 0, fw==Direction.SOUTH?.875f: 0, fw==Direction.WEST?.125f: 1, 1, fw==Direction.NORTH?.125f: 1};
		if(new BlockPos(5, 2, 2).equals(posInMultiblock))
			return new float[]{fl==Direction.WEST?.5f: fl==Direction.EAST?.375f: 0, 0, fl==Direction.NORTH?.5f: fl==Direction.SOUTH?.375f: 0, fl==Direction.EAST?.5f: fl==Direction.WEST?.625f: 1, 1, fl==Direction.SOUTH?.5f: fl==Direction.NORTH?.625f: 1};

		if(posInMultiblock.getX()==1&&posInMultiblock.getZ()==2)
			return new float[]{fw==Direction.WEST?.5f: 0, 0, fw==Direction.NORTH?.5f: 0, fw==Direction.EAST?.5f: 1, 1, fw==Direction.SOUTH?.5f: 1};
		if(posInMultiblock.getX() >= 3&&posInMultiblock.getY()==0&&posInMultiblock.getZ()==0)
			return new float[]{fw==Direction.EAST?.5f: 0, 0, fw==Direction.SOUTH?.5f: 0, fw==Direction.WEST?.5f: 1, 1, fw==Direction.NORTH?.5f: 1};
		if(posInMultiblock.getX() >= 3&&posInMultiblock.getY()==0&&posInMultiblock.getZ()==2)
			return new float[]{fw==Direction.WEST?.5f: 0, 0, fw==Direction.NORTH?.5f: 0, fw==Direction.EAST?.5f: 1, 1, fw==Direction.SOUTH?.5f: 1};


		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		Direction fl = facing;
		Direction fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();

		if(posInMultiblock.getX()==1&&posInMultiblock.getZ()==2)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(fw==Direction.WEST?.5f: 0, 0, fw==Direction.NORTH?.5f: 0, fw==Direction.EAST?.5f: 1, 1, fw==Direction.SOUTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fw==Direction.EAST?.5f: fw==Direction.WEST?0: .25f, .25f, fw==Direction.SOUTH?.5f: fw==Direction.NORTH?0: .25f, fw==Direction.WEST?.5f: fw==Direction.EAST?1: .75f, .75f, fw==Direction.NORTH?.5f: fw==Direction.SOUTH?1: .75f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(posInMultiblock.getX() >= 3&&posInMultiblock.getY()==0&&posInMultiblock.getZ()==0)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(fw==Direction.EAST?.5f: 0, 0, fw==Direction.SOUTH?.5f: 0, fw==Direction.WEST?.5f: 1, 1, fw==Direction.NORTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock.getX()==3)
				list.add(new AxisAlignedBB(fw==Direction.WEST||fl==Direction.EAST?.5f: 0, .5f, fw==Direction.NORTH||fl==Direction.SOUTH?.5f: 0, fw==Direction.EAST||fl==Direction.WEST?.5f: 1, 1, fw==Direction.SOUTH||fl==Direction.NORTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			else if(posInMultiblock.getX()==4)
				list.add(new AxisAlignedBB(fw==Direction.WEST?.5f: 0, .5f, fw==Direction.NORTH?.5f: 0, fw==Direction.EAST?.5f: 1, 1, fw==Direction.SOUTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			else
				list.add(new AxisAlignedBB(fw==Direction.WEST||fl==Direction.WEST?.5f: 0, .5f, fw==Direction.NORTH||fl==Direction.NORTH?.5f: 0, fw==Direction.EAST||fl==Direction.EAST?.5f: 1, 1, fw==Direction.SOUTH||fl==Direction.SOUTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(new BlockPos(3, 2, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(fl==Direction.EAST?.5f: fl==Direction.WEST?.375f: 0, 0, fl==Direction.SOUTH?.5f: fl==Direction.NORTH?.375f: 0, fl==Direction.WEST?.5f: fl==Direction.EAST?.625f: 1, 1, fl==Direction.NORTH?.5f: fl==Direction.SOUTH?.625f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fl==Direction.EAST?.625f: fw==Direction.EAST?.875f: 0, 0, fl==Direction.SOUTH?.625f: fw==Direction.SOUTH?.875f: 0, fl==Direction.WEST?.375f: fw==Direction.WEST?.125f: 1, 1, fl==Direction.NORTH?.375f: fw==Direction.NORTH?.125f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(new BlockPos(5, 2, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(fl==Direction.WEST?.5f: fl==Direction.EAST?.375f: 0, 0, fl==Direction.NORTH?.5f: fl==Direction.SOUTH?.375f: 0, fl==Direction.EAST?.5f: fl==Direction.WEST?.625f: 1, 1, fl==Direction.SOUTH?.5f: fl==Direction.NORTH?.625f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fl==Direction.WEST?.625f: fw==Direction.EAST?.875f: 0, 0, fl==Direction.NORTH?.625f: fw==Direction.SOUTH?.875f: 0, fl==Direction.EAST?.375f: fw==Direction.WEST?.125f: 1, 1, fl==Direction.SOUTH?.375f: fw==Direction.NORTH?.125f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
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
				new BlockPos(1, 0, 2),
				new BlockPos(1, 1, 2),
				new BlockPos(1, 2, 2)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 0)
		);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return false;
	}

	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(getPos().offset(facing, -1), facing.getOpposite()), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(this.output, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, getPos().offset(facing, -1), output, facing);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 0;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 0;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return 0;
	}


	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
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
		return new int[0];
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
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
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}


	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(CompoundNBT tag)
	{
		return null;
	}

	@Override
	public void disassemble()
	{
		super.disassemble();
		BlockPos wheelPos = this.getBlockPosForPos(wheelCenterOffset);
		TileEntity center = world.getTileEntity(wheelPos);
		if(center instanceof BucketWheelTileEntity)
			world.addBlockEvent(center.getPos(), center.getBlockState().getBlock(), 0, 0);
	}
}