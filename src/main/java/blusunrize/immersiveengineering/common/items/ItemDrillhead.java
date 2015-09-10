package blusunrize.immersiveengineering.common.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;

public class ItemDrillhead extends ItemIEBase implements IDrillHead
{
	public ItemDrillhead()
	{
		super("drillhead", 1, "steel","iron");
		perms = new DrillHeadPerm[this.subNames.length];
		//Maximal damage is slightly proportionate to pickaxes
		addPerm(0, new DrillHeadPerm("ingotSteel",3,1,3,10,7,4000,"immersiveengineering:drill_diesel"));
		addPerm(1, new DrillHeadPerm("ingotIron",2,1,2,9,6,2000,"immersiveengineering:drill_iron"));
	}

	DrillHeadPerm[] perms;
	private void addPerm(int i, DrillHeadPerm perm)
	{
		if(i<perms.length)
			perms[i] = perm;
	}
	private DrillHeadPerm getHeadPerm(ItemStack stack)
	{
		if(stack.getItemDamage()>=0 && stack.getItemDamage()<perms.length)
			return perms[stack.getItemDamage()];
		return new DrillHeadPerm("",0,0,0,0,0,0,"immersiveengineering:textures/models/drill_diesel");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(stack.getItemDamage()<getSubNames().length)
		{
			list.add( StatCollector.translateToLocalFormatted(Lib.DESC_FLAVOUR+"drillhead.size", getMiningSize(stack),getMiningDepth(stack)));
			list.add( StatCollector.translateToLocalFormatted(Lib.DESC_FLAVOUR+"drillhead.level", Utils.getHarvestLevelName(getMiningLevel(stack))));
			list.add( StatCollector.translateToLocalFormatted(Lib.DESC_FLAVOUR+"drillhead.speed", Utils.formatDouble(getMiningSpeed(stack), "0.###")));
			list.add( StatCollector.translateToLocalFormatted(Lib.DESC_FLAVOUR+"drillhead.damage", Utils.formatDouble(getAttackDamage(stack), "0.###")));

			int maxDmg = getMaximumHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			String status = ""+(quote<.1?EnumChatFormatting.RED: quote<.3?EnumChatFormatting.GOLD: quote<.6?EnumChatFormatting.YELLOW: EnumChatFormatting.GREEN);
			String s = status+(getMaximumHeadDamage(stack)-getHeadDamage(stack))+"/"+getMaximumHeadDamage(stack);
			list.add( StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"durability", s));
		}
	}
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(int i=0;i<getSubNames().length;i++)
		{
			ItemStack s = new ItemStack(this,1,i);
			if(!OreDictionary.getOres(getHeadPerm(s).repairMaterial).isEmpty())
				list.add(s);
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		super.registerIcons(ir);
		for(DrillHeadPerm p : this.perms)
			p.icon = ir.registerIcon(p.texture);
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack material)
	{
		return Utils.compareToOreName(material, getHeadPerm(stack).repairMaterial);
	}

	@Override
	public boolean beforeBlockbreak(ItemStack drill, ItemStack head, EntityPlayer player)
	{
		return false;
	}

	@Override
	public void afterBlockbreak(ItemStack drill, ItemStack head, EntityPlayer player)
	{
	}

	@Override
	public int getMiningSize(ItemStack head)
	{
		return getHeadPerm(head).drillSize;
	}
	@Override
	public int getMiningDepth(ItemStack head)
	{
		return getHeadPerm(head).drillDepth;
	}

	@Override
	public int getMiningLevel(ItemStack head)
	{
		return getHeadPerm(head).drillLevel;
	}

	@Override
	public float getMiningSpeed(ItemStack head)
	{
		return getHeadPerm(head).drillSpeed;
	}
	@Override
	public float getAttackDamage(ItemStack head)
	{
		return getHeadPerm(head).drillAttack;
	}

	@Override
	public int getHeadDamage(ItemStack head)
	{
		return ItemNBTHelper.getInt(head, "headDamage");
	}

	@Override
	public int getMaximumHeadDamage(ItemStack head)
	{
		return getHeadPerm(head).maxDamage;
	}

	@Override
	public void damageHead(ItemStack head, int dmg)
	{
		ItemNBTHelper.setInt(head, "headDamage", ItemNBTHelper.getInt(head, "headDamage")+dmg);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double)ItemNBTHelper.getInt(stack, "headDamage") / (double)getMaximumHeadDamage(stack);
	}
	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "headDamage")>0;
	}

	@Override
	public IIcon getDrillTexture(ItemStack drill, ItemStack head)
	{
		DrillHeadPerm perm = getHeadPerm(head);
		return perm.icon!=null?perm.icon: IEContent.itemDrill.icons[0];
	}


	static class DrillHeadPerm
	{
		final String repairMaterial;
		final int drillSize;
		final int drillDepth;
		final int drillLevel;
		final float drillSpeed;
		final float drillAttack;
		final int maxDamage;
		final String texture;
		public IIcon icon;

		public DrillHeadPerm(String repairMaterial, int drillSize, int drillDepth, int drillLevel, float drillSpeed, int drillAttack, int maxDamage, String texture)
		{
			this.repairMaterial=repairMaterial;
			this.drillSize=drillSize;
			this.drillDepth=drillDepth;
			this.drillLevel=drillLevel;
			this.drillSpeed=drillSpeed;
			this.drillAttack=drillAttack;
			this.maxDamage=maxDamage;
			this.texture=texture;
		}
	}
}