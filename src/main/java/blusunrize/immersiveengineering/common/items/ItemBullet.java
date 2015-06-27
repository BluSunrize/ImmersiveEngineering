package blusunrize.immersiveengineering.common.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFireworkCharge;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.IBullet;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class ItemBullet extends ItemIEBase implements IBullet
{
	public ItemBullet()
	{
		super("bullet", 64, "emptyCasing","emptyShell","casull","armorPiercing","buckshot","HE","dragonsbreath");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b)
	{
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
			if(stack.hasTagCompound())
			{
				NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("Explosion");
				if (nbttagcompound != null)
					ItemFireworkCharge.func_150902_a(nbttagcompound, list);
			}
	}

	@Override
	public ItemStack getCasing(ItemStack stack)
	{
		return new ItemStack(this, 1, stack.getItemDamage()==1||stack.getItemDamage()==4||stack.getItemDamage()==6?1:0);
	}
	@Override
	public boolean canSpawnBullet(ItemStack bulletStack)
	{
		return bulletStack!=null && bulletStack.getItemDamage()>1;
	}
	@Override
	public void spawnBullet(EntityPlayer player, ItemStack bulletStack, boolean electro)
	{
		Vec3 vec = player.getLookVec();
		int type = bulletStack.getItemDamage()-2;
		switch(type)
		{
		case 0://casull
			doSpawnBullet(player, vec, vec, type, bulletStack, electro);
			break;
		case 1://armorPiercing
			doSpawnBullet(player, vec, vec, type, bulletStack, electro);
			break;
		case 2://buckshot
			for(int i=0; i<10; i++)
			{
				Vec3 vecDir = vec.addVector(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
				doSpawnBullet(player, vec, vecDir, type, bulletStack, electro);
			}
			break;
		case 3://HE
			doSpawnBullet(player, vec, vec, type, bulletStack, electro);
			break;
		case 4://dragonsbreath
			for(int i=0; i<30; i++)
			{
				Vec3 vecDir = vec.addVector(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
				EntityRevolvershot shot = doSpawnBullet(player, vec, vecDir, type, bulletStack, electro);
				shot.setTickLimit(10);
				shot.setFire(3);
			}
			break;
		}
	}

	EntityRevolvershot doSpawnBullet(EntityPlayer player, Vec3 vecSpawn, Vec3 vecDir, int type, ItemStack stack, boolean electro)
	{
		//		double dX = player.posX+vecSpawn.xCoord;
		//		double dY = player.posY+player.getEyeHeight()+vecSpawn.yCoord;
		//		double dZ = player.posZ+vecSpawn.zCoord;
		EntityRevolvershot bullet = new EntityRevolvershot(player.worldObj, player, vecDir.xCoord*1.5,vecDir.yCoord*1.5,vecDir.zCoord*1.5, type, stack);
		bullet.motionX = vecDir.xCoord;
		bullet.motionY = vecDir.yCoord;
		bullet.motionZ = vecDir.zCoord;
		bullet.bulletElectro = electro;
		player.worldObj.spawnEntityInWorld(bullet);
		player.mountEntity(bullet);
		return bullet;
	}
}