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
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TurretGunTileEntity extends TurretTileEntity
{
	public static TileEntityType<TurretGunTileEntity> TYPE;

	public int cycleRender;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
	public boolean expelCasings = false;

	public TurretGunTileEntity()
	{
		super(TYPE);
	}

	@Override
	protected double getRange()
	{
		return 16;
	}

	@Override
	protected boolean canActivate()
	{
		return this.energyStorage.getEnergyStored() >= IEConfig.MACHINES.turret_gun_consumption.get()&&!inventory.get(0).isEmpty();
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
		int energy = IEConfig.MACHINES.turret_gun_consumption.get();
		ItemStack bulletStack = inventory.get(0);
		if(!bulletStack.isEmpty()&&this.energyStorage.extractEnergy(energy, true)==energy)
		{
			IBullet bullet = ((BulletItem)bulletStack.getItem()).getType();
			if(bullet!=null&&bullet.isValidForTurret())
			{
				ItemStack casing = bullet.getCasing(bulletStack);
				if(expelCasings||casing.isEmpty()||inventory.get(1).isEmpty()||(ItemStack.areItemsEqual(casing, inventory.get(1))&&
						inventory.get(1).getCount()+casing.getCount() <= inventory.get(1).getMaxStackSize()))
				{
					this.energyStorage.extractEnergy(energy, false);
					this.sendRenderPacket();

					Vector3d vec = getGunToTargetVec(target).normalize();

					int count = bullet.getProjectileCount(null);
					if(count==1)
					{
						Entity entBullet = getBulletEntity(world, vec, bullet);
						world.addEntity(bullet.getProjectile(null, bulletStack, entBullet, false));
					}
					else
						for(int i = 0; i < count; i++)
						{
							Vector3d vecDir = vec.add(Utils.RAND.nextGaussian()*.1, Utils.RAND.nextGaussian()*.1, Utils.RAND.nextGaussian()*.1);
							Entity entBullet = getBulletEntity(world, vecDir, bullet);
							world.addEntity(bullet.getProjectile(null, bulletStack, entBullet, false));
						}
					bulletStack.shrink(1);
					if(bulletStack.getCount() <= 0)
						inventory.set(0, ItemStack.EMPTY);
					if(!casing.isEmpty())
					{
						if(expelCasings)
						{
							double cX = getPos().getX()+.5;
							double cY = getPos().getY()+1.375;
							double cZ = getPos().getZ()+.5;
							Vector3d vCasing = vec.rotateYaw(-1.57f);
							world.addParticle(RedstoneParticleData.REDSTONE_DUST, cX+vCasing.x, cY+vCasing.y, cZ+vCasing.z, 0, 0, 0);
							ItemEntity entCasing = new ItemEntity(world, cX+vCasing.x, cY+vCasing.y, cZ+vCasing.z, casing.copy());
							entCasing.setMotion(0, -.01, 0);
							world.addEntity(entCasing);
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
					world.playSound(null, getPos(), sound, SoundCategory.BLOCKS, 1, 1);
				}
			}
		}
	}

	protected void sendRenderPacket()
	{
		CompoundNBT tag = new CompoundNBT();
		tag.putBoolean("cycle", true);
		ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> getWorldNonnull().getChunkAt(pos)),
				new MessageTileSync(this, tag));
	}

	RevolvershotEntity getBulletEntity(World world, Vector3d vecDir, IBullet type)
	{
		Vector3d gunPos = getGunPosition();
		RevolvershotEntity bullet = new RevolvershotEntity(world, gunPos.x+vecDir.x, gunPos.y+vecDir.y, gunPos.z+vecDir.z, 0, 0, 0, type);
		bullet.setMotion(vecDir);
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
		return true;
	}

	@Override
	public void tick()
	{
		if(world.isRemote&&!isDummy()&&cycleRender > 0)
			cycleRender--;
		super.tick();
	}

	@Override
	public void receiveMessageFromServer(CompoundNBT message)
	{
		if(message.contains("cycle"))
			cycleRender = 5;
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		super.receiveMessageFromClient(message);
		if(message.contains("expelCasings", NBT.TAG_BYTE))
			expelCasings = message.getBoolean("expelCasings");
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		expelCasings = nbt.getBoolean("expelCasings");
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 2);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putBoolean("expelCasings", expelCasings);
		if(!descPacket)
			nbt.put("inventory", Utils.writeInventory(inventory));
	}

	private LazyOptional<IItemHandler> itemHandler = registerConstantCap(
			new IEInventoryHandler(2, this, 0, new boolean[]{true, false}, new boolean[]{false, true})
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(!isDummy()&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY&&(facing==null||facing==Direction.DOWN||facing==this.getFacing().getOpposite()))
			return itemHandler.cast();
		return super.getCapability(capability, facing);
	}
}