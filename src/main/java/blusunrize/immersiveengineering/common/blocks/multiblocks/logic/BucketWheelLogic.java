package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IClientTickableMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IServerTickableMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BucketWheelLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.BucketWheelShapes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;

public class BucketWheelLogic implements IClientTickableMultiblock<State>, IServerTickableMultiblock<State>
{
	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		tickClient(context);
		if(context.getState().active&&context.getLevel().shouldTickModulo(20))
			context.requestMasterBESync();
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final var state = context.getState();
		if(state.active)
		{
			state.rotation += IEServerConfig.MACHINES.excavator_speed.get();
			state.rotation %= 360;
		}
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> ctx)
	{
		return new State();
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		return LazyOptional.empty();
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter()
	{
		return BucketWheelShapes.SHAPE_GETTER;
	}

	public static void spawnParticles(State state, ItemStack stack, IMultiblockLevel level)
	{
		if(!IEServerConfig.MACHINES.excavator_particles.get())
			return;
		if(!(level.getRawLevel() instanceof ServerLevel rawLevel))
			return;
		final var facing = level.getOrientation().front();
		Axis axis = facing.getAxis();
		// TODO test, 50% chance that it's inverse of what it should be
		int sign = (state.renderReverse^facing.getAxisDirection()==AxisDirection.NEGATIVE)?1: -1;
		final var topCenterAbs = level.toAbsolute(new Vec3(2.5, 4.5, .5));
		double fixPosOffset = .5*sign;
		double fixVelOffset = .075*sign;
		for(int i = 0; i < 16; i++)
		{
			double mX = (rawLevel.random.nextDouble()-.5)*.01;
			double mY = rawLevel.random.nextDouble()*-0.05D;
			double mZ = (rawLevel.random.nextDouble()-.5)*.01;
			double rndPosOffset = .2*(rawLevel.random.nextDouble()-.5);

			if(axis==Axis.X)
				mX += fixVelOffset;
			else
				mZ += fixVelOffset;

			// TODO batch-spawn? Also port Silf's changes to this.
			rawLevel.sendParticles(
					new ItemParticleOption(ParticleTypes.ITEM, stack),
					topCenterAbs.x+axis.choose(fixPosOffset, 0, rndPosOffset),
					topCenterAbs.y,
					topCenterAbs.z+axis.choose(rndPosOffset, 0, fixPosOffset),
					0,
					mX, mY, mZ, 1
			);
		}
	}

	public static class State implements IMultiblockState
	{
		public float rotation = 0;
		public final NonNullList<ItemStack> digStacks = NonNullList.withSize(8, ItemStack.EMPTY);
		public boolean active = false;
		// Used to adjust the direction of the wheel when installed in the excavator
		public boolean renderReverse = false;

		//TODO
		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.putFloat("rotation", rotation);
			ListTag stacksNBT = new ListTag();
			for(final var stack : digStacks)
				stacksNBT.add(stack.save(new CompoundTag()));
			nbt.put("stacks", stacksNBT);
			nbt.putBoolean("active", active);
			nbt.putBoolean("renderReverse", renderReverse);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			rotation = nbt.getFloat("rotation");
			final var stacksNBT = nbt.getList("stacks", Tag.TAG_COMPOUND);
			for(int i = 0; i < stacksNBT.size(); ++i)
				digStacks.set(i, ItemStack.of(stacksNBT.getCompound(i)));
			active = nbt.getBoolean("active");
			renderReverse = nbt.getBoolean("renderReverse");
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			writeSaveNBT(nbt);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			readSaveNBT(nbt);
		}
	}
}
