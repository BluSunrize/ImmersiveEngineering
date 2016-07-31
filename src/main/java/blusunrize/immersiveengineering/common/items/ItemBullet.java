package blusunrize.immersiveengineering.common.items;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IBullet;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershotFlare;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershotHoming;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBullet extends ItemIEBase implements IBullet
{
	public ItemBullet()
	{
		super("bullet", 64, "emptyCasing","emptyShell","casull","armorPiercing","buckshot","HE","dragonsbreath","homing","wolfpack","silver","potion","flare");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(int i=0;i<getSubNames().length;i++)
			if((i!=7&&i!=8) || (Loader.isModLoaded("Botania")&&Config.getBoolean("compat_Botania")))
				list.add(new ItemStack(this,1,i));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		if(stack.getItemDamage()==10)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
			if(pot!=null && pot.getItem() instanceof ItemPotion)
			{
				List effects = PotionUtils.getEffectsFromStack(pot);
				HashMultimap hashmultimap = HashMultimap.create();
				Iterator iterator1;
				if(effects != null && !effects.isEmpty())
				{
					iterator1 = effects.iterator();
					while(iterator1.hasNext())
					{
						PotionEffect potioneffect = (PotionEffect)iterator1.next();
						String s1 = I18n.format(potioneffect.getEffectName()).trim();
						Potion potion = potioneffect.getPotion();
						Map<IAttribute, AttributeModifier> map = potion.getAttributeModifierMap();

						if(map!=null && map.size()>0)
						{
							Iterator<Entry<IAttribute, AttributeModifier>> iterator = map.entrySet().iterator();
							while (iterator.hasNext())
							{
								Entry<IAttribute, AttributeModifier> entry = iterator.next();
								AttributeModifier attributemodifier = entry.getValue();
								AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), potion.getAttributeModifierAmount(potioneffect.getAmplifier(), attributemodifier), attributemodifier.getOperation());
								hashmultimap.put((entry.getKey()).getAttributeUnlocalizedName(), attributemodifier1);
							}
						}

						if (potioneffect.getAmplifier()>0)
							s1 = s1 + " " + I18n.format("potion.potency." + potioneffect.getAmplifier()).trim();
						if (potioneffect.getDuration()>20)
							s1 = s1 + " (" + Potion.getPotionDurationString(potioneffect,1) + ")";
						if (potion.isBadEffect())
							list.add(TextFormatting.RED + s1);
						else
							list.add(TextFormatting.GRAY + s1);
					}
				}
				else
				{
					String s = I18n.format("potion.empty").trim();
					list.add(TextFormatting.GRAY + s);
				}
				if(!hashmultimap.isEmpty())
				{
					list.add("");
					list.add(TextFormatting.DARK_PURPLE + I18n.format("potion.effects.whenDrank"));
					iterator1 = hashmultimap.entries().iterator();

					while(iterator1.hasNext())
					{
						Entry entry1 = (Entry)iterator1.next();
						AttributeModifier attributemodifier2 = (AttributeModifier)entry1.getValue();
						double d0 = attributemodifier2.getAmount();
						double d1;

						if(attributemodifier2.getOperation()!=1 && attributemodifier2.getOperation()!=2)
							d1 = attributemodifier2.getAmount();
						else
							d1 = attributemodifier2.getAmount() * 100.0D;

						if (d0>0.0D)
							list.add(TextFormatting.BLUE + I18n.format("attribute.modifier.plus." + attributemodifier2.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.format("attribute.name." + entry1.getKey())));
						else if(d0<0.0D)
						{
							d1 *= -1.0D;
							list.add(TextFormatting.RED + I18n.format("attribute.modifier.take." + attributemodifier2.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.format("attribute.name." + entry1.getKey())));
						}
					}
				}
			}
		}
		else if(stack.getItemDamage()==11)
		{
			String hexCol = Integer.toHexString(this.getColourForIEItem(stack, 1));
			list.add(I18n.format(Lib.DESC_INFO+"bullet.flareColour", "<hexcol="+hexCol+":#"+hexCol+">"));
		}
	}
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		if(stack.getItemDamage()==10)
		{
			String s = this.getUnlocalizedNameInefficiently(stack);
			ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
			if(pot!=null)
				if(pot.getItem() instanceof ItemLingeringPotion)
					s+=".linger";
				else if(pot.getItem() instanceof ItemSplashPotion)
					s+=".splash";
			return I18n.format(s+".name").trim();
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}
	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		if(stack.getItemDamage()==10 && pass==1)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
			if(pot!=null)
				return PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(pot));
		}
		if(stack.getItemDamage()==11 && pass==1)
			return ItemNBTHelper.hasKey(stack, "flareColour")?ItemNBTHelper.getInt(stack, "flareColour"):0xcc2e06;
		return super.getColourForIEItem(stack, pass);
	}

	@Override
	public ItemStack getCasing(ItemStack stack)
	{
		return new ItemStack(this, 1, stack.getItemDamage()==1||stack.getItemDamage()==4||stack.getItemDamage()==6||stack.getItemDamage()==11?1:0);
	}
	@Override
	public boolean canSpawnBullet(ItemStack bulletStack)
	{
		return bulletStack!=null && bulletStack.getItemDamage()>1 && (bulletStack.getItemDamage()!=10||ItemNBTHelper.getItemStack(bulletStack, "potion")!=null);
	}
	@Override
	public void spawnBullet(EntityPlayer player, ItemStack bulletStack, boolean electro)
	{
		Vec3d vec = player.getLookVec();
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
					Vec3d vecDir = vec.addVector(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
					doSpawnBullet(player, vec, vecDir, type, bulletStack, electro);
				}
				break;
			case 3://HE
				doSpawnBullet(player, vec, vec, type, bulletStack, electro);
				break;
			case 4://dragonsbreath
				for(int i=0; i<30; i++)
				{
					Vec3d vecDir = vec.addVector(player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1,player.getRNG().nextGaussian()*.1);
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
			case 8://Potion
				EntityRevolvershot shot = doSpawnBullet(player, vec, vec, type, bulletStack, electro);
				shot.bulletPotion = ItemNBTHelper.getItemStack(bulletStack, "potion");
				break;
			case 9://Flare
				EntityRevolvershotFlare flare = new EntityRevolvershotFlare(player.worldObj, player, vec.xCoord*1.5,vec.yCoord*1.5,vec.zCoord*1.5, type, bulletStack);
				flare.motionX = vec.xCoord;
				flare.motionY = vec.yCoord;
				flare.motionZ = vec.zCoord;
				flare.bulletElectro = electro;
				flare.colour = this.getColourForIEItem(bulletStack, 1);
				flare.setColourSynced();
				player.worldObj.spawnEntityInWorld(flare);
				break;
		}
	}

	EntityRevolvershot doSpawnBullet(EntityPlayer player, Vec3d vecSpawn, Vec3d vecDir, int type, ItemStack stack, boolean electro)
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