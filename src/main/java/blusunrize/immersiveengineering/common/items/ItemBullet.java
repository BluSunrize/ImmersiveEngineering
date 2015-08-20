package blusunrize.immersiveengineering.common.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.tool.IBullet;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershotHoming;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBullet extends ItemIEBase implements IBullet
{
	public ItemBullet()
	{
		super("bullet", 64, "emptyCasing","emptyShell","casull","armorPiercing","buckshot","HE","dragonsbreath","homing","wolfpack","silver");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(int i=0;i<getSubNames().length;i++)
			if((i!=7&&i!=8) || Loader.isModLoaded("Botania"))
				list.add(new ItemStack(this,1,i));
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
		case 5://homing
			EntityRevolvershotHoming bullet = new EntityRevolvershotHoming(player.worldObj, player, vec.xCoord*1.5,vec.yCoord*1.5,vec.zCoord*1.5, type, bulletStack);
			bullet.motionX = vec.xCoord;
			bullet.motionY = vec.yCoord;
			bullet.motionZ = vec.zCoord;
			bullet.bulletElectro = electro;
			player.worldObj.spawnEntityInWorld(bullet);
			break;
		case 6://wolfpack
			doSpawnBullet(player, vec, vec, type, bulletStack, electro);
			break;
		case 7://Silver
			doSpawnBullet(player, vec, vec, type, bulletStack, electro);
			break;
		}
	}

	EntityRevolvershot doSpawnBullet(EntityPlayer player, Vec3 vecSpawn, Vec3 vecDir, int type, ItemStack stack, boolean electro)
	{
		EntityRevolvershot bullet = new EntityRevolvershot(player.worldObj, player, vecDir.xCoord*1.5,vecDir.yCoord*1.5,vecDir.zCoord*1.5, type, stack);
		bullet.motionX = vecDir.xCoord;
		bullet.motionY = vecDir.yCoord;
		bullet.motionZ = vecDir.zCoord;
		bullet.bulletElectro = electro;
		player.worldObj.spawnEntityInWorld(bullet);
		return bullet;
	}
}