package blusunrize.immersiveengineering.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCaseMinecart;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.lib.manual.ManualUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

public class ItemShader extends ItemIEBase implements IShaderItem
{
	public ItemShader()
	{
		super("shader", 1);
		this.icons = new IIcon[4];

		addShader("Rosequartz", 0, EnumRarity.common, new int[]{65,35,35,255}, new int[]{230,180,180,255}, new int[]{240,205,205,255},new int[]{230,180,180,255});
		addShader("Argo", 2, EnumRarity.common, new int[]{45,45,45,255}, new int[]{220,220,220,255}, new int[]{220,120,35,255},new int[]{200,200,200,255});
		addShader("Sunstrike", 5, EnumRarity.rare, new int[]{115,115,115,255}, new int[]{205,105,0,255}, new int[]{215,58,0,185},new int[]{215,58,0,185});
		addShader("Locus", 2, EnumRarity.common, new int[]{10,10,10,255}, new int[]{74,74,74,255}, new int[]{132,150,76,255},new int[]{74,74,74,255});
		addShader("Felix", 1, EnumRarity.common, new int[]{10,10,10,255}, new int[]{74,74,74,255}, new int[]{240,136,3,255},new int[]{74,74,74,255});
		addShader("Sharkface", 2, EnumRarity.uncommon, new int[]{10,10,10,255}, new int[]{74,74,74,255}, new int[]{145,0,8,255},new int[]{74,74,74,255}, "shark",true,null);
		addShader("Dragon's Breath", 1, EnumRarity.uncommon, new int[]{25,25,25,255}, new int[]{51,63,43,255}, new int[]{138,138,138,255},new int[]{138,138,138,255}, "shark",true,null);
		addShader("Falconmoon", 3, EnumRarity.common, new int[]{103,99,107,255}, new int[]{244,238,235,255}, new int[]{45,45,45,255},new int[]{244,238,235,255});
		addShader("Magnum", 1, EnumRarity.common, new int[]{86,56,44,255},new int[]{220,220,220,255},new int[]{160,160,160,255},new int[]{220,220,220,255});
		addShader("Fox", 2, EnumRarity.uncommon, new int[]{45,45,45,255}, new int[]{212,126,49,255}, new int[]{234,234,234,255},new int[]{234,234,234,255});
		addShader("Vault-Tec", 0, EnumRarity.common, new int[]{86,56,44,255},new int[]{26,71,133,255},new int[]{192,170,80,255},new int[]{170,170,170,255});
		addShader("Sponsor", 0, EnumRarity.epic, new int[]{25,25,25,255}, new int[]{247,27,36,255}, new int[]{255,255,255,255},new int[]{170,170,170,255}, "sponsor",false,null);
		((ShaderCaseMinecart)ShaderRegistry.getShader("Sponsor", "minecart")).mirrorSideForPass[2]=false;
		addShader("Mass Fusion", 3, EnumRarity.rare, new int[]{110,90,55,255}, new int[]{57,71,48,255}, new int[]{84,84,84,255}, new int[]{170,170,170,255}, "fusion",true,null);

		addShader("StormFlower", 1, EnumRarity.common, new int[]{39,52,39,255},new int[]{40,111,48,255},new int[]{75,146,85,255},new int[]{40,111,48,255});
		addShader("Mil\u00f3", 2, EnumRarity.uncommon, new int[]{59,27,16,255},new int[]{103,0,4,255},new int[]{206,126,16,255},new int[]{103,0,4,255});
		addShader("Trident", 2, EnumRarity.uncommon, new int[]{81,81,81,255},new int[]{168,168,168,255},new int[]{41,211,255,255},new int[]{175,175,175,255});
		addShader("Chloris", 4, EnumRarity.rare, new int[]{56,50,42,255},new int[]{56,50,42,255},new int[]{136,250,190,255},new int[]{200,200,200,255});
		addShader("Crescent Rose", 2, EnumRarity.common, new int[]{20,20,20,255},new int[]{145,0,8,255},new int[]{8,8,8,255},new int[]{164,164,164,255});
		addShader("Qrow", 2, EnumRarity.uncommon, new int[]{109,28,17,255},new int[]{216,215,208,255},new int[]{49,54,64,255},new int[]{177,167,163,255});

		addShader("Vanguard", 3, EnumRarity.uncommon, new int[]{55,55,55,255},new int[]{19,27,66,255},new int[]{184,108,20,255},new int[]{220,220,220,255});
		addShader("Regal", 4, EnumRarity.uncommon, new int[]{216,212,209,255},new int[]{67,28,29,255},new int[]{216,212,209,255},new int[]{216,212,209,255});
		addShader("Harrowed", 4, EnumRarity.rare, new int[]{22,19,33,255},new int[]{67,28,29,255},new int[]{22,19,33,255},new int[]{22,19,33,255});
		addShader("Taken", 5, EnumRarity.epic, new int[]{17,28,38,255},new int[]{17,28,38,255},new int[]{186,215,221,255},new int[]{17,28,38,255}, null,false,null);

		addShader("Angel's Thesis", 2, EnumRarity.epic, new int[]{30,30,30,255},new int[]{117,70,151,255},new int[]{119,185,61,255},new int[]{80,80,80,255}, null,false,null);

		addShader("Warbird", 7, EnumRarity.uncommon, new int[]{49,54,64,255},new int[]{216,215,208,255},new int[]{235,172,0,255},new int[]{216,215,208,255});
		addShader("Matrix", 7, EnumRarity.rare, new int[]{5,63,60,255},new int[]{225,225,255,255},new int[]{212,255,255,255},new int[]{255,255,255,255}, "pipes",true,new int[]{132,221,216,255});
		addShader("Twili", 5, EnumRarity.epic, new int[]{85,93,112,255},new int[]{26,30,43,255},new int[]{34,39,57,255},new int[]{29,181,142,255}, "circuit",false,new int[]{29,181,142,255});
		addShader("Usurper", 3, EnumRarity.epic, new int[]{62,30,30,255},new int[]{92,97,86,255},new int[]{17,16,16,255},new int[]{115,122,108,255}, "circuit",false,new int[]{202,47,56,255});

		addShader("Glacis", 6, EnumRarity.rare, new int[]{73,155,194,255}, new int[]{118,208,249,200}, new int[]{189,255,253,200}, new int[]{189,255,253,200});
		addShader("Solum", 3, EnumRarity.rare, new int[]{104,90,72,255}, new int[]{208,161,111,200}, new int[]{148,116,96,180}, new int[]{148,116,96,180});
		addShader("Aero", 1, EnumRarity.rare, new int[]{99,159,98,255}, new int[]{120,233,128,220}, new int[]{232,255,220,160}, new int[]{120,233,128,220});
		addShader("Phoenix", 5, EnumRarity.rare, new int[]{117,0,0,255}, new int[]{208,0,0,255}, new int[]{255,127,0,255}, new int[]{255,127,0,255});

		addShader("Radiant", 3, EnumRarity.uncommon, new int[]{163,110,171,255},new int[]{248,219,188,255},new int[]{199,154,177,255},new int[]{216,216,227,255}, "pipes",true,new int[]{241,201,30,255});
		addShader("Hollow", 4, EnumRarity.uncommon, new int[]{84,45,28,255},new int[]{238,197,229,255},new int[]{204,137,128,255},new int[]{196,161,170,255}, "pipes",true,new int[]{196,152,56,255});
	}

	@Override
	public ShaderCase getShaderCase(ItemStack shader, ItemStack item, String shaderType)
	{
		String name = getShaderName(shader);
		return ShaderRegistry.getShader(name, shaderType);
	}

	public void addShader(String name, int overlayType, EnumRarity rarity, int[] colourBackground, int[] colourPrimary, int[] colourSecondary, int[] colourBlade)
	{
		this.addShader(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, colourBlade, null,true,null);
	}

	public void addShader(String name, int overlayType, EnumRarity rarity, int[] colourBackground, int[] colourPrimary, int[] colourSecondary, int[] colourBlade, String additionalTexture, boolean loot, int[] colourOverlay)
	{
		ShaderRegistry.ShaderRegistryEntry entry = ShaderRegistry.registerShader(name, Integer.toString(overlayType), rarity, colourPrimary, colourSecondary, colourBackground, colourBlade, additionalTexture, loot,true);
		if(colourOverlay!=null)
			for(ShaderCase sCase : entry.cases.values())
				sCase.setOverlayColour(colourOverlay);
		//		ShaderCaseRevolver revolver = IEApi.registerShader_Revolver(name, overlayType, colour0, colour1, colour2, colour3, additionalTexture);
		//		revolver.glowLayer = revolver_glow;
		//		IEApi.registerShader_Chemthrower(name, overlayType, colour0, colour1, colour2, true,false, additionalTexture);
		//		IEApi.registerShader_Minecart(name, overlayType, colour1, colour2, additionalTexture);
		//		IEApi.registerShader_Balloon(name, overlayType, colour1, colour2, additionalTexture);
	}

	public String getShaderName(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
		{
			String name = ItemNBTHelper.getString(stack, "shader_name");
			if(ShaderRegistry.shaderRegistry.containsKey(ItemNBTHelper.getString(stack, "shader_name")))
				return name;
			else
			{
				Set<String> keys = ShaderRegistry.shaderRegistry.keySet();
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
		list.add(StatCollector.translateToLocal("Level: "+this.getRarity(stack).rarityColor+this.getRarity(stack).rarityName));
		if(!GuiScreen.isShiftKeyDown())
			list.add(StatCollector.translateToLocal(Lib.DESC_INFO+"shader.applyTo")+" "+StatCollector.translateToLocal(Lib.DESC_INFO+"holdShift"));
		else
		{
			list.add(StatCollector.translateToLocal(Lib.DESC_INFO+"shader.applyTo"));
			String name = getShaderName(stack);
			if(name!=null && !name.isEmpty())
			{
				List<ShaderCase> array = ShaderRegistry.shaderRegistry.get(name).getCases();
				for(ShaderCase sCase : array)
					list.add(EnumChatFormatting.DARK_GRAY+" "+StatCollector.translateToLocal(Lib.DESC_INFO+"shader."+sCase.getShaderType()));
			}
		}
	}
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String s = getShaderName(stack);
		return super.getItemStackDisplayName(stack)+(s!=null&&!s.isEmpty()?(": "+s):"");
	}
	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		String s = getShaderName(stack);
		return ShaderRegistry.shaderRegistry.containsKey(s)?ShaderRegistry.shaderRegistry.get(s).getRarity():EnumRarity.common;
	}


	@Override
	public WeightedRandomChestContent getChestGenBase(ChestGenHooks chest, Random random, WeightedRandomChestContent original)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("shader_name", ShaderRegistry.chestLootShaders.get(random.nextInt(ShaderRegistry.chestLootShaders.size())));
		original.theItemId.setTagCompound(tag);
		return original;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(String key : ShaderRegistry.shaderList)
		{
			ItemStack s = new ItemStack(item);
			ItemNBTHelper.setString(s, "shader_name", key);
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
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			List<ShaderCase> array = ShaderRegistry.shaderRegistry.get(name).getCases();
			ShaderCase sCase = array.size()>0?array.get(0):null;
			if(sCase!=null)
			{
				int[] col = pass==0?sCase.getUnderlyingColour(): pass==1?sCase.getPrimaryColour(): sCase.getSecondaryColour();
				if(col!=null&&col.length>3)
					return (col[3]<<24)+(col[0]<<16)+(col[1]<<8)+col[2];
			}
		}
		return super.getColorFromItemStack(stack, pass);
	}
}