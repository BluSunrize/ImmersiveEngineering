/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.excavator.MineralWorldInfo;
import blusunrize.immersiveengineering.api.multiblocks.blocks.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process_old.MultiblockProcess;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ExcavatorBlockEntity extends PoweredMultiblockBlockEntity<ExcavatorBlockEntity, MultiblockRecipe> implements
		IBlockBounds
{
	private static final BlockPos wheelCenterOffset = new BlockPos(1, 1, 1);
	public boolean active = false;

	public ExcavatorBlockEntity(BlockEntityType<ExcavatorBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.EXCAVATOR, 64000, true, type, pos, state);
	}


	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
	}

	public BlockPos getWheelCenterPos()
	{
		return this.getBlockPosForPos(wheelCenterOffset);
	}

	@Override
	protected int getComparatorValueOnMaster()
	{
		BlockPos wheelPos = getWheelCenterPos();
		if(SafeChunkUtils.getSafeBE(level, wheelPos) instanceof BucketWheelBlockEntity)
		{
			MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(level, wheelPos);
			if(info==null)
				return 0;
			if(ExcavatorHandler.mineralVeinYield==0)
				return 15;
			long totalDepletion = 0;
			List<Pair<MineralVein, Integer>> veins = info.getAllVeins();
			if(veins.isEmpty())
				return 0;
			for(Pair<MineralVein, Integer> pair : veins)
				totalDepletion += pair.getFirst().getDepletion();
			totalDepletion /= veins.size();
			float remain = (ExcavatorHandler.mineralVeinYield-totalDepletion)/(float)ExcavatorHandler.mineralVeinYield;
			return Mth.ceil(Math.max(remain, 0)*15);
		}
		return 0;
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		BlockPos wheelPos = this.getBlockPosForPos(wheelCenterOffset);
		if(level.isAreaLoaded(wheelPos, 5))
		{
			BlockEntity center = level.getBlockEntity(wheelPos);

			if(center instanceof BucketWheelBlockEntity wheel)
			{
				if(wheel!=wheel.master())
					return;
				float rot = 0;
				int target = -1;
				Direction fRot = this.getFacing().getCounterClockWise();
				boolean mirrored = getIsMirrored();

				if((wheel.getFacing()==fRot)&&(wheel.getIsMirrored()==mirrored))
				{
					if(active!=wheel.active)
						level.blockEvent(wheel.getBlockPos(), wheel.getBlockState().getBlock(), 0, active?1: 0);
					rot = wheel.rotation;
					if(rot%45 > 40)
						target = Math.round(rot/360f*8)%8;
				}
				else
					wheel.adjustStructureFacingAndMirrored(fRot,mirrored);

				if(!isRSDisabled())
				{
					MineralVein mineralVein = ExcavatorHandler.getRandomMineral(level, wheelPos);
					MineralMix mineral = mineralVein!=null?mineralVein.getMineral(level): null;

					int consumed = IEServerConfig.MACHINES.excavator_consumption.get();
					int extracted = energyStorage.extractEnergy(consumed, true);
					if(extracted >= consumed)
					{
						energyStorage.extractEnergy(consumed, false);
						active = true;

						if(target >= 0)
						{
							int targetDown = (target+4)%8;
							CompoundTag packet = new CompoundTag();
							if(wheel.digStacks.get(targetDown).isEmpty())
							{
								ItemStack blocking = this.digBlocksInTheWay(wheel);
								if(!blocking.isEmpty())
								{
									wheel.digStacks.set(targetDown, blocking);
									wheel.setChanged();
									this.markContainingBlockForUpdate(null);
								}
								else if(mineral!=null)
								{
									// Extracted to a method, to allow for early exiting
									fillBucket(mineralVein, mineral, wheelPos, wheel, targetDown);
									mineralVein.deplete();
								}
								if(!wheel.digStacks.get(targetDown).isEmpty())
								{
									packet.putInt("fill", targetDown);
									packet.put("fillStack", wheel.digStacks.get(targetDown).save(new CompoundTag()));
								}
							}
							if(!wheel.digStacks.get(target).isEmpty())
							{
								this.doProcessOutput(wheel.digStacks.get(target).copy());
								Block b = Block.byItem(wheel.digStacks.get(target).getItem());
								if(b!=Blocks.AIR)
									wheel.spawnParticles(wheel.digStacks.get(target));
								wheel.digStacks.set(target, ItemStack.EMPTY);
								wheel.setChanged();
								this.markContainingBlockForUpdate(null);
								packet.putInt("empty", target);
							}
							if(!packet.isEmpty())
							{
								packet.putInt("rotation", (int)wheel.rotation);
								ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
										new MessageBlockEntitySync(wheel, packet));
							}
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

	ItemStack digBlocksInTheWay(BucketWheelBlockEntity wheel)
	{
		BlockPos pos = wheel.getBlockPos().offset(0, -4, 0);
		ItemStack s = digBlock(pos);
		if(!s.isEmpty())
			return s;
		//Backward 1
		s = digBlock(pos.relative(getFacing(), -1));
		if(!s.isEmpty())
			return s;
		//Backward 2
		s = digBlock(pos.relative(getFacing(), -2));
		if(!s.isEmpty())
			return s;
		//Forward 1
		s = digBlock(pos.relative(getFacing(), 1));
		if(!s.isEmpty())
			return s;
		//Forward 2
		s = digBlock(pos.relative(getFacing(), 2));
		if(!s.isEmpty())
			return s;

		//Backward+Sides
		s = digBlock(pos.relative(getFacing(), -1).relative(getFacing().getClockWise()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.relative(getFacing(), -1).relative(getFacing().getCounterClockWise()));
		if(!s.isEmpty())
			return s;
		//Center Sides
		s = digBlock(pos.relative(getFacing().getClockWise()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.relative(getFacing().getCounterClockWise()));
		if(!s.isEmpty())
			return s;
		//Forward+Sides
		s = digBlock(pos.relative(getFacing(), 1).relative(getFacing().getClockWise()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.relative(getFacing(), 1).relative(getFacing().getCounterClockWise()));
		if(!s.isEmpty())
			return s;
		return ItemStack.EMPTY;
	}


	ItemStack digBlock(BlockPos pos)
	{
		if(!(level instanceof ServerLevel))
			return ItemStack.EMPTY;
		FakePlayer fakePlayer = FakePlayerUtil.getFakePlayer(level);
		BlockState blockstate = level.getBlockState(pos);
		Block block = blockstate.getBlock();
		if(!level.isEmptyBlock(pos)&&blockstate.getDestroySpeed(level, pos)!=-1)
		{
			if(!block.canHarvestBlock(blockstate, level, pos, fakePlayer))
				return ItemStack.EMPTY;
			if(block.onDestroyedByPlayer(blockstate, level, pos, fakePlayer, true, blockstate.getFluidState()))
			{
				block.destroy(level, pos, blockstate);

				ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
				tool.enchant(Enchantments.SILK_TOUCH, 1);
				LootContext.Builder dropContext = new Builder((ServerLevel)level)
						.withOptionalParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
						.withOptionalParameter(LootContextParams.TOOL, tool);

				List<ItemStack> itemsNullable = blockstate.getDrops(dropContext);
				NonNullList<ItemStack> items = NonNullList.create();
				items.addAll(itemsNullable);

				for(int i = 0; i < items.size(); i++)
					if(i!=0)
					{
						ItemEntity ei = new ItemEntity(level, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, items.get(i).copy());
						this.level.addFreshEntity(ei);
					}
				level.levelEvent(2001, pos, Block.getId(blockstate));
				if(items.size() > 0)
					return items.get(0);
			}
		}
		return ItemStack.EMPTY;
	}

	private void fillBucket(MineralVein mineralVein, MineralMix mineralMix, BlockPos wheelPos, BucketWheelBlockEntity wheel, int targetDown)
	{
		if(mineralVein.isDepleted())
			return;
		ItemStack ore = mineralMix.getRandomOre(ApiUtils.RANDOM);
		if(ore.isEmpty())
			return;
		// if random number of 0-1 is smaller than the fail chance of the specific mineral
		// or if random number of 0-1 is smaller than the distance based fail chance of the vein
		if(ApiUtils.RANDOM.nextFloat() < mineralMix.failChance || ApiUtils.RANDOM.nextFloat() < mineralVein.getFailChance(wheelPos))
			wheel.digStacks.set(targetDown, mineralMix.getRandomSpoil(ApiUtils.RANDOM));
		else
			wheel.digStacks.set(targetDown, ore);
		wheel.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(ExcavatorBlockEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return getShape(SHAPES);
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getX()==2&&posInMultiblock.getZ()==4)
			return ImmutableList.of(
					new AABB(0, 0, 0, .5f, 1, 1),
					new AABB(.5f, .25f, .25f, 1, .75f, .75f)
			);
		else if(posInMultiblock.getZ() < 3&&posInMultiblock.getY()==0&&posInMultiblock.getX()==0)
		{
			List<AABB> list = Lists.newArrayList(new AABB(.5f, 0, 0, 1, 1, 1));
			if(posInMultiblock.getZ()==2)
				list.add(new AABB(0, .5f, 0, .5f, 1, .5f));
			else if(posInMultiblock.getZ()==1)
				list.add(new AABB(0, .5f, 0, .5f, 1, 1));
			else
				list.add(new AABB(0, .5f, .5f, .5f, 1, 1));
			return list;
		}
		else if(new BlockPos(2, 2, 2).equals(posInMultiblock))
			return ImmutableList.of(
					new AABB(0, 0, .375f, 1, 1, .5f),
					new AABB(.875f, 0, 0, 1, 1, .375f)
			);
		else if(new BlockPos(2, 2, 0).equals(posInMultiblock))
			return ImmutableList.of(
					new AABB(0, 0, .5f, 1, 1, .625f),
					new AABB(.875f, 0, .625f, 1, 1, 1)
			);
		final AABB ret;
		if(new BlockPos(0, 2, 2).equals(posInMultiblock))
			ret = new AABB(0, 0, 0, 1, .5f, .5f);
		else if(new BlockPos(0, 2, 1).equals(posInMultiblock))
			ret = new AABB(0, 0, 0, 1, .5f, 1);
		else if(new BlockPos(0, 2, 0).equals(posInMultiblock))
			ret = new AABB(0, 0, .5f, 1, .5f, 1);
		else if(new BlockPos(2, 2, 2).equals(posInMultiblock))
			ret = new AABB(0, 0, .375f, 1, 1, .5f);
		else if(new BlockPos(2, 2, 1).equals(posInMultiblock))
			ret = new AABB(.875f, 0, 0, 1, 1, 1);
		else if(new BlockPos(2, 2, 0).equals(posInMultiblock))
			ret = new AABB(0, 0, .5f, 1, 1, .625f);
		else if(posInMultiblock.getX()==2&&posInMultiblock.getZ()==4)
			ret = new AABB(0, 0, 0, .5f, 1, 1);
		else if(posInMultiblock.getZ() < 3&&posInMultiblock.getY()==0&&posInMultiblock.getX()==0)
			ret = new AABB(.5f, 0, 0, 1, 1, 1);
		else if(posInMultiblock.getZ() < 3&&posInMultiblock.getY()==0&&posInMultiblock.getX()==2)
			ret = new AABB(0, 0, 0, .5f, 1, 1);
		else
			ret = new AABB(0, 0, 0, 1, 1, 1);
		return ImmutableList.of(ret);
	}

	@Override
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(
				new MultiblockFace(2, 0, 4, RelativeBlockFace.RIGHT),
				new MultiblockFace(2, 1, 4, RelativeBlockFace.RIGHT),
				new MultiblockFace(2, 2, 4, RelativeBlockFace.RIGHT)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 5)
		);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process)
	{
		return false;
	}

	private CapabilityReference<IItemHandler> output = CapabilityReference.forBlockEntityAt(this,
			() -> new DirectionalBlockPos(getBlockPos().relative(getFacing(), -1), getFacing().getOpposite()), ForgeCapabilities.ITEM_HANDLER);

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(this.output, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(level, getBlockPos().relative(getFacing(), -1), output, getFacing());
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process)
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
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process)
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
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}


	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected MultiblockRecipe getRecipeForId(Level level, ResourceLocation id)
	{
		return null;
	}

	@Override
	public void disassemble()
	{
		super.disassemble();
		BlockPos wheelPos = this.getBlockPosForPos(wheelCenterOffset);
		BlockEntity center = level.getBlockEntity(wheelPos);
		if(center instanceof BucketWheelBlockEntity)
			level.blockEvent(center.getBlockPos(), center.getBlockState().getBlock(), 0, 0);
	}
}