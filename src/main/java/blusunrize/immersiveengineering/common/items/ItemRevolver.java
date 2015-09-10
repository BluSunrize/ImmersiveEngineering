package blusunrize.immersiveengineering.common.items;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.IBullet;
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.InventoryStorageItem;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRevolver extends ItemUpgradeableTool
{
	public ItemRevolver()
	{
		super("revolver", 1, IUpgrade.UpgradeType.REVOLVER, "normal","speedloader");
	}

	@Override
	public void registerIcons(IIconRegister ir)
	{
		this.icons[1] = ir.registerIcon("immersiveengineering:"+itemName+"_"+"speedloader");
	}

	public HashMap<String, IIcon> revolverIcons = new HashMap();
	public IIcon revolverDefaultTexture;
	public void stichRevolverTextures(IIconRegister ir)
	{
		revolverDefaultTexture = ir.registerIcon("immersiveengineering:revolver");
		for(String key : specialRevolversByTag.keySet())
			if(!key.isEmpty())
			{
				int split = key.lastIndexOf("_");
				if(split<0)
					split = key.length();
				
//				revolverIcons.put(key, ir.registerIcon("immersiveengineering:revolver_"+key.substring(0,split)));
			}
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 18+2;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, InventoryStorageItem invItem)
	{
		return new Slot[]
				{
				new IESlot.Upgrades(container, invItem,18+0, 80,32, IUpgrade.UpgradeType.REVOLVER, stack, true),
				new IESlot.Upgrades(container, invItem,18+1,100,32, IUpgrade.UpgradeType.REVOLVER, stack, true),
				};
	}
	@Override
	public boolean canModify(ItemStack stack)
	{
		return stack.getItemDamage()!=1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(int i=0;i<2;i++)
			list.add(new ItemStack(this,1,i));
		//		for(Map.Entry<String, SpecialRevolver> e : specialRevolversByTag.entrySet())
		//		{
		//			ItemStack stack = new ItemStack(this,1,0);
		//			applySpecialCrafting(stack, e.getValue());
		//			this.recalculateUpgrades(stack);
		//			list.add(stack);
		//		}
	}
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(stack.getItemDamage()!=1)
		{
			String tag = getRevolverDisplayTag(stack);
			if(!tag.isEmpty())
				list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"revolver."+tag));
			else if(ItemNBTHelper.hasKey(stack, "flavour"))
				list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"revolver."+ItemNBTHelper.getString(stack, "flavour")));
			else if(stack.getItemDamage()==0)
				list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"revolver"));
		}
	}
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if(stack.getItemDamage()!=1)
		{
			String tag = getRevolverDisplayTag(stack);
			if(!tag.isEmpty())
				return this.getUnlocalizedName()+"."+tag;
		}
		return super.getUnlocalizedName(stack);
	}
	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public Multimap getAttributeModifiers(ItemStack stack)
	{
		Multimap multimap = super.getAttributeModifiers(stack);
		double melee = getUpgrades(stack).getDouble("melee");
		if(melee!=0)
			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", melee, 0));
		double speed = getUpgrades(stack).getDouble("speed");
		if(speed!=0)
			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", speed, 1));
		return multimap;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.bow;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		if(!world.isRemote && stack.getItemDamage()!=1 && ent!=null && ItemNBTHelper.hasKey(stack, "blocked"))
		{
			int l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "casings", ent);
			if(l==0)
				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderFill", "tile.piston.in",.3f,3, 1,6,1);
			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderFill", ent);
			if(l==0)
				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderClose", "fire.ignite",.6f,5, 1,6,1);
			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderClose", ent);
			if(l==0)
				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderSpin", "note.hat",.1f,5, 5,8,1);
			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderSpin", ent);
			if(l==0)
				ItemNBTHelper.remove(stack, "blocked");
		}
	}
	@Override
	public ItemStack onItemRightClick(ItemStack revolver, World world, EntityPlayer player)
	{
		if(!world.isRemote)
		{
			if(player.isSneaking() || revolver.getItemDamage()==1)
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Revolver, world, (int)player.posX,(int)player.posY,(int)player.posZ);
			else if(this.getUpgrades(revolver).getBoolean("nerf"))
				world.playSoundAtEntity(player, "random.pop", 1F, .6f);
			else
			{
				ItemStack[] bullets = getBullets(revolver);

				if(isEmpty(revolver))
				{
					for(int i=0; i<player.inventory.getSizeInventory(); i++)
					{
						ItemStack loader = player.inventory.getStackInSlot(i);
						if(loader!=null && loader.getItem() == this && loader.getItemDamage()==1 && !isEmpty(loader))
						{
							int dc = 0;
							for(ItemStack b : bullets)
								if(b!=null)
								{
									world.spawnEntityInWorld(new EntityItem(world, player.posX,player.posY,player.posZ, b ));
									dc++;
								}
							world.playSoundAtEntity(player, "fire.ignite", .5f, 3);
							ItemNBTHelper.setDelayedSoundsForStack(revolver, "casings", "random.successful_hit",.05f,5, dc/2, 8,2);
							setBullets(revolver, getBullets(loader));
							setBullets(loader, new ItemStack[8]);
							player.inventory.setInventorySlotContents(i, loader);
							player.inventory.markDirty();

							ItemNBTHelper.setBoolean(revolver, "blocked", true);
							return revolver;
						}
					}
				}

				if(!ItemNBTHelper.getBoolean(revolver, "blocked"))
				{
					if(bullets[0]!=null && bullets[0].getItem() instanceof IBullet && ((IBullet)bullets[0].getItem()).canSpawnBullet(bullets[0]))
					{
						((IBullet)bullets[0].getItem()).spawnBullet(player, bullets[0], getUpgrades(revolver).getBoolean("electro"));
						bullets[0]= ((IBullet)bullets[0].getItem()).getCasing(bullets[0]);
						world.playSoundAtEntity(player, "fireworks.blast", .6f, 1);
						world.playSoundAtEntity(player, "mob.wither.shoot", .3f, 5f);
					}
					else
						world.playSoundAtEntity(player, "note.hat", .6f, 3);

					ItemStack[] cycled = new ItemStack[getBulletSlotAmount(revolver)];
					for(int i=1; i<cycled.length; i++)
						cycled[i-1] = bullets[i];
					cycled[cycled.length-1] = bullets[0];
					setBullets(revolver, cycled);
				}
			}
		}
		return revolver;
	}

	public boolean isEmpty(ItemStack stack)
	{
		ItemStack[] bullets = getBullets(stack);
		boolean empty = true;
		for(ItemStack b : bullets)
			if(b!=null && b.getItem() instanceof IBullet && ((IBullet)b.getItem()).canSpawnBullet(b))
				empty=false;
		return empty;
	}
	public ItemStack[] getBullets(ItemStack revolver)
	{
		ItemStack[] stackList = this.getContainedItems(revolver);
		ItemStack[] bullets = new ItemStack[getBulletSlotAmount(revolver)];
		System.arraycopy(stackList,0, bullets,0, bullets.length);
		return bullets;
	}
	public void setBullets(ItemStack revolver, ItemStack[] bullets)
	{
		ItemStack[] stackList = this.getContainedItems(revolver);
		for(int i=0; i<bullets.length; i++)
			stackList[i] = bullets[i];
		this.setContainedItems(revolver, stackList);
	}
	public int getBulletSlotAmount(ItemStack revolver)
	{
		return 8+this.getUpgrades(revolver).getInteger("bullets");
	}
	public NBTTagCompound getUpgradeBase(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "baseUpgrades");
	}

	public String getRevolverDisplayTag(ItemStack revolver)
	{
		String tag = ItemNBTHelper.getString(revolver, "elite");
		if(!tag.isEmpty())
		{
			int split = tag.lastIndexOf("_");
			if(split<0)
				split = tag.length();
			return tag.substring(0,split);
		}
		return "";
	}

	public IIcon getRevolverIcon(ItemStack revolver)
	{
		String tag = ItemNBTHelper.getString(revolver, "elite");
		if(!tag.isEmpty())
			return this.revolverIcons.get(tag);
		else
			return this.revolverDefaultTexture;
	}

	public String[] compileRender(ItemStack revolver)
	{
		HashSet<String> render = new HashSet<String>();
		render.add("revolver_frame");
		render.add("barrel");
		render.add("cosmetic_compensator");
		String tag = ItemNBTHelper.getString(revolver, "elite");
		String flavour = ItemNBTHelper.getString(revolver, "flavour");
		if(tag!=null && !tag.isEmpty() && specialRevolversByTag.containsKey(tag))
		{
			SpecialRevolver r = specialRevolversByTag.get(tag);
			if(r!=null && r.renderAdditions!=null)
				for(String ss : r.renderAdditions)
					render.add(ss);
		}
		else if(flavour!=null && !flavour.isEmpty() && specialRevolversByTag.containsKey(flavour))
		{
			SpecialRevolver r = specialRevolversByTag.get(flavour);
			if(r!=null && r.renderAdditions!=null)
				for(String ss : r.renderAdditions)
					render.add(ss);
		}
		NBTTagCompound upgrades = this.getUpgrades(revolver);
		if(upgrades.getInteger("bullets")>0 && !render.contains("dev_mag"))
			render.add("player_mag");
		if(upgrades.getDouble("melee")>0 && !render.contains("dev_bayonet"))
		{
			render.add("bayonet_attachment");
			render.add("player_bayonet");
		}
		if(upgrades.getBoolean("electro"))
		{
			render.add("player_electro_0");
			render.add("player_electro_1");
		}
		return render.toArray(new String[render.size()]);
	}


	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player)
	{
		if(stack==null || player==null)
			return;
		if(stack.getItemDamage()==1)
			return;
		player.triggerAchievement(IEAchievements.makeRevolver);
	    String uuid = player.getUniqueID().toString();
		if(specialRevolvers.containsKey(uuid))
		{
			ArrayList<SpecialRevolver> list = new ArrayList(specialRevolvers.get(uuid));
			if(!list.isEmpty())
			{
				list.add(null);
				String existingTag = ItemNBTHelper.getString(stack, "elite");
				if(existingTag.isEmpty())
					applySpecialCrafting(stack, list.get(0));
				else
				{
					int i=0;
					for(; i<list.size(); i++)
						if(list.get(i)!=null && existingTag.equals(list.get(i).tag))
							break;
					int next = (i+1)%list.size();
					applySpecialCrafting(stack, list.get(next));
				}
			}
		}
		this.recalculateUpgrades(stack);
	}
	public void applySpecialCrafting(ItemStack stack, SpecialRevolver r)
	{
		if(r==null)
		{
			ItemNBTHelper.remove(stack, "elite");
			ItemNBTHelper.remove(stack, "flavour");
			ItemNBTHelper.remove(stack, "baseUpgrades");
			return;
		}
		if(r.tag!=null && !r.tag.isEmpty())
			ItemNBTHelper.setString(stack, "elite", r.tag);
		if(r.flavour!=null && !r.flavour.isEmpty())
			ItemNBTHelper.setString(stack, "flavour", r.flavour);
		NBTTagCompound baseUpgrades = new NBTTagCompound();
		for(Map.Entry<String, Object> e : r.baseUpgrades.entrySet())
		{
			if(e.getValue() instanceof Boolean)
				baseUpgrades.setBoolean(e.getKey(), (Boolean)e.getValue());
			else if(e.getValue() instanceof Integer)
				baseUpgrades.setInteger(e.getKey(), (Integer)e.getValue());
			else if(e.getValue() instanceof Float)
				baseUpgrades.setDouble(e.getKey(), (Float)e.getValue());
			else if(e.getValue() instanceof Double)
				baseUpgrades.setDouble(e.getKey(), (Double)e.getValue());
			else if(e.getValue() instanceof String)
				baseUpgrades.setString(e.getKey(), (String)e.getValue());
		}
		ItemNBTHelper.setTagCompound(stack, "baseUpgrades", baseUpgrades);
	}
	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
		ItemStack[] contents = this.getContainedItems(stack);
		if(contents[18]!=null&&contents[19]!=null)
			player.triggerAchievement(IEAchievements.upgradeRevolver);
	}

	public static final ArrayListMultimap<String, SpecialRevolver> specialRevolvers = ArrayListMultimap.create();
	public static final Map<String, SpecialRevolver> specialRevolversByTag = new HashMap<String, SpecialRevolver>();
	static
	{
		//		HashMap<String, SpecialRevolver> map = new HashMap<String, SpecialRevolver>();
		//		SpecialRevolver r = new SpecialRevolver(1,"fenrir",0,"");
		//		map.put("f34afdfb-996b-4020-b8a2-b740e2937b29", r);
		//		r = new SpecialRevolver(1,"",1,"");
		//		map.put("07c11943-628b-4671-a331-84899d08e538", r);
		//		map.put("48a16fc8-bc1f-4e72-84e9-7ec73b7d8ea1", r);
		//		r = new SpecialRevolver(3,"sns",0,"");
		//		map.put("e8b46b33-3e17-4b64-8d07-9af116df7d3b", r);
		//		map.put("58d506e2-7ee7-4774-8b22-c7a57eda488b", r);
		//		map.put("df0f4696-8a55-4777-b49d-6b38d6e1b501", r);
		//		map.put("b72d87ce-fa98-4a5a-b5a0-5db51a018d09", r);
		//		r = new SpecialRevolver(4,"nerf",0,"");
		//		map.put("4f3a8d1e-33c1-44e7-bce8-e683027c7dac", r);
		//		r = new SpecialRevolver(1,"earthshaker",0,"");
		//		map.put("c2024e2a-dd76-4bc9-9ea3-b771f18f23b6", r);
		//		r = new SpecialRevolver(1,"bee",0,"");
		//		map.put("ca5a40eb-9f48-4b40-bb94-3e0f2d18c9a7", r);
		//		r = new SpecialRevolver(0,"warlord",0,"");
		//		map.put("c2e83bd4-e8df-40d6-a639-58ba8b05401e", r);
		//		r = new SpecialRevolver(0,"",0,"rommie");
		//		map.put("4f1b6e70-4a7d-45e0-a69e-3550d528cd89", r);
		//		eliteGunmen = Collections.unmodifiableMap(map);
		//TODO Myst,Kihira
	}


	public static class SpecialRevolver
	{
		public final String[] uuid;
		public final String tag;
		public final String flavour;
		public final HashMap<String, Object> baseUpgrades;
		public final String[] renderAdditions;
		public SpecialRevolver(String[] uuid, String tag, String flavour, HashMap<String, Object> baseUpgrades, String[] renderAdditions)
		{
			this.uuid=uuid;
			this.tag=tag;
			this.flavour=flavour;
			this.baseUpgrades=baseUpgrades;
			this.renderAdditions=renderAdditions;
		}
	}
}