/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.TileContainer;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TurretGunTileEntity extends TurretTileEntity<TurretGunTileEntity>
{
	public int cycleRender;
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
	public boolean expelCasings = false;

	public TurretGunTileEntity(BlockPos pos, BlockState state)
	{
		super(IETileTypes.TURRET_GUN.get(), pos, state);
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
			IBullet bullet = ((BulletItem)bulletStack.getItem()).getType();
			if(bullet!=null&&bullet.isValidForTurret())
			{
				ItemStack casing = bullet.getCasing(bulletStack);
				if(expelCasings||casing.isEmpty()||inventory.get(1).isEmpty()||(ItemStack.isSame(casing, inventory.get(1))&&
						inventory.get(1).getCount()+casing.getCount() <= inventory.get(1).getMaxStackSize()))
				{
					this.energyStorage.extractEnergy(energy, false);
					this.sendRenderPacket();

					Vec3 vec = getGunToTargetVec(target).normalize();

					int count = bullet.getProjectileCount(null);
					if(count==1)
					{
						Entity entBullet = getBulletEntity(level, vec, bullet);
						level.addFreshEntity(bullet.getProjectile(null, bulletStack, entBullet, false));
					}
					else
						for(int i = 0; i < count; i++)
						{
							Vec3 vecDir = vec.add(Utils.RAND.nextGaussian()*.1, Utils.RAND.nextGaussian()*.1, Utils.RAND.nextGaussian()*.1);
							Entity entBullet = getBulletEntity(level, vecDir, bullet);
							level.addFreshEntity(bullet.getProjectile(null, bulletStack, entBullet, false));
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
						sound = IESounds.revolverFire;
					level.playSound(null, getBlockPos(), sound, SoundSource.BLOCKS, 1, 1);
				}
			}
		}
	}

	protected void sendRenderPacket()
	{
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("cycle", true);
		ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> getWorldNonnull().getChunkAt(worldPosition)),
				new MessageTileSync(this, tag));
	}

	RevolvershotEntity getBulletEntity(Level world, Vec3 vecDir, IBullet type)
	{
		Vec3 gunPos = getGunPosition();
		RevolvershotEntity bullet = new RevolvershotEntity(world, gunPos.x+vecDir.x, gunPos.y+vecDir.y, gunPos.z+vecDir.z, 0, 0, 0, type);
		bullet.setDeltaMovement(vecDir);
		return bullet;
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
	public void receiveMessageFromClient(CompoundTag message)
	{
		super.receiveMessageFromClient(message);
		if(message.contains("expelCasings", NBT.TAG_BYTE))
			expelCasings = message.getBoolean("expelCasings");
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		expelCasings = nbt.getBoolean("expelCasings");
		if(!descPacket)
			ContainerHelper.loadAllItems(nbt, inventory);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putBoolean("expelCasings", expelCasings);
		if(!descPacket)
			ContainerHelper.saveAllItems(nbt, inventory);
	}

	private final LazyOptional<IItemHandler> itemHandler = registerConstantCap(
			new IEInventoryHandler(2, this, 0, new boolean[]{true, false}, new boolean[]{false, true})
	);
	public LazyOptional<IItemHandler> containerHandler = registerConstantCap(
			new IEInventoryHandler(2, this)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(!isDummy()&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY&&(facing==null||facing==Direction.DOWN||facing==this.getFacing().getOpposite()))
			return itemHandler.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public TileContainer<TurretGunTileEntity, ?> getContainerType()
	{
		return IEContainerTypes.GUN_TURRET;
	}
}