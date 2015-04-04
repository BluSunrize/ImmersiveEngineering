package blusunrize.immersiveengineering.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.IBullet;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;

public class ItemBullet extends ItemIEBase implements IBullet
{
	public ItemBullet()
	{
		super("bullet", 64, "emptyCasing","emptyShell","casull","armorPiercing","buckshot","HE","dragonsbreath");
	}

	@Override
	public ItemStack getCasing(ItemStack stack)
	{
		return new ItemStack(this, 1, stack.getItemDamage()==4||stack.getItemDamage()==6?1:0);
	}
	@Override
	public boolean canSpawnBullet(ItemStack bulletStack)
	{
		return bulletStack!=null && bulletStack.getItemDamage()>1;
	}
	@Override
	public void spawnBullet(EntityPlayer player, ItemStack bulletStack)
	{
		Vec3 vec = player.getLookVec();
		int type = bulletStack.getItemDamage()-2;
		switch(type)
		{
		case 0://casull
			doSpawnBullet(player, vec, vec, type);
			break;
		case 1://armorPiercing
			doSpawnBullet(player, vec, vec, type);
			break;
		case 2://buckshot
			for(int i=0; i<10; i++)
			{
				Vec3 vecDir = vec.addVector(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
				doSpawnBullet(player, vec, vecDir, type);
			}
			break;
		case 3://HE
			doSpawnBullet(player, vec, vec, type);
			break;
		case 4://dragonsbreath
			for(int i=0; i<30; i++)
			{
				Vec3 vecDir = vec.addVector(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
				EntityRevolvershot shot = doSpawnBullet(player, vec, vecDir, type);
				shot.setTickLimit(10);
				shot.setFire(3);
			}
			break;
		}
	}
	
	EntityRevolvershot doSpawnBullet(EntityPlayer player, Vec3 vecSpawn, Vec3 vecDir, int type)
	{
		double dX = player.posX+vecSpawn.xCoord;
		double dY = player.posY+player.getEyeHeight()+vecSpawn.yCoord;
		double dZ = player.posZ+vecSpawn.zCoord;
		EntityRevolvershot bullet = new EntityRevolvershot(player.worldObj, dX,dY,dZ, vecDir.xCoord,vecDir.yCoord,vecDir.zCoord, type);
		bullet.motionX = vecDir.xCoord;
		bullet.motionY = vecDir.yCoord;
		bullet.motionZ = vecDir.zCoord;
		player.worldObj.spawnEntityInWorld(bullet);
		return bullet;
	}
}