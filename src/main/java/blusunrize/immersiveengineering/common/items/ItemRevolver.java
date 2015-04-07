package blusunrize.immersiveengineering.common.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IBullet;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;

import com.google.common.collect.Multimap;

public class ItemRevolver extends ItemIEBase
{
	public ItemRevolver()
	{
		super("revolver", 1, "normal","elite","speedloader");
	}

	@Override
	public void registerIcons(IIconRegister ir)
	{
		this.icons[2] = ir.registerIcon("immersiveengineering:"+itemName+"_"+"speedloader");
	}
	
	@Override
	public Multimap getAttributeModifiers(ItemStack stack)
	{
		Multimap multimap = super.getAttributeModifiers(stack);
		if(stack.getItemDamage()==1)
			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double)8, 0));
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
		if(!world.isRemote && stack.getItemDamage()<2 && ent!=null && ItemNBTHelper.hasKey(stack, "blocked"))
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
			{
				System.out.println("unblock!");
				ItemNBTHelper.remove(stack, "blocked");
			}
		}
	}
	@Override
	public ItemStack onItemRightClick(ItemStack revolver, World world, EntityPlayer player)
	{
		if(!world.isRemote)
		{
			if(player.isSneaking() || revolver.getItemDamage()==2)
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Revolver, world, (int)player.posX,(int)player.posY,(int)player.posZ);
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
						((ItemBullet)bullets[0].getItem()).spawnBullet(player, bullets[0]);
						bullets[0]= ((ItemBullet)bullets[0].getItem()).getCasing(bullets[0]);
						world.playSoundAtEntity(player, "fireworks.blast", .6f, 1);
						world.playSoundAtEntity(player, "mob.wither.shoot", .3f, 5f);
					}
					else
						world.playSoundAtEntity(player, "note.hat", .6f, 3);

					ItemStack[] cycled = new ItemStack[8];
					for(int i=1; i<cycled.length; i++)
						cycled[i-1] = bullets[i];
					cycled[7] = bullets[0];
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
		ItemStack[] stackList = new ItemStack[8];
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
}