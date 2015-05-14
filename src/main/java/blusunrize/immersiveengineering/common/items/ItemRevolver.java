package blusunrize.immersiveengineering.common.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IBullet;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;

import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRevolver extends ItemIEBase
{
	public ItemRevolver()
	{
		super("revolver", 1, "normal","elite","speedloader","speed","nerf");
	}

	@Override
	public void registerIcons(IIconRegister ir)
	{
		this.icons[2] = ir.registerIcon("immersiveengineering:"+itemName+"_"+"speedloader");
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(int i=0;i<3;i++)
			list.add(new ItemStack(this,1,i));
		//		for(SpecialRevolver r : eliteGunmen.values())
		//		{
		//			ItemStack s = new ItemStack(this,1,r.meta);
		//			if(r.tag!=null && !r.tag.isEmpty())
		//			{
		//				s.setTagCompound(new NBTTagCompound());
		//				s.getTagCompound().setString("elite",r.tag);
		//			}
		//			list.add(s);
		//		}
	}
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(ItemNBTHelper.hasKey(stack, "elite"))
			list.add(StatCollector.translateToLocal(Lib.DESC+"flavour.revolver."+ItemNBTHelper.getString(stack, "elite")));
		else if(stack.getItemDamage()==1)
			list.add(StatCollector.translateToLocal(Lib.DESC+"flavour.revolver.elite"));
		else
			list.add(StatCollector.translateToLocal(Lib.DESC+"flavour.revolver"));
	}
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "elite"))
			return this.getUnlocalizedName()+"."+ItemNBTHelper.getString(stack, "elite");
		return super.getUnlocalizedName(stack);
	}

	@Override
	public Multimap getAttributeModifiers(ItemStack stack)
	{
		Multimap multimap = super.getAttributeModifiers(stack);
		if(stack.getItemDamage()==1)
			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double)8, 0));
		if(stack.getItemDamage()==3)
			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", 0.3, 1));
		if(stack.getItemDamage()==4)
			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double)-1, 0));

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
		if(!world.isRemote && stack.getItemDamage()!=2 && ent!=null && ItemNBTHelper.hasKey(stack, "blocked"))
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
			if(player.isSneaking() || revolver.getItemDamage()==2)
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Revolver, world, (int)player.posX,(int)player.posY,(int)player.posZ);
			else if(revolver.getItemDamage()==4)
				player.addChatMessage(new ChatComponentText("*plop*"));
			else
			{
				ItemStack[] bullets = getBullets(revolver);

				if(isEmpty(revolver))
				{
					for(int i=0; i<player.inventory.getSizeInventory(); i++)
					{
						ItemStack loader = player.inventory.getStackInSlot(i);
						if(loader!=null && loader.getItem() == this && loader.getItemDamage()==2 && !isEmpty(loader))
						{
							int dc = 0;
							for(ItemStack b : bullets)
								if(b!=null)
								{
									world.spawnEntityInWorld(new EntityItem(world, player.posX,player.posY,player.posZ, b.getItem() instanceof IBullet?((IBullet)b.getItem()).getCasing(b):b ));
									dc++;
								}
							world.playSoundAtEntity(player, "fire.ignite", .5f, 3);
							ItemNBTHelper.setDelayedSoundsForStack(revolver, "casings", "random.successful_hit",.05f,5, dc/2, 8,2);
							setBullets(revolver, getBullets(loader));
							setBullets(loader, new ItemStack[8]);
							ItemNBTHelper.setBoolean(revolver, "blocked", true);
							return revolver;
						}
					}
				}

				if(!ItemNBTHelper.getBoolean(revolver, "blocked"))
				{
					if(bullets[0]!=null && bullets[0].getItem() instanceof IBullet && ((IBullet)bullets[0].getItem()).canSpawnBullet(bullets[0]))
					{
						((IBullet)bullets[0].getItem()).spawnBullet(player, bullets[0]);
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
		ItemStack[] stackList = new ItemStack[getBulletSlotAmount(revolver)];
		if(revolver.hasTagCompound())
		{
			NBTTagList inv = revolver.getTagCompound().getTagList("Bullets",10);
			for (int i=0; i<inv.tagCount(); i++)
			{
				NBTTagCompound tag = inv.getCompoundTagAt(i);
				int slot = tag.getByte("Slot") & 0xFF;
				if ((slot >= 0) && (slot < stackList.length))
					stackList[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
		return stackList;
	}
	public void setBullets(ItemStack revolver, ItemStack[] stackList)
	{
		NBTTagList inv = new NBTTagList();
		for (int i = 0; i < stackList.length; i++)
			if (stackList[i] != null)
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)i);
				stackList[i].writeToNBT(tag);
				inv.appendTag(tag);
			}
		if(!revolver.hasTagCompound())
			revolver.setTagCompound(new NBTTagCompound());
		revolver.getTagCompound().setTag("Bullets",inv);
	}
	public int getBulletSlotAmount(ItemStack revolver)
	{
		return (ItemNBTHelper.getInt(revolver, "upgrade")&1)==1?14:8;
	}

	public String getRevolverTexture(ItemStack revolver)
	{
		if(revolver.hasTagCompound() && revolver.getTagCompound().hasKey("elite"))
			return "immersiveengineering:textures/models/revolver_"+revolver.getTagCompound().getString("elite")+".png";
		if(revolver.getItemDamage()==1)
			return "immersiveengineering:textures/models/revolver_dev.png";
		else if(revolver.getItemDamage()==4)
			return "immersiveengineering:textures/models/revolver_nerf.png";
		else
			return "immersiveengineering:textures/models/revolver.png";
	}

	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player)
	{
		if(stack!=null && player!=null && eliteGunmen.containsKey(player.getUniqueID().toString()))
		{
			SpecialRevolver r = eliteGunmen.get(player.getUniqueID().toString());
			stack.setItemDamage(r.meta);
			if(r.tag!=null && !r.tag.isEmpty())
				ItemNBTHelper.setString(stack, "elite", r.tag);
			ItemNBTHelper.setInt(stack, "upgrade", r.upgrades);
		}
	}

	static final Map<String, SpecialRevolver> eliteGunmen;
	static
	{
		HashMap<String, SpecialRevolver> map = new HashMap<String, SpecialRevolver>();
		SpecialRevolver r = new SpecialRevolver(1,"fenrir",0);
		map.put("f34afdfb-996b-4020-b8a2-b740e2937b29", r);
		r = new SpecialRevolver(1,"",1);
		map.put("07c11943-628b-4671-a331-84899d08e538", r);
		map.put("48a16fc8-bc1f-4e72-84e9-7ec73b7d8ea1", r);
		r = new SpecialRevolver(3,"sns",0);
		map.put("e8b46b33-3e17-4b64-8d07-9af116df7d3b", r);
		map.put("58d506e2-7ee7-4774-8b22-c7a57eda488b", r);
		map.put("df0f4696-8a55-4777-b49d-6b38d6e1b501", r);
		map.put("b72d87ce-fa98-4a5a-b5a0-5db51a018d09", r);
		r = new SpecialRevolver(4,"nerf",0);
		map.put("4f3a8d1e-33c1-44e7-bce8-e683027c7dac", r);
		r = new SpecialRevolver(1,"earthshaker",0);
		map.put("c2024e2a-dd76-4bc9-9ea3-b771f18f23b6", r);
		r = new SpecialRevolver(1,"bee",0);
		map.put("ca5a40eb-9f48-4b40-bb94-3e0f2d18c9a7", r);
		r = new SpecialRevolver(0,"warlord",0);
		map.put("c2e83bd4-e8df-40d6-a639-58ba8b05401e", r);
		eliteGunmen = Collections.unmodifiableMap(map);
	}
	static class SpecialRevolver
	{
		final int meta;
		final String tag;
		final int upgrades;
		public SpecialRevolver(int meta, String tag, int upgrades)
		{
			this.meta=meta;
			this.tag=tag;
			this.upgrades=upgrades;
		}
	}
}