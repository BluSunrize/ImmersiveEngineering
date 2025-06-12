/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

public class TurretGunBlockEntity extends TurretBlockEntity<TurretGunBlockEntity>
{
	public static final int NUM_SLOTS = 2;

	public int cycleRender;
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	public boolean expelCasings = false;

	public TurretGunBlockEntity(BlockEntityType<TurretGunBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	protected double getRange()
	{
		return 16;
	}

	@Override
	protected boolean canActivate()
	{
		return this.energyStorage.getEnergyStored() >= IEServerConfig.MACHINES.turret_gun_consumption.get()&&!inventory.get(0).isEmpty();
	}

	@Override
	protected int getChargeupTicks()
	{
		return 5;
	}

	@Override
	protected int getActiveTicks()
	{
		return 5;
	}

	@Override
	protected boolean loopActivation()
	{
		return false;
	}

	@Override
	protected void activate()
	{
		int energy = IEServerConfig.MACHINES.turret_gun_consumption.get();
		ItemStack bulletStack = inventory.get(0);
		if(bulletStack.getItem() instanceof BulletItem&&this.energyStorage.extractEnergy(energy, true)==energy)
		{
			IBullet<?> bullet = ((BulletItem<?>)bulletStack.getItem()).getType();
			if(bullet!=null&&bullet.isValidForTurret())
			{
				ItemStack casing = bullet.getCasing(bulletStack);
				if(expelCasings||casing.isEmpty()||inventory.get(1).isEmpty()||(ItemStack.isSameItem(casing, inventory.get(1))&&
						inventory.get(1).getCount()+casing.getCount() <= inventory.get(1).getMaxStackSize()))
				{
					this.energyStorage.extractEnergy(energy, false);
					this.sendRenderPacket();

					Vec3 vec = getGunToTargetVec(target).normalize();

					int count = bullet.getProjectileCount(null);
					if(count==1)
						level.addFreshEntity(getBulletEntity(vec, bulletStack));
					else
						for(int i = 0; i < count; i++)
						{
							Vec3 vecDir = vec.add(ApiUtils.RANDOM.nextGaussian()*.1, ApiUtils.RANDOM.nextGaussian()*.1, ApiUtils.RANDOM.nextGaussian()*.1);
							level.addFreshEntity(getBulletEntity(vecDir, bulletStack));
						}
					bulletStack.shrink(1);
					if(bulletStack.getCount() <= 0)
						inventory.set(0, ItemStack.EMPTY);
					if(!casing.isEmpty())
					{
						if(expelCasings)
						{
							double cX = getBlockPos().getX()+.5;
							double cY = getBlockPos().getY()+1.375;
							double cZ = getBlockPos().getZ()+.5;
							Vec3 vCasing = vec.yRot(-1.57f);
							level.addParticle(DustParticleOptions.REDSTONE, cX+vCasing.x, cY+vCasing.y, cZ+vCasing.z, 0, 0, 0);
							ItemEntity entCasing = new ItemEntity(level, cX+vCasing.x, cY+vCasing.y, cZ+vCasing.z, casing.copy());
							entCasing.setDeltaMovement(0, -.01, 0);
							level.addFreshEntity(entCasing);
						}
						else
						{
							if(inventory.get(1).isEmpty())
								inventory.set(1, casing.copy());
							else
								inventory.get(1).grow(casing.getCount());
						}
					}
					SoundEvent sound = bullet.getSound();
					if(sound==null)
						sound = IESounds.revolverFire.value();
					level.playSound(null, getBlockPos(), sound, SoundSource.BLOCKS, 1, 1);
				}
			}
		}
	}

	protected void sendRenderPacket()
	{
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("cycle", true);
		PacketDistributor.sendToPlayersTrackingChunk(
				(ServerLevel)level, new ChunkPos(worldPosition), new MessageBlockEntitySync(getBlockPos(), tag)
		);
	}

	private Entity getBulletEntity(Vec3 vecDir, ItemStack bulletStack)
	{
		Entity shot = ((BulletItem<?>)bulletStack.getItem()).createBullet(
				level, null, getGunPosition(), vecDir, bulletStack, false
		);
		if(shot instanceof Projectile projectile)
		{
			FakePlayer fakePlayer = FakePlayerUtil.getFakePlayer(level);
			projectile.setOwner(fakePlayer);
		}
		return shot;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(slot==0)
			return stack.getItem() instanceof BulletItem;
		else
			return true;
	}

	@Override
	public void tickClient()
	{
		super.tickClient();
		if(!isDummy()&&cycleRender > 0)
			cycleRender--;
	}

	@Override
	public void receiveMessageFromServer(CompoundTag message)
	{
		if(message.contains("cycle"))
			cycleRender = 5;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		if(!descPacket)
		{
			expelCasings = nbt.getBoolean("expelCasings");
			ContainerHelper.loadAllItems(nbt, inventory, provider);
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		if(!descPacket)
		{
			nbt.putBoolean("expelCasings", expelCasings);
			ContainerHelper.saveAllItems(nbt, inventory, provider);
		}
	}

	private final IItemHandler itemHandler = new IEInventoryHandler(
			NUM_SLOTS, this, 0, new boolean[]{true, false}, new boolean[]{false, true}
	);

	public static void registerCapabilities(BECapabilityRegistrar<TurretGunBlockEntity> registrar)
	{
		TurretBlockEntity.registerCapabilitiesBase(registrar);
		registrar.register(ItemHandler.BLOCK, (be, facing) -> {
			if(!be.isDummy()&&(facing==null||facing==Direction.DOWN||facing==be.getFacing().getOpposite()))
				return be.itemHandler;
			else
				return null;
		});
	}

	@Override
	public ArgContainer<TurretGunBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.GUN_TURRET;
	}
}