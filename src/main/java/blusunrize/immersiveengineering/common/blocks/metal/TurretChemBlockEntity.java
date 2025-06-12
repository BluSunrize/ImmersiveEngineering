/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import blusunrize.immersiveengineering.common.util.IESounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class TurretChemBlockEntity extends TurretBlockEntity<TurretChemBlockEntity>
{
	public static final int TANK_VOLUME = 4*FluidType.BUCKET_VOLUME;

	public FluidTank tank = new FluidTank(TANK_VOLUME);
	public boolean ignite = false;

	public TurretChemBlockEntity(BlockEntityType<TurretChemBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	protected double getRange()
	{
		return 8;
	}

	@Override
	protected boolean canActivate()
	{
		return tank.getFluidAmount() > 0&&this.energyStorage.getEnergyStored() >= IEServerConfig.MACHINES.turret_chem_consumption.get();
	}

	@Override
	protected int getChargeupTicks()
	{
		return 10;
	}

	@Override
	protected int getActiveTicks()
	{
		return 1;
	}

	@Override
	protected boolean loopActivation()
	{
		return true;
	}

	@Override
	protected void activate()
	{
		FluidStack fs = this.tank.getFluid().copy();
		if(!fs.isEmpty())
		{
			int consumed = IEServerConfig.TOOLS.chemthrower_consumption.get();
			int energy = IEServerConfig.MACHINES.turret_chem_consumption.get();
			if(consumed <= fs.getAmount()&&this.energyStorage.extractEnergy(energy, true) >= energy)
			{
				tank.drain(consumed, FluidAction.EXECUTE);
				this.energyStorage.extractEnergy(energy, false);
				Vec3 v = getGunToTargetVec(target).normalize();

				int split = 8;
				boolean isGas = fs.getFluid().is(Tags.Fluids.GASEOUS);

				float scatter = isGas?.15f: .05f;
				float range = isGas?.5f: 1f;
//				if(getUpgrades(stack).getBoolean("focus"))
//				{
//					range += .25f;
//					scatter -= .025f;
//				}
				boolean ignite = ChemthrowerHandler.isFlammable(fs.getFluid())&&this.ignite;

				FakePlayer fakePlayer = FakePlayerUtil.getFakePlayer(level);
				for(int i = 0; i < split; i++)
				{
					Vec3 vecDir = v.add(ApiUtils.RANDOM.nextGaussian()*scatter, ApiUtils.RANDOM.nextGaussian()*scatter, ApiUtils.RANDOM.nextGaussian()*scatter);
					Vec3 throwerPos = getGunPosition();
					ChemthrowerShotEntity chem = new ChemthrowerShotEntity(level, throwerPos.x+v.x*0.875, throwerPos.y+v.y*0.875,
							throwerPos.z+v.z*0.875, fs);
					chem.setOwner(fakePlayer);
					chem.setDeltaMovement(vecDir.scale(range));
					if(ignite)
						chem.igniteForSeconds(10);
					if(!level.isClientSide)
						level.addFreshEntity(chem);
				}
				if(tick%4==0)
				{
					if(ignite)
						level.playSound(null, getBlockPos(), IESounds.sprayFire.value(), SoundSource.BLOCKS, .5F, 1.5F);
					else
						level.playSound(null, getBlockPos(), IESounds.spray.value(), SoundSource.BLOCKS, .5F, .75F);
				}
			}
		}
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		if(!descPacket)
		{
			tank.readFromNBT(provider, nbt.getCompound("tank"));
			ignite = nbt.getBoolean("ignite");
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		if(!descPacket)
		{
			nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
			nbt.putBoolean("ignite", ignite);
		}
	}

	private final IFluidHandler tankCap = makeFluidInput(tank);

	public static void registerCapabilities(BECapabilityRegistrar<TurretChemBlockEntity> registrar)
	{
		TurretBlockEntity.registerCapabilitiesBase(registrar);
		registrar.register(FluidHandler.BLOCK, (be, facing) -> {
			if(!be.isDummy()&&(facing==null||facing==Direction.DOWN||facing==be.getFacing().getOpposite()))
				return be.tankCap;
			else
				return null;
		});
	}

	@Override
	public ArgContainer<TurretChemBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.CHEM_TURRET;
	}
}