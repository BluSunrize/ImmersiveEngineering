package blusunrize.immersiveengineering.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCaseRevolver;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IEVillagerTradeHandler.MerchantItem;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.lib.manual.ManualUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemShader extends ItemIEBase implements IShaderItem
{
	public ItemShader()
	{
		super("shader", 1);
		this.icons = new IIcon[4];

		addShader("Rosequartz", 0, new int[]{65,35,35,255}, new int[]{230,180,180,255}, new int[]{240,205,205,255},new int[]{230,180,180,255});
		addShader("Argo", 2, new int[]{45,45,45,255}, new int[]{220,220,220,255}, new int[]{220,120,35,255},new int[]{200,200,200,255});
		addShader("Sunstrike", 5, new int[]{115,115,115,255}, new int[]{205,105,0,255}, new int[]{215,58,0,185},new int[]{215,58,0,185});
		addShader("Locus", 2, new int[]{10,10,10,255}, new int[]{74,74,74,255}, new int[]{132,150,76,255},new int[]{74,74,74,255});
		addShader("Felix", 1, new int[]{10,10,10,255}, new int[]{74,74,74,255}, new int[]{240,136,3,255},new int[]{74,74,74,255});
		addShader("Sharkface", 2, new int[]{10,10,10,255}, new int[]{74,74,74,255}, new int[]{145,0,8,255},new int[]{74,74,74,255}, "shark",-1);
		addShader("Dragon's Breath", 1, new int[]{25,25,25,255}, new int[]{51,63,43,255}, new int[]{138,138,138,255},new int[]{138,138,138,255}, "shark",-1);
		addShader("Falconmoon", 3, new int[]{103,99,107,255}, new int[]{244,238,235,255}, new int[]{45,45,45,255},new int[]{244,238,235,255});
		addShader("Magnum", 1, new int[]{86,56,44,255},new int[]{220,220,220,255},new int[]{160,160,160,255},new int[]{220,220,220,255});
		addShader("Vault-Tec", 0, new int[]{86,56,44,255},new int[]{26,71,133,255},new int[]{192,170,80,255},new int[]{170,170,170,255});
		addShader("Sponsor", 0, new int[]{25,25,25,255}, new int[]{247,27,36,255}, new int[]{255,255,255,255},new int[]{170,170,170,255}, "sponsor",-1);
		addShader("Mass Fusion", 3, new int[]{110,90,55,255}, new int[]{57,71,48,255}, new int[]{84,84,84,255}, new int[]{170,170,170,255}, "fusion",-1);
		
		addShader("StormFlower", 1, new int[]{39,52,39,255},new int[]{40,111,48,255},new int[]{75,146,85,255},new int[]{40,111,48,255});
		addShader("Mil\u00f3", 2, new int[]{59,27,16,255},new int[]{103,0,4,255},new int[]{206,126,16,255},new int[]{103,0,4,255});
		addShader("Trident", 2, new int[]{81,81,81,255},new int[]{168,168,168,255},new int[]{41,211,255,255},new int[]{175,175,175,255});
		addShader("Chloris", 4, new int[]{56,50,42,255},new int[]{56,50,42,255},new int[]{136,250,190,255},new int[]{200,200,200,255});
		addShader("Crescent Rose", 2, new int[]{20,20,20,255},new int[]{145,0,8,255},new int[]{8,8,8,255},new int[]{164,164,164,255}, null,0x001);

		addShader("Vanguard", 3, new int[]{55,55,55,255},new int[]{19,27,66,255},new int[]{184,108,20,255},new int[]{220,220,220,255});
		addShader("Regal", 4, new int[]{216,212,209,255},new int[]{67,28,29,255},new int[]{216,212,209,255},new int[]{216,212,209,255});
		addShader("Harrowed", 4, new int[]{22,19,33,255},new int[]{67,28,29,255},new int[]{22,19,33,255},new int[]{22,19,33,255});
		addShader("Taken", 5, new int[]{17,28,38,255},new int[]{17,28,38,255},new int[]{186,215,221},new int[]{17,28,38,255});
	
		addShader("Angel's Thesis", 2, new int[]{30,30,30,255},new int[]{117,70,151,255},new int[]{119,185,61,255},new int[]{80,80,80,255});
	}

	@Override
	public ShaderCase getShaderCase(ItemStack shader, ItemStack item, String shaderType)
	{
		String name = getShaderName(shader);
		return IEApi.getShader(name, shaderType);
	}

	public void addShader(String name, int overlayType, int[] colour0, int[] colour1, int[] colour2, int[] colour3)
	{
		this.addShader(name, overlayType, colour0, colour1, colour2, colour3, null,-1);
	}

	public void addShader(String name, int overlayType, int[] colour0, int[] colour1, int[] colour2, int[] colour3, String revolver_additionalTexture, int revolver_glow)
	{
		ShaderCaseRevolver revolver = IEApi.registerShader_Revolver(name, overlayType, colour0, colour1, colour2, colour3, revolver_additionalTexture);
		revolver.glowLayer = revolver_glow;
		
		IEApi.registerShader_Chemthrower(name, overlayType, colour0, colour1, colour2, true,false, revolver_additionalTexture);
		
		//		NBTTagCompound tag = new NBTTagCompound();
		//		tag.setString("shader_name", name);
		//		tag.setInteger("shader_overlay", overlayType);
		//		tag.setIntArray("shader_colour0", colour0);
		//		tag.setIntArray("shader_colour1", colour1);
		//		tag.setIntArray("shader_colour2", colour2);
		//		tag.setIntArray("shader_colour3", colour3);
		//		if(additionalTexture!=null && !additionalTexture.isEmpty())
		//			tag.setString("shader_extraTexture", additionalTexture);
		//		IEApi.shaderList.add(tag);
	}


	public String getShaderName(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
		{
			String name = ItemNBTHelper.getString(stack, "shader_name");
			if(IEApi.shaderCaseRegistry.containsKey(ItemNBTHelper.getString(stack, "shader_name")))
				return name;
			else
			{
				Set<String> keys = IEApi.shaderCaseRegistry.keySet();
				ArrayList<String> corrected = ManualUtils.getPrimitiveSpellingCorrections(name, keys.toArray(new String[keys.size()]), 4);
				if(!corrected.isEmpty())
				{
					IELogger.info("SHADER UPDATE: Fixing "+name+"to "+corrected.get(0));
					IELogger.info("Others: "+corrected);
					ItemNBTHelper.setString(stack, "shader_name", corrected.get(0));
					return corrected.get(0);
				}
			}
		}
		return "";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
	}
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String s = getShaderName(stack);
		return super.getItemStackDisplayName(stack)+(s!=null&&!s.isEmpty()?(": "+s):"");
	}


	@Override
	public WeightedRandomChestContent getChestGenBase(ChestGenHooks chest, Random random, WeightedRandomChestContent original)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("shader_name", IEApi.shaderList.get(random.nextInt(IEApi.shaderList.size())));
		original.theItemId.setTagCompound(tag);
		return original;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(String key : IEApi.shaderList)
		{
			ItemStack s = new ItemStack(item);
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("shader_name", key);
			s.setTagCompound(tag);
			list.add(s);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		for(int i=0; i<3; i++)
			this.icons[i] = ir.registerIcon("immersiveengineering:shader_"+i);
		this.icons[3] = ir.registerIcon("immersiveengineering:shader_slot");
	}
	@Override
	public IIcon getIconFromDamageForRenderPass(int meta, int pass)
	{
		return icons[pass];
	}
	@Override
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}
	@Override
	public int getRenderPasses(int metadata)
	{
		return 3;
	}
	@Override
	public int getColorFromItemStack(ItemStack stack, int pass)
	{
		String name = getShaderName(stack);
		if(IEApi.shaderCaseRegistry.containsKey(name))
		{
			ShaderCase sCase = IEApi.shaderCaseRegistry.get(name).get(0);
			int[] col = pass==0?sCase.getUnderlyingColour(): pass==1?sCase.getPrimaryColour(): sCase.getSecondaryColour();
			if(col!=null&&col.length>3)
				return (col[3]<<24)+(col[0]<<16)+(col[1]<<8)+col[2];
		}
		return super.getColorFromItemStack(stack, pass);
	}


	public static class ShaderMerchantItem extends MerchantItem
	{
		public ShaderMerchantItem()
		{
			super(IEContent.itemShader,1,1);
		}

		public ItemStack getItem(Random rand)
		{
			ItemStack s = Utils.copyStackWithAmount(this.item, 1);
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("shader_name", IEApi.shaderList.get(rand.nextInt(IEApi.shaderList.size())));
			s.setTagCompound(tag);
			return s;
		}
	}
}
