package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public class TileEntityTurretGun extends TileEntityTurret
{
	public int cycleRender;
	private ItemStack[] inventory = new ItemStack[2];
	public boolean expelCasings = false;

	@Override
	protected double getRange()
	{
		return 16;
	}
	@Override
	protected boolean canActivate()
	{
		return this.energyStorage.getEnergyStored()>= IEConfig.Machines.turret_gun_consumption && inventory[0]!=null;
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
		int energy = IEConfig.Machines.turret_gun_consumption;
		ItemStack bulletStack = inventory[0];
		if(bulletStack!=null && this.energyStorage.extractEnergy(energy,true)==energy)
		{
			String key = ItemNBTHelper.getString(bulletStack, "bullet");
			IBullet bullet = BulletHandler.getBullet(key);
			if(bullet!=null&&bullet.isValidForTurret())
			{
				ItemStack casing = bullet.getCasing(bulletStack);
				if(expelCasings||casing==null||inventory[1]==null||(OreDictionary.itemMatches(casing, inventory[1], false)&&inventory[1].stackSize+casing.stackSize <= inventory[1].getMaxStackSize()))
				{
					this.energyStorage.extractEnergy(energy,false);
					NBTTagCompound tag = new NBTTagCompound();
					tag.setBoolean("cycle", true);
					ImmersiveEngineering.packetHandler.sendToAll(new MessageTileSync(this, tag));

					double dX = target.posX-(getPos().getX()+.5);
					double dY = target.posY-(getPos().getY()+.5);
					double dZ = target.posZ-(getPos().getZ()+.5);
					Vec3d vec = new Vec3d(dX, dY, dZ).normalize();

					int count = bullet.getProjectileCount(null, bulletStack);
					if(count==1)
					{
						Entity entBullet = getBulletEntity(worldObj, vec, bullet);
						worldObj.spawnEntityInWorld(bullet.getProjectile(null, bulletStack, entBullet, false));
					} else
						for(int i = 0; i < count; i++)
						{
							Vec3d vecDir = vec.addVector(worldObj.rand.nextGaussian()*.1, worldObj.rand.nextGaussian()*.1, worldObj.rand.nextGaussian()*.1);
							Entity entBullet = getBulletEntity(worldObj, vecDir, bullet);
							worldObj.spawnEntityInWorld(bullet.getProjectile(null, bulletStack, entBullet, false));
						}
					if(--bulletStack.stackSize<=0)
						inventory[0] = null;
					if(casing!=null)
					{
						if(expelCasings)
						{
							double cX = getPos().getX()+.5;
							double cY = getPos().getY()+1.375;
							double cZ = getPos().getZ()+.5;
							Vec3d vCasing = vec.rotateYaw(-1.57f);
							worldObj.spawnParticle(EnumParticleTypes.REDSTONE, cX+vCasing.xCoord, cY+vCasing.yCoord, cZ+vCasing.zCoord, 0,0,0, 1,0);
							EntityItem entCasing = new EntityItem(worldObj, cX+vCasing.xCoord, cY+vCasing.yCoord, cZ+vCasing.zCoord, casing.copy());
							entCasing.motionX = 0;
							entCasing.motionY = -0.01;
							entCasing.motionZ = 0;
							worldObj.spawnEntityInWorld(entCasing);
						} else
						{
							if(inventory[1]==null)
								inventory[1] = casing.copy();
							else
								inventory[1].stackSize += casing.stackSize;
						}
					}
					worldObj.playSound(getPos().getX(), getPos().getY(), getPos().getZ(), IESounds.revolverFire, SoundCategory.BLOCKS, 1f, 1f, true);
				}
			}
		}
	}

	EntityRevolvershot getBulletEntity(World world, Vec3d vecDir, IBullet type)
	{
		EntityRevolvershot bullet = new EntityRevolvershot(world, getPos().getX()+.5+vecDir.xCoord,getPos().getY()+1.375+vecDir.yCoord,getPos().getZ()+.5+vecDir.zCoord, 0,0,0, type);
		bullet.motionX = vecDir.xCoord;
		bullet.motionY = vecDir.yCoord;
		bullet.motionZ = vecDir.zCoord;
		return bullet;
	}

	@Override
	public ItemStack[] getInventory()
	{
		return inventory;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public void update()
	{
		if(worldObj.isRemote&&!dummy&&cycleRender>0)
			cycleRender--;
		super.update();
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message)
	{
		if(message.hasKey("cycle"))
			cycleRender = 5;
	}
	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		super.receiveMessageFromClient(message);
		if(message.hasKey("expelCasings"))
			expelCasings = message.getBoolean("expelCasings");
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		expelCasings = nbt.getBoolean("expelCasings");
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 2);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setBoolean("expelCasings", expelCasings);
		if(!descPacket)
			Utils.writeInventory(inventory);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
//		if(!dummy && capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing==null||facing==EnumFacing.DOWN||facing==this.facing.getOpposite()))
//			return true;
		return super.hasCapability(capability, facing);
	}
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
//		if(!dummy && capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing==null||facing==EnumFacing.DOWN||facing==this.facing.getOpposite()))
//			return (T)tank;
		return super.getCapability(capability, facing);
	}
}