package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.excavator.MineralWorldInfo;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IServerTickableMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ExcavatorLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.ExcavatorShapes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.DroppingMultiblockOutput;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ExcavatorLogic implements IServerTickableMultiblock<State>
{
	private static final Set<CapabilityPosition> ENERGY_INPUTS = Set.of(
			new CapabilityPosition(2, 0, 4, RelativeBlockFace.LEFT),
			new CapabilityPosition(2, 1, 4, RelativeBlockFace.LEFT),
			new CapabilityPosition(2, 2, 4, RelativeBlockFace.LEFT)
	);
	private static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 5);
	private static final MultiblockFace ITEM_OUTPUT = new MultiblockFace(1, 1, 6, RelativeBlockFace.BACK);
	private static final BlockPos WHEEL_CENTER = new BlockPos(1, 1, 1);
	private static final Vec3 WHEEL_CENTER_TOP = Vec3.atCenterOf(WHEEL_CENTER.above(2));
	private static final List<BlockPos> DIG_POSITIONS = Util.make(() -> {
		BlockPos belowWheelCenter = WHEEL_CENTER.offset(0, -4, 0);
		return List.of(
				belowWheelCenter,
				//Backward 1
				belowWheelCenter.offset(0, 0, 1),
				//Backward 2
				belowWheelCenter.offset(0, 0, 2),
				//Forward 1
				belowWheelCenter.offset(0, 0, -1),
				//Forward 2
				belowWheelCenter.offset(0, 0, -2),
				//Backward+Sides
				belowWheelCenter.offset(1, 0, 1),
				belowWheelCenter.offset(-1, 0, 1),
				//Center Sides
				belowWheelCenter.offset(1, 0, 0),
				belowWheelCenter.offset(-1, 0, 0),
				//Forward+Sides
				belowWheelCenter.offset(1, 0, -1),
				belowWheelCenter.offset(-1, 0, -1)
		);
	});

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final var level = context.getLevel();
		final var state = context.getState();
		state.comparators.updateComparators(context);

		float rot;
		int target = -1;
		final var wheelHelper = getWheel(level);
		if(wheelHelper==null)
			return;
		final var wheel = wheelHelper.getState();
		final var wheelCtx = wheelHelper.getContext();
		adjustWheel(level.getOrientation(), wheelCtx.getLevel().getOrientation(), wheel);
		wheel.active = state.active;
		rot = wheel.rotation;
		if(rot%45 > 40)
			target = Math.round(rot/360f*8)%8;

		if(context.getRedstoneInputValue(REDSTONE_POS, 0) <= 0)
		{
			state.active = false;
			return;
		}
		final var rawLevel = level.getRawLevel();
		MineralVein mineralVein = ExcavatorHandler.getRandomMineral(rawLevel, level.toAbsolute(WHEEL_CENTER));
		MineralMix mineral = mineralVein!=null?mineralVein.getMineral(rawLevel): null;

		int consumed = IEServerConfig.MACHINES.excavator_consumption.get();
		int extracted = state.energy.extractEnergy(consumed, true);
		if(extracted < consumed)
		{
			state.active = false;
			return;
		}
		state.energy.extractEnergy(consumed, false);
		state.active = true;

		if(target >= 0)
		{
			boolean wheelChanged = false;
			int targetDown = (target+4)%8;
			if(wheel.digStacks.get(targetDown).isEmpty())
			{
				ItemStack blocking = this.digBlocksInTheWay(level);
				if(!blocking.isEmpty())
				{
					wheel.digStacks.set(targetDown, blocking);
					wheelChanged = true;
				}
				else if(mineral!=null)
				{
					// Extracted to a method, to allow for early exiting
					if(fillBucket(mineralVein, mineral, level.toAbsolute(WHEEL_CENTER), wheel, targetDown))
						wheelChanged = true;
					mineralVein.deplete();
				}
			}
			if(!wheel.digStacks.get(target).isEmpty())
			{
				state.output.insertOrDrop(wheel.digStacks.get(target).copy(), level);
				Block b = Block.byItem(wheel.digStacks.get(target).getItem());
				if(b!=Blocks.AIR)
					spawnParticles(wheel.digStacks.get(target), level);
				wheel.digStacks.set(target, ItemStack.EMPTY);
				wheelChanged = true;
			}
			if(wheelChanged)
			{
				wheelCtx.markMasterDirty();
				wheelCtx.requestMasterBESync();
			}
		}
	}

	private void adjustWheel(
			MultiblockOrientation excavatorOrientation,
			MultiblockOrientation wheelOrientation,
			BucketWheelLogic.State wheel
	)
	{
		final var wheelFront = wheelOrientation.front().getCounterClockWise();
		wheel.reverseRotation = wheelFront!=excavatorOrientation.front();
	}

	private void spawnParticles(ItemStack stack, IMultiblockLevel level)
	{
		if(!IEServerConfig.MACHINES.excavator_particles.get())
			return;
		if(!(level.getRawLevel() instanceof ServerLevel rawLevel))
			return;
		final var facing = level.getOrientation().front();
		Axis axis = facing.getAxis();
		int sign = level.getOrientation().mirrored()?-1: 1;
		final var topCenterAbs = level.toAbsolute(WHEEL_CENTER_TOP);
		double fixPosOffset = .5*sign;
		double fixVelOffset = .075*sign;
		for(int i = 0; i < 16; i++)
		{
			double mX = (rawLevel.random.nextDouble()-.5)*.01;
			double mY = rawLevel.random.nextDouble()*-0.05D;
			double mZ = (rawLevel.random.nextDouble()-.5)*.01;
			double rndPosOffset = .2*(rawLevel.random.nextDouble()-.5);

			if(axis==Axis.Z)
				mX += fixVelOffset;
			else
				mZ += fixVelOffset;

			// TODO batch-spawn? Also port Silf's changes to this.
			rawLevel.sendParticles(
					new ItemParticleOption(ParticleTypes.ITEM, stack),
					topCenterAbs.x+axis.choose(rndPosOffset, 0, fixPosOffset),
					topCenterAbs.y,
					topCenterAbs.z+axis.choose(fixPosOffset, 0, rndPosOffset),
					0,
					mX, mY, mZ, 1
			);
		}
	}

	@Nullable
	private static IMultiblockBEHelperMaster<BucketWheelLogic.State> getWheel(IMultiblockLevel level)
	{
		if(!(level.getBlockEntity(WHEEL_CENTER) instanceof MultiblockBlockEntityMaster<?> wheelBE))
			return null;
		final var helper = wheelBE.getHelper();
		if(helper.getState() instanceof BucketWheelLogic.State)
			//noinspection unchecked
			return (IMultiblockBEHelperMaster<BucketWheelLogic.State>)helper;
		else
			return null;
	}

	private ItemStack digBlocksInTheWay(IMultiblockLevel level)
	{
		for(final BlockPos attemptPos : DIG_POSITIONS)
		{
			final ItemStack dug = digBlock(attemptPos, level);
			if(!dug.isEmpty())
				return dug;
		}
		return ItemStack.EMPTY;
	}

	private ItemStack digBlock(BlockPos relativePos, IMultiblockLevel level)
	{
		final var rawLevel = level.getRawLevel();
		if(!(rawLevel instanceof ServerLevel serverLevel))
			return ItemStack.EMPTY;
		FakePlayer fakePlayer = FakePlayerUtil.getFakePlayer(rawLevel);
		BlockState blockstate = level.getBlockState(relativePos);
		Block block = blockstate.getBlock();
		final var absolutePos = level.toAbsolute(relativePos);
		if(!blockstate.isAir()&&blockstate.getDestroySpeed(rawLevel, absolutePos)!=-1)
		{
			if(!block.canHarvestBlock(blockstate, rawLevel, absolutePos, fakePlayer))
				return ItemStack.EMPTY;
			if(block.onDestroyedByPlayer(
					blockstate, rawLevel, absolutePos, fakePlayer, true, blockstate.getFluidState()
			))
			{
				block.destroy(rawLevel, absolutePos, blockstate);

				ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
				tool.enchant(Enchantments.SILK_TOUCH, 1);
				LootContext.Builder dropContext = new Builder(serverLevel)
						.withOptionalParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(absolutePos))
						.withOptionalParameter(LootContextParams.TOOL, tool);

				List<ItemStack> itemsNullable = blockstate.getDrops(dropContext);
				NonNullList<ItemStack> items = NonNullList.create();
				items.addAll(itemsNullable);

				for(int i = 0; i < items.size(); i++)
					if(i!=0)
					{
						ItemEntity ei = new ItemEntity(EntityType.ITEM, rawLevel);
						ei.setItem(items.get(i).copy());
						ei.setPos(Vec3.atCenterOf(absolutePos));
						rawLevel.addFreshEntity(ei);
					}
				rawLevel.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, absolutePos, Block.getId(blockstate));
				if(items.size() > 0)
					return items.get(0);
			}
		}
		return ItemStack.EMPTY;
	}

	private boolean fillBucket(
			MineralVein mineralVein, MineralMix mineralMix,
			BlockPos wheelPos, BucketWheelLogic.State wheel,
			int targetDown
	)
	{
		if(mineralVein.isDepleted())
			return false;
		ItemStack ore = mineralMix.getRandomOre(ApiUtils.RANDOM);
		if(ore.isEmpty())
			return false;
		// if random number of 0-1 is smaller than the fail chance of the specific mineral
		// or if random number of 0-1 is smaller than the distance based fail chance of the vein
		if(ApiUtils.RANDOM.nextFloat() < mineralMix.failChance||ApiUtils.RANDOM.nextFloat() < mineralVein.getFailChance(wheelPos))
			wheel.digStacks.set(targetDown, mineralMix.getRandomSpoil(ApiUtils.RANDOM));
		else
			wheel.digStacks.set(targetDown, ore);
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		if(position.side()==null||ENERGY_INPUTS.contains(position))
			return ctx.getState().energyCap.cast(ctx);
		return LazyOptional.empty();
	}

	private static int computeComparatorValue(IMultiblockLevel level)
	{
		if(getWheel(level)==null)
			return 0;
		final var wheelPos = level.toAbsolute(WHEEL_CENTER);
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(level.getRawLevel(), wheelPos);
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

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter()
	{
		return ExcavatorShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState
	{
		// TODO make local?
		private boolean active = false;
		private final MutableEnergyStorage energy = new MutableEnergyStorage(64000);
		private final StoredCapability<IEnergyStorage> energyCap = new StoredCapability<>(energy);
		private final DroppingMultiblockOutput output;
		private final ComparatorManager<State> comparators;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.output = new DroppingMultiblockOutput(ITEM_OUTPUT, ctx);
			this.comparators = new ComparatorManager<>();
			this.comparators.addComparator(c -> computeComparatorValue(c.getLevel()), REDSTONE_POS);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("energy", energy.serializeNBT());
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			energy.deserializeNBT(nbt.get("energy"));
		}
	}
}
