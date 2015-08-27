package blusunrize.immersiveengineering.common.items;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.tool.IBullet;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershotHoming;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;

import com.google.common.collect.HashMultimap;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBullet extends ItemIEBase implements IBullet
{
	IIcon iconPotion;
	public ItemBullet()
	{
		super("bullet", 64, "emptyCasing","emptyShell","casull","armorPiercing","buckshot","HE","dragonsbreath","homing","wolfpack","silver","potion");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(int i=0;i<getSubNames().length;i++)
			if((i!=7&&i!=8) || Loader.isModLoaded("Botania"))
				list.add(new ItemStack(this,1,i));
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
				List effects = ((ItemPotion)pot.getItem()).getEffects(pot);
				HashMultimap hashmultimap = HashMultimap.create();
				Iterator iterator1;
				if(effects != null && !effects.isEmpty())
				{
					iterator1 = effects.iterator();
					while(iterator1.hasNext())
					{
						PotionEffect potioneffect = (PotionEffect)iterator1.next();
						String s1 = StatCollector.translateToLocal(potioneffect.getEffectName()).trim();
						Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
						Map map = potion.func_111186_k();

						if(map!=null && map.size()>0)
						{
							Iterator iterator = map.entrySet().iterator();
							while (iterator.hasNext())
							{
								Entry entry = (Entry)iterator.next();
								AttributeModifier attributemodifier = (AttributeModifier)entry.getValue();
								AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), potion.func_111183_a(potioneffect.getAmplifier(), attributemodifier), attributemodifier.getOperation());
								hashmultimap.put(((IAttribute)entry.getKey()).getAttributeUnlocalizedName(), attributemodifier1);
							}
						}

						if (potioneffect.getAmplifier()>0)
							s1 = s1 + " " + StatCollector.translateToLocal("potion.potency." + potioneffect.getAmplifier()).trim();
						if (potioneffect.getDuration()>20)
							s1 = s1 + " (" + Potion.getDurationString(potioneffect) + ")";
						if (potion.isBadEffect())
							list.add(EnumChatFormatting.RED + s1);
						else
							list.add(EnumChatFormatting.GRAY + s1);
					}
				}
				else
				{
					String s = StatCollector.translateToLocal("potion.empty").trim();
					list.add(EnumChatFormatting.GRAY + s);
				}
				if(!hashmultimap.isEmpty())
				{
					list.add("");
					list.add(EnumChatFormatting.DARK_PURPLE + StatCollector.translateToLocal("potion.effects.whenDrank"));
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
							list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier2.getOperation(), new Object[] {ItemStack.field_111284_a.format(d1), StatCollector.translateToLocal("attribute.name." + (String)entry1.getKey())}));
						else if(d0<0.0D)
						{
							d1 *= -1.0D;
							list.add(EnumChatFormatting.RED + StatCollector.translateToLocalFormatted("attribute.modifier.take." + attributemodifier2.getOperation(), new Object[] {ItemStack.field_111284_a.format(d1), StatCollector.translateToLocal("attribute.name." + (String)entry1.getKey())}));
						}
					}
				}
			}
		}
	}

	@Override
	public void registerIcons(IIconRegister ir)
	{
		super.registerIcons(ir);
		this.iconPotion = ir.registerIcon("immersiveengineering:bullet_potion_layer");
	}
	@Override
	public IIcon getIconFromDamageForRenderPass(int meta, int pass)
	{
		if(meta==10 && pass==0)
			return iconPotion;
		return super.getIconFromDamageForRenderPass(meta, pass);
	}
	@Override
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}
	@Override
	public int getColorFromItemStack(ItemStack stack, int pass)
	{
		if(stack.getItemDamage()==10 && pass==0)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
			return PotionHelper.func_77915_a(pot!=null?pot.getItemDamage():0, false);
		}
		return super.getColorFromItemStack(stack, pass);
	}

	@Override
	public ItemStack getCasing(ItemStack stack)
	{
		return new ItemStack(this, 1, stack.getItemDamage()==1||stack.getItemDamage()==4||stack.getItemDamage()==6?1:0);
	}
	@Override
	public boolean canSpawnBullet(ItemStack bulletStack)
	{
		return bulletStack!=null && bulletStack.getItemDamage()>1 && (bulletStack.getItemDamage()!=10||ItemNBTHelper.getItemStack(bulletStack, "potion")!=null);
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
		case 8://Potion
			EntityRevolvershot shot = doSpawnBullet(player, vec, vec, type, bulletStack, electro);
			shot.bulletPotion = ItemNBTHelper.getItemStack(bulletStack, "potion");
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